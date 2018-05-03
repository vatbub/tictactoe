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


import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.github.vatbub.common.core.logging.FOKLogger;
import com.github.vatbub.tictactoe.Board;
import com.github.vatbub.tictactoe.common.*;
import com.github.vatbub.tictactoe.view.Main;
import javafx.application.Platform;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Does all the networking tasks for the online multiplayer game.
 */
@SuppressWarnings({"WeakerAccess"})
public class KryoGameConnections {
    private static Client kryoClient;
    private static OnOpponentFoundRunnable onOpponentFoundRunnable;
    private static Runnable onUnexpectedDisconnectRunnable;
    private static OnlineMultiplayerRequestOpponentRequest lastOpponentRequest;

    private static Board connectedBoard;

    @SuppressWarnings("unused")
    public static void connect() throws Exception {
        connect((Runnable) null);
    }

    @SuppressWarnings("unused")
    public static void connect(String host) throws IOException {
        connect(host, null);
    }

    @SuppressWarnings("unused")
    public static void connect(String host, int tcpPort) throws IOException {
        connect(host, tcpPort, null);
    }

    public static void connect(Runnable onConnected) throws Exception {
        com.github.vatbub.awsec2wakelauncher.applicationclient.Client wakeLauncherClient = new com.github.vatbub.awsec2wakelauncher.applicationclient.Client(new URL(Main.getApplicationConfiguration().getValue("defaultWakeServerURL")));
        com.github.vatbub.awsec2wakelauncher.applicationclient.Client.IpInfo ipInfo = wakeLauncherClient.launchAndWaitForInstance(Main.getApplicationConfiguration().getValue("defaultInstanceId"));
        connect(ipInfo.getInstanceDns(), onConnected);
    }

    public static void connect(String host, Runnable onConnected) throws IOException {
        connect(host, 90, onConnected);
    }

