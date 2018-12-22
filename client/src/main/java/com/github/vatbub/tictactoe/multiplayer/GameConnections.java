package com.github.vatbub.tictactoe.multiplayer;

/*-
 * #%L
 * tictactoe
 * %%
 * Copyright (C) 2016 - 2017 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.github.vatbub.common.core.logging.FOKLogger;
import com.github.vatbub.tictactoe.Board;
import com.github.vatbub.tictactoe.common.*;
import com.github.vatbub.tictactoe.view.Main;
import com.google.gson.Gson;
import com.jsunsoft.http.HttpRequest;
import com.jsunsoft.http.HttpRequestBuilder;
import com.jsunsoft.http.NoSuchContentException;
import com.jsunsoft.http.ResponseDeserializer;
import javafx.application.Platform;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.logging.Level;

/**
 * Does all the networking tasks for the online multiplayer game.
 */
@SuppressWarnings({"WeakerAccess"})
public class GameConnections {
    private static final int DEFAULT_NETWORK_WAIT_TIME_IN_MS_SLOW = 1000;
    private static final int DEFAULT_NETWORK_WAIT_TIME_IN_MS_FAST = 100;
    private static GameConnections instance;
    private final Gson gson = new Gson();
    private String connectionId;
    private URL serverUrl;
    private OnlineMultiPlayerRequestOpponentRequest lastOpponentRequest;
    private Board connectedBoard;
    private Thread requestAndProcessGameDataThread;
    private Thread sendMovesThread;
    private boolean stopGameDataProcessing;
    private boolean stopMoveProcessing;
    private LinkedList<Move> pendingMoves = new LinkedList<>();

    public static GameConnections getInstance() {
        if (instance == null)
            instance = new GameConnections();
        return instance;
    }

    public void abortLastOpponentRequestIfApplicable() throws URISyntaxException {
        try {
            if (lastOpponentRequest != null)
                abortLastOpponentRequest();
        } catch (URISyntaxException e) {
            throw e;
        } catch (Exception e) {
            FOKLogger.log(getClass().getName(), Level.SEVERE, "Unable to abort the last opponent request due to an exception", e);
        }
    }

    public void abortLastOpponentRequest() throws URISyntaxException {
        abortOpponentRequest(lastOpponentRequest);
    }

    public void abortOpponentRequest(OnlineMultiPlayerRequestOpponentRequest request) throws URISyntaxException {
        if (!isConnectedToServer())
            throw new IllegalStateException("Not connected to the relay server");

        FOKLogger.info(GameConnections.class.getName(), "Aborting the opponent request with id " + request.getRequestId() + "...");

        request.setOperation(Operation.AbortRequest);
        doRequest(serverUrl, request);
    }

    private void doMove(Move move) {
        if (getConnectedBoard().getPlayerAt(move.getRow(), move.getColumn()) == null && Main.currentMainWindowInstance.isBlockedForInput()) {
            getConnectedBoard().doTurn(move);
            Main.currentMainWindowInstance.updateCurrentPlayerLabel();
            Main.currentMainWindowInstance.renderRows();
        }
    }

    public Board getConnectedBoard() {
        return connectedBoard;
    }

    public void setConnectedBoard(Board connectedBoard) {
        this.connectedBoard = connectedBoard;
    }

    public void sendMove(Move move) {
        FOKLogger.info(GameConnections.class.getName(), "Adding move to be sent: " + move.toString());
        if (isConnectedToServer())
            pendingMoves.addLast(move);
        else
            throw new IllegalStateException("Game not connected");
    }

    public void sendCancelGameRequest() throws URISyntaxException {
        FOKLogger.info(GameConnections.class.getName(), "Cancelling the game...");
        if (isConnectedToServer()) {
            doRequest(serverUrl, new CancelGameRequest(connectionId));
        }
    }

    public boolean isConnectedToServer() {
        return connectionId != null;
    }

    public boolean isGameConnected() throws URISyntaxException {
        if (!isConnectedToServer())
            throw new IllegalStateException("Not connected to a server");
        IsEnrolledInGameResponse response = doRequest(serverUrl, new IsEnrolledInGameRequest(connectionId));
        return response.isEnrolled();
    }

