package com.github.vatbub.tictactoe.kryo;

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

import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Does all the networking tasks for the online multiplayer game.
 */
@SuppressWarnings({"WeakerAccess"})
public class KryoGameConnections {
    public static final String COMMON_PACKAGE_NAME = "com.github.vatbub.tictactoe.common";
    private static KryoGameConnections instance;
    private final Gson gson = new Gson();
    private String connectionId;
    private URL serverUrl;
    private OnlineMultiPlayerRequestOpponentRequest lastOpponentRequest;
    private Board connectedBoard;
    private boolean gameConnected;
    private Thread requestAndProcessGameDataThread;
    private boolean stopGameDataProcessing;

    public static KryoGameConnections getInstance() {
        if (instance == null)
            instance = new KryoGameConnections();
        return instance;
    }

    public void abortLastOpponentRequestIfApplicable() throws URISyntaxException {
        if (lastOpponentRequest != null) {
            abortLastOpponentRequest();
        }
    }

    public void abortLastOpponentRequest() throws URISyntaxException {
        abortOpponentRequest(lastOpponentRequest);
    }

    public void abortOpponentRequest(OnlineMultiPlayerRequestOpponentRequest request) throws URISyntaxException {
        if (!isConnectedToServer())
            throw new IllegalStateException("Not connected to the relay server");

        FOKLogger.info(KryoGameConnections.class.getName(), "Aborting the opponent request with id " + request.getRequestId() + "...");

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

    public void sendMove(Move move) throws URISyntaxException {
        FOKLogger.info(KryoGameConnections.class.getName(), "Sending a move...");
        if (isConnectedToServer())
            doRequest(serverUrl, new MoveRequest(connectionId, move));
        else
            throw new IllegalStateException("Game not connected");
    }

    public void sendCancelGameRequest() throws URISyntaxException {
        FOKLogger.info(KryoGameConnections.class.getName(), "Cancelling the game...");
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
        IsEnrolledInGameResponse response = doRequestWithType(serverUrl, new IsEnrolledInGameRequest(connectionId));
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
        FOKLogger.info(KryoGameConnections.class.getName(), "Resetting all kryo connections...");

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

    private <T extends ServerInteraction> T doRequestWithType(URL url, ServerInteraction request) throws URISyntaxException {
        ServerInteraction response = doRequest(url, request);
        //noinspection unchecked
        return (T) response;
    }

    private ServerInteraction doRequest(URL url, ServerInteraction request) throws URISyntaxException {
        String json = doRequest(url, gson.toJson(request));
        ServerInteraction response = gson.fromJson(json, ServerInteractionImpl.class);
        switch (response.getClassName()) {
            case COMMON_PACKAGE_NAME + ".CancelGameResponse":
                return gson.fromJson(json, CancelGameResponse.class);
            case COMMON_PACKAGE_NAME + ".BadRequestException":
                return gson.fromJson(json, BadRequestException.class);
            case COMMON_PACKAGE_NAME + ".GetConnectionIdResponse":
                return gson.fromJson(json, GetConnectionIdResponse.class);
            case COMMON_PACKAGE_NAME + ".GetGameDataResponse":
                return gson.fromJson(json, GetGameDataResponse.class);
            case COMMON_PACKAGE_NAME + ".OnlineMultiPlayerRequestOpponentException":
                return gson.fromJson(json, OnlineMultiPlayerRequestOpponentException.class);
            case COMMON_PACKAGE_NAME + ".OnlineMultiPlayerRequestOpponentResponse":
                return gson.fromJson(json, OnlineMultiPlayerRequestOpponentResponse.class);
            case COMMON_PACKAGE_NAME + ".RemoveDataResponse":
                return gson.fromJson(json, RemoveDataResponse.class);
            case COMMON_PACKAGE_NAME + ".IsEnrolledInGameResponse":
                return gson.fromJson(json, IsEnrolledInGameResponse.class);
            case COMMON_PACKAGE_NAME + ".MoveResponse":
                return gson.fromJson(json, MoveResponse.class);
            default:
                return response;
        }
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
        GetConnectionIdResponse getConnectionIdResponse = doRequestWithType(serverUrl, new GetConnectionIdRequest());
        connectionId = getConnectionIdResponse.getConnectionId();

        if (onConnected != null) {
            onConnected.run();
        }
    }

    public void requestOpponent(String clientIdentifier, String desiredOpponentIdentifier, OnOpponentFoundRunnable onOpponentFound) {
        OnlineMultiPlayerRequestOpponentRequest request = new OnlineMultiPlayerRequestOpponentRequest(connectionId);
        request.setClientIdentifier(clientIdentifier);
        request.setDesiredOpponentIdentifier(desiredOpponentIdentifier);
        request.setOperation(Operation.RequestOpponent);

        requestOpponent(request, onOpponentFound);
    }

    public void requestOpponent(OnlineMultiPlayerRequestOpponentRequest request, OnOpponentFoundRunnable onOpponentFound) {
        if (!isConnectedToServer())
            throw new IllegalStateException("Not connected to the relay server");

        FOKLogger.info(KryoGameConnections.class.getName(), "Requesting an opponent...");
        lastOpponentRequest = request;

        Thread pollThread = new Thread(() -> {
            try {
                OnlineMultiPlayerRequestOpponentResponse response;
                int waitTime = 10;
                do {
                    Thread.sleep(waitTime);
                    waitTime = waitTime * 2;
                    FOKLogger.info(KryoGameConnections.class.getName(), "Waiting for an opponent...");
                    response = doRequestWithType(serverUrl, request);
                    FOKLogger.info(KryoGameConnections.class.getName(), "Response code: " + response.getResponseCode());
                } while (response.getResponseCode() == ResponseCode.WaitForOpponent);

                lastOpponentRequest = null;
                if (response.getResponseCode() == ResponseCode.OpponentFound && onOpponentFound != null)
                    onOpponentFound.run(response);

                requestAndProcessGameData();
            } catch (URISyntaxException | InterruptedException e) {
                throw new RuntimeException(e);
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
                    response = doRequestWithType(serverUrl, new GetGameDataRequest(connectionId));
                    if (response.isGameCancelled()) {
                        cancelGame(response.getCancelReason());
                        break;
                    }

                    if (response.getMoves() == null)
                        continue;

                    for (Move move : response.getMoves())
                        doMove(move);
                }
            } catch (URISyntaxException e) {
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
}