    public static void connect(String host, int tcpPort, Runnable onConnected) throws IOException {
        if (isGameConnected()) {
            resetConnections();
        }

        kryoClient = new Client();

        kryoClient.start();
        KryoCommon.registerRequiredClasses(kryoClient.getKryo());
        kryoClient.getKryo().setReferences(true);
        //registerGameClasses(kryoClient.getKryo());
        kryoClient.setKeepAliveTCP(2500);

        kryoClient.addListener(new Listener() {
            @Override
            public void disconnected(Connection connection) {
                if (onUnexpectedDisconnectRunnable != null) {
                    onUnexpectedDisconnectRunnable.run();
                }
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof OnlineMultiplayerRequestOpponentResponse) {
                    OnlineMultiplayerRequestOpponentResponse response = (OnlineMultiplayerRequestOpponentResponse) object;
                    FOKLogger.info(KryoGameConnections.class.getName(), "Received OnlineMultiplayerRequestOpponentResponse");
                    FOKLogger.info(KryoGameConnections.class.getName(), "Response code: " + response.getResponseCode());

                    if (onOpponentFoundRunnable != null) {
                        onOpponentFoundRunnable.run(response);
                    }
                } else if (object instanceof Move && connection != null) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "The game received a move!");
                    doMove((Move) object);
                } else if (object instanceof CancelGameRequest && connection != null) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "The game received a CancelGameRequest!");
                    connection.sendTCP(new CancelGameResponse());
                    cancelGame(((CancelGameRequest) object).getReason());
                } else if (object instanceof FrameworkMessage.KeepAlive) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "Received keepAlive message from server");
                } else if (object instanceof GameException) {
                    FOKLogger.log(KryoGameConnections.class.getName(), Level.SEVERE, "Received a GameException!", (GameException) object);
                } else {
                    FOKLogger.severe(KryoGameConnections.class.getName(), "Received illegal object");
                }
            }
        });

        kryoClient.connect(5000, host, tcpPort);

        if (onConnected != null) {
            onConnected.run();
        }
    }

    public static void requestOpponent(String clientIdentifier, String desiredOpponentIdentifier, OnOpponentFoundRunnable onOpponentFound, Runnable onUnexpectedDisconnect) {
        OnlineMultiplayerRequestOpponentRequest request = new OnlineMultiplayerRequestOpponentRequest();
        request.setClientIdentifier(clientIdentifier);
        request.setDesiredOpponentIdentifier(desiredOpponentIdentifier);
        request.setOperation(Operation.RequestOpponent);

        requestOpponent(request, onOpponentFound, onUnexpectedDisconnect);
    }

    public static void requestOpponent(OnlineMultiplayerRequestOpponentRequest request, OnOpponentFoundRunnable onOpponentFound, Runnable onUnexpectedDisconnect) {
        if (!isGameConnected()) {
            throw new IllegalStateException("Not connected to the relay server");
        }

        FOKLogger.info(KryoGameConnections.class.getName(), "Requesting an opponent...");
        onOpponentFoundRunnable = onOpponentFound;
        onUnexpectedDisconnectRunnable = onUnexpectedDisconnect;
        lastOpponentRequest = request;
        kryoClient.sendTCP(request);
    }

    public static void abortLastOpponentRequestIfApplicable() {
        if (lastOpponentRequest != null) {
            abortLastOpponentRequest();
        }
    }

    public static void abortLastOpponentRequest() {
        abortOpponentRequest(lastOpponentRequest);
    }

    public static void abortOpponentRequest(OnlineMultiplayerRequestOpponentRequest request) {
        if (!isGameConnected()) {
            throw new IllegalStateException("Not connected to the relay server");
        }

        FOKLogger.info(KryoGameConnections.class.getName(), "Aborting the opponent request with id " + request.getRequestId() + "...");

        request.setOperation(Operation.AbortRequest);
        kryoClient.sendTCP(request);
    }

    private static void doMove(Move move) {
        if (getConnectedBoard().getPlayerAt(move.getRow(), move.getColumn()) == null && Main.currentMainWindowInstance.isBlockedForInput()) {
            getConnectedBoard().doTurn(move);
            Main.currentMainWindowInstance.updateCurrentPlayerLabel();
            Main.currentMainWindowInstance.renderRows();
        }
    }


    public static Board getConnectedBoard() {
        return connectedBoard;
    }

    public static void setConnectedBoard(Board connectedBoard) {
        KryoGameConnections.connectedBoard = connectedBoard;
    }

    public static void sendMove(Move move) {
        FOKLogger.info(KryoGameConnections.class.getName(), "Sending a move...");
        if (isGameConnected()) {
            kryoClient.sendTCP(move);
        } else {
            throw new IllegalStateException("Game not connected");
        }
    }

    public static void sendCancelGameRequest() {
        FOKLogger.info(KryoGameConnections.class.getName(), "Cancelling the game...");
        if (isGameConnected()) {
            kryoClient.sendTCP(new CancelGameRequest());
        } else {
            throw new IllegalStateException("Game not connected");
        }
    }

    public static boolean isGameConnected() {
        return kryoClient != null;
    }

    public static void cancelGame(@Nullable String reason) {
        KryoGameConnections.resetConnections();
        if (reason == null) {
            reason = "The opponent cancelled the game.";
        }
        @Nullable String finalReason = reason;
        Platform.runLater(() -> Main.currentMainWindowInstance.showErrorMessage("The game was cancelled.", finalReason));
    }

    /**
     * Closes all internet connections and aborts pending opponent requests
     */
    public static void resetConnections() {
        FOKLogger.info(KryoGameConnections.class.getName(), "Resetting all kryo connections...");
        onUnexpectedDisconnectRunnable = null;
        if (isGameConnected()) {
            Client oldRelayKryoClient = kryoClient;
            abortLastOpponentRequestIfApplicable();
            oldRelayKryoClient.stop();
            kryoClient = null;
        }
    }
}
