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
import com.github.vatbub.tictactoe.Board;
import com.github.vatbub.tictactoe.common.*;
import com.github.vatbub.tictactoe.view.Main;
import common.internet.Internet;
import javafx.application.Platform;
import logging.FOKLogger;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Does all the networking tasks for the online multiplayer game.
 */
@SuppressWarnings({"WeakerAccess"})
public class KryoGameConnections {
    private static Client relayKryoClient;
    private static boolean gameConnected;
    private static OnOpponentFoundRunnable onOpponentFoundRunnable;
    private static OnlineMultiplayerRequestOpponentRequest lastOpponentRequest;

    private static Board connectedBoard;

    @SuppressWarnings("unused")
    public static void connect() throws IOException {
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

    public static void connect(Runnable onConnected) throws IOException {
        // vatbubtictactoeserver.herokuapp.com
        // connect("52.59.117.143", onConnected);
        // connect("localhost", onConnected);
        // connect("SURFACEFREDERIK", onConnected);
        // connect("ec2-35-158-95-215.eu-central-1.compute.amazonaws.com", onConnected);
        // connect("35.156.178.255", onConnected);
        connect("ec2-35-156-178-255.eu-central-1.compute.amazonaws.com", onConnected);
    }

    public static void connect(String host, Runnable onConnected) throws IOException {
        connect(host, 90, onConnected);
    }

    public static void connect(String host, int tcpPort, Runnable onConnected) throws IOException {
        if (relayKryoClient != null) {
            resetConnections();
        }

        relayKryoClient = new Client();

        // ping the host to wake him up (e. g. om heroku)
        String pingAddress = "http://" + host;
        FOKLogger.info(KryoGameConnections.class.getName(), "Pinging the host " + pingAddress + " to wake him up...");
        try {
            String pingResponse = Internet.webread(new URL(pingAddress));
            FOKLogger.info(KryoGameConnections.class.getName(), "Ping response: " + pingResponse);
        } catch (Exception e) {
            FOKLogger.severe(KryoGameConnections.class.getName(), "Ping failed. Still trying to connect using KryoNet, reason for the failed ping: " + e.getLocalizedMessage());
        }

        relayKryoClient.start();
        KryoCommon.registerRequiredClasses(relayKryoClient.getKryo());
        relayKryoClient.getKryo().setReferences(true);
        //registerGameClasses(relayKryoClient.getKryo());
        relayKryoClient.setKeepAliveTCP(2500);

        relayKryoClient.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof OnlineMultiplayerRequestOpponentResponse) {
                    OnlineMultiplayerRequestOpponentResponse response = (OnlineMultiplayerRequestOpponentResponse) object;
                    FOKLogger.info(KryoGameConnections.class.getName(), "Received OnlineMultiplayerRequestOpponentResponse");
                    FOKLogger.info(KryoGameConnections.class.getName(), "Response code: " + response.getResponseCode());
                    if (response.getResponseCode().equals(ResponseCode.OpponentFound)) {
                        // connect the game
                        if (!gameConnected) {
                            gameConnected = true;
                            FOKLogger.info(KryoGameConnections.class.getName(), "Connecting the game...");
                        } else {
                            FOKLogger.severe(KryoGameConnections.class.getName(), "Cannot connect the game, game is already connected");
                            connection.sendTCP(new GameException("Node already connected"));
                        }
                    }
                    if (onOpponentFoundRunnable != null) {
                        onOpponentFoundRunnable.run(response);
                    }
                } else if (object instanceof Move && connection != null) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "The game received a move!");
                    doMove((Move) object);
                } else if (object instanceof CancelGameRequest && connection != null) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "The game received a CancelGameRequest!");
                    connection.sendTCP(new CancelGameResponse());
                    cancelGame();
                } else if (object instanceof FrameworkMessage.KeepAlive) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "Received keepAlive message from server");
                } else if (object instanceof GameException) {
                    FOKLogger.log(KryoGameConnections.class.getName(), Level.SEVERE, "Received a GameException!", (GameException) object);
                } else {
                    FOKLogger.severe(KryoGameConnections.class.getName(), "Received illegal object");
                }
            }
        });

        relayKryoClient.connect(5000, host, tcpPort);

        if (onConnected != null) {
            onConnected.run();
        }
    }

    public static void requestOpponent(String clientIdentifier, String desiredOpponentIdentifier, OnOpponentFoundRunnable onOpponentFound) {
        OnlineMultiplayerRequestOpponentRequest request = new OnlineMultiplayerRequestOpponentRequest();
        request.setClientIdentifier(clientIdentifier);
        request.setDesiredOpponentIdentifier(desiredOpponentIdentifier);
        request.setOperation(Operation.RequestOpponent);

        requestOpponent(request, onOpponentFound);
    }

    public static void requestOpponent(OnlineMultiplayerRequestOpponentRequest request, OnOpponentFoundRunnable onOpponentFound) {
        if (relayKryoClient == null) {
            throw new IllegalStateException("Not connected to the relay server");
        }

        FOKLogger.info(KryoGameConnections.class.getName(), "Requesting an opponent...");
        onOpponentFoundRunnable = onOpponentFound;
        lastOpponentRequest = request;
        relayKryoClient.sendTCP(request);
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
        if (relayKryoClient == null) {
            throw new IllegalStateException("Not connected to the relay server");
        }

        FOKLogger.info(KryoGameConnections.class.getName(), "Aborting the opponent request with id " + request.getRequestId() + "...");

        request.setOperation(Operation.AbortRequest);
        relayKryoClient.sendTCP(request);
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
        if (relayKryoClient != null) {
            relayKryoClient.sendTCP(move);
        } else {
            throw new IllegalStateException("Game not connected");
        }
    }

    public static void sendCancelGameRequest() {
        FOKLogger.info(KryoGameConnections.class.getName(), "Cancelling the game...");
        if (relayKryoClient != null) {
            relayKryoClient.sendTCP(new CancelGameRequest());
        } else {
            throw new IllegalStateException("Game not connected");
        }
    }

    public static void cancelGame() {
        KryoGameConnections.resetConnections();
        Platform.runLater(() -> Main.currentMainWindowInstance.showErrorMessage("The game was cancelled.", "The opponent cancelled the game."));
    }

    /**
     * Closes all internet connections and aborts pending opponent requests
     */
    public static void resetConnections() {
        FOKLogger.info(KryoGameConnections.class.getName(), "Resetting all kryo connections...");
        if (relayKryoClient != null) {
            Client oldRelayKryoClient = relayKryoClient;
            abortLastOpponentRequestIfApplicable();
            oldRelayKryoClient.stop();
            relayKryoClient = null;
        }
    }
}