    public void cancelGame(@Nullable String reason) throws URISyntaxException {
        resetConnections(false);
        if (reason == null) {
            reason = "The opponent cancelled the game.";
        }
        @Nullable String finalReason = reason;
        Platform.runLater(() -> Main.currentMainWindowInstance.showErrorMessage("The game was cancelled.", finalReason));
    }

    public void resetConnections() throws URISyntaxException {
        resetConnections(true);
    }

    /**
     * Closes all internet connections and aborts pending opponent requests
     */
    public void resetConnections(boolean cancelGamesOnServer) throws URISyntaxException {
        FOKLogger.info(GameConnections.class.getName(), "Resetting all game connections...");

        FOKLogger.info(GameConnections.class.getName(), "Waiting for pending moves to be sent...");
        stopMoveProcessing();

        if (isConnectedToServer()) {
            abortLastOpponentRequestIfApplicable();
            RemoveDataRequest removeDataRequest = new RemoveDataRequest(connectionId);
            removeDataRequest.setCancelGames(cancelGamesOnServer);
            doRequest(serverUrl, removeDataRequest);
        }

        if (cancelGamesOnServer)
            stopGameDataProcessing();

        connectionId = null;
        serverUrl = null;
    }

    private String doRequest(URL url, String json) throws URISyntaxException {
        FOKLogger.info(getClass().getName(), "Sending the following json:\n" + json);
        HttpRequest<String> httpRequest = HttpRequestBuilder.createPost(url.toURI(), String.class)
                .responseDeserializer(ResponseDeserializer.ignorableDeserializer()).build();
        String responseJson = httpRequest.executeWithBody(json).get();
        FOKLogger.info(getClass().getName(), "Received the following json:\n" + responseJson);
        return responseJson;
    }

    @SuppressWarnings("unchecked")
    private <T extends ServerInteraction> T doRequest(URL url, ServerInteraction request) throws URISyntaxException {
        String json = doRequest(url, gson.toJson(request));
        ServerInteraction response = gson.fromJson(json, ServerInteractionImpl.class);
        try {
            Class clazz = Class.forName(response.getClassName());
            return gson.fromJson(json, (Type) clazz);
        } catch (ClassNotFoundException e) {
            return (T) response;
        }
    }

    private void doRequestAsync(URL url, ServerInteraction request) {
        doRequestAsync(url, request, null);
    }

    private <T extends ServerInteraction> void doRequestAsync(URL url, ServerInteraction request, @SuppressWarnings("SameParameterValue") @Nullable OnRequestCompletedRunnable onRequestCompletedRunnable) {
        new Thread(() -> {
            try {
                T response = doRequest(url, request);
                if (onRequestCompletedRunnable != null)
                    onRequestCompletedRunnable.run(response);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @SuppressWarnings("unused")
    public void connect(URL serverUrl) throws URISyntaxException {
        connect(serverUrl, null);
    }

    public void connect(URL serverUrl, Runnable onConnected) throws URISyntaxException {
        if (isConnectedToServer()) {
            resetConnections();
        }

        this.serverUrl = serverUrl;
        GetConnectionIdResponse getConnectionIdResponse = doRequest(serverUrl, new GetConnectionIdRequest());
        connectionId = getConnectionIdResponse.getConnectionId();

        if (onConnected != null) {
            onConnected.run();
        }
    }

    public void requestOpponent(String clientIdentifier, String desiredOpponentIdentifier, OnOpponentFoundRunnable onOpponentFound, OnExceptionRunnable onException) {
        OnlineMultiPlayerRequestOpponentRequest request = new OnlineMultiPlayerRequestOpponentRequest(connectionId);
        request.setClientIdentifier(clientIdentifier);
        request.setDesiredOpponentIdentifier(desiredOpponentIdentifier);
        request.setOperation(Operation.RequestOpponent);

        requestOpponent(request, onOpponentFound, onException);
    }

    public void requestOpponent(OnlineMultiPlayerRequestOpponentRequest request, OnOpponentFoundRunnable onOpponentFound, OnExceptionRunnable onException) {
        if (!isConnectedToServer())
            throw new IllegalStateException("Not connected to the relay server");

        FOKLogger.info(GameConnections.class.getName(), "Requesting an opponent...");
        lastOpponentRequest = request;

        Thread pollThread = new Thread(() -> {
            try {
                OnlineMultiPlayerRequestOpponentResponse response;
                while (true) {
                    FOKLogger.info(GameConnections.class.getName(), "Waiting for an opponent...");
                    response = doRequest(serverUrl, request);
                    FOKLogger.info(GameConnections.class.getName(), "Response code: " + response.getResponseCode());

                    if (response.getResponseCode() != ResponseCode.WaitForOpponent)
                        break;
                    else
                        Thread.sleep(DEFAULT_NETWORK_WAIT_TIME_IN_MS_SLOW);
                }

                lastOpponentRequest = null;
                if (response.getResponseCode() == ResponseCode.OpponentFound && onOpponentFound != null)
                    onOpponentFound.run(response);

                requestAndProcessGameData();
                sendMovesDaemon();
            } catch (Exception e) {
                onException.run(e);
            }
        });

        pollThread.start();
    }

    private void requestAndProcessGameData() {
        stopGameDataProcessing = false;
        requestAndProcessGameDataThread = new Thread(() -> {
            try {
                GetGameDataResponse response;
                while (!stopGameDataProcessing) {
                    if (serverUrl == null)
                        break;

                    response = doRequest(serverUrl, new GetGameDataRequest(connectionId));
                    if (response.isGameCancelled()) {
                        cancelGame(response.getCancelReason());
                        break;
                    }

                    if (response.getMoves() == null)
                        continue;

                    for (Move move : response.getMoves())
                        doMove(move);

                    Thread.sleep(DEFAULT_NETWORK_WAIT_TIME_IN_MS_FAST);
                }
            } catch (URISyntaxException | InterruptedException e) {
                throw new RuntimeException(e);
            } catch (NoSuchContentException e) {
                FOKLogger.log(getClass().getName(), Level.SEVERE, "Unable to get game data", e);
            }
        });
        requestAndProcessGameDataThread.start();
    }

    private void stopGameDataProcessing() {
        if (requestAndProcessGameDataThread == null || !requestAndProcessGameDataThread.isAlive())
            return;

        stopGameDataProcessing = true;
        try {
            requestAndProcessGameDataThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMovesDaemon() {
        stopMoveProcessing = false;
        sendMovesThread = new Thread(() -> {
            try {
                while (!stopMoveProcessing || !pendingMoves.isEmpty()) {
                    // send pending moves
                    Move pendingMove;
                    while ((pendingMove = pendingMoves.pollFirst()) != null) {
                        FOKLogger.info(getClass().getName(), "Sending move: " + pendingMove.toString());
                        doRequest(serverUrl, new MoveRequest(connectionId, pendingMove));
                    }
                    Thread.sleep(DEFAULT_NETWORK_WAIT_TIME_IN_MS_FAST);
                }
            } catch (URISyntaxException | InterruptedException e) {
                throw new RuntimeException(e);
            } catch (NoSuchContentException e) {
                FOKLogger.log(getClass().getName(), Level.SEVERE, "Unable to send moves", e);
            }
        });
        sendMovesThread.start();
    }

    private void stopMoveProcessing() {
        if (sendMovesThread == null || !sendMovesThread.isAlive())
            return;

        stopMoveProcessing = true;
        try {
            sendMovesThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Called when a opponent was found using {@link GameConnections#requestOpponent(String, String, OnOpponentFoundRunnable, OnExceptionRunnable)}
     *
     * @see GameConnections#requestOpponent(OnlineMultiPlayerRequestOpponentRequest, OnOpponentFoundRunnable, OnExceptionRunnable)
     * @see GameConnections#requestOpponent(String, String, OnOpponentFoundRunnable, OnExceptionRunnable)
     */
    public interface OnOpponentFoundRunnable {
        void run(OnlineMultiPlayerRequestOpponentResponse response);
    }

    public interface OnExceptionRunnable {
        void run(Throwable throwable);
    }

    private interface OnRequestCompletedRunnable {
        <T extends ServerInteraction> void run(T response);
    }
}
