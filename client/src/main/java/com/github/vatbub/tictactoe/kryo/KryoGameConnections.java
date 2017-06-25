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


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryonet.*;
import com.github.vatbub.tictactoe.Board;
import com.github.vatbub.tictactoe.common.*;
import com.github.vatbub.tictactoe.view.Main;
import common.internet.Internet;
import logging.FOKLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.logging.Level;

/**
 * Does all the networking tasks for the online multiplayer game.
 */
public class KryoGameConnections {
    public static final int gameServerTCPPort = 8181;

    private static Client relayKryoClient;
    private static Client gameKryoClient;
    private static Server gameKryoServer;
    private static boolean gameConnected;
    private static OnOpponentFoundRunnable onOpponentFoundRunnable;
    private static OnlineMultiplayerRequestOpponentRequest lastOpponentRequest;
    /**
     * Only used by the game server, not the client
     */
    private static Connection gameConnection;
    private static Board connectedBoard;

    public static void connect() throws IOException {
        connect((Runnable) null);
    }

    public static void connect(String host) throws IOException {
        connect(host, null);
    }

    public static void connect(String host, int tcpPort) throws IOException {
        connect(host, tcpPort, null);
    }

    public static void connect(Runnable onConnected) throws IOException {
        // vatbubtictactoeserver.herokuapp.com
        // connect("52.59.117.143", onConnected);
        connect("localhost", onConnected);
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
                        FOKLogger.info(KryoGameConnections.class.getName(), "Opponents inet address");
                        FOKLogger.info(KryoGameConnections.class.getName(), response.getOpponentInetSocketAddress().toString());
                    }
                    if (onOpponentFoundRunnable != null) {
                        onOpponentFoundRunnable.run(response);
                    }
                } else if (object instanceof FrameworkMessage.KeepAlive) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "Received keepAlive message from server");
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

    public static void requestOpponent(String clientIdentifier, OnOpponentFoundRunnable onOpponentFound) {
        requestOpponent(clientIdentifier, null, onOpponentFound);
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

    private static void registerGameClasses(Kryo kryo) {
        kryo.register(Board.Move.class, new JavaSerializer());
        kryo.register(StartGameRequest.class, new JavaSerializer());
        kryo.register(StartGameResponse.class, new JavaSerializer());
        kryo.register(StartGameException.class, new JavaSerializer());
    }

    public static void launchGameServer(int tcpPort, OnOpponentConnectedRunnable onConnected) throws IOException {
        if (gameKryoServer != null) {
            throw new IllegalStateException("Game server already running");
        } else {
            gameKryoServer = new Server();
        }

        if (gameConnected) {
            throw new IllegalStateException("Game already connected");
        }

        FOKLogger.info(KryoGameConnections.class.getName(), "Launching the game server...");

        KryoCommon.registerRequiredClasses(gameKryoServer.getKryo());
        registerGameClasses(gameKryoServer.getKryo());
        gameKryoServer.getKryo().setReferences(true);

        gameKryoServer.bind(tcpPort);
        gameKryoServer.start();

        gameKryoServer.addListener(new Listener() {
            @SuppressWarnings("Duplicates")
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof StartGameRequest) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "The game server received a StartGameRequest from " + connection.getRemoteAddressTCP().getHostString() + "!");
                    if (!gameConnected) {
                        gameConnected = true;
                        gameConnection = connection;
                        connection.sendTCP(new StartGameResponse());
                        FOKLogger.info(KryoGameConnections.class.getName(), "Connecting the game...");
                        onConnected.run(((StartGameRequest) object).getOpponentIdentifier());
                    } else {
                        FOKLogger.severe(KryoGameConnections.class.getName(), "Cannot connect the game, game is already connected");
                        connection.sendTCP(new StartGameException("Node already connected"));
                    }
                } else if (object instanceof Board.Move && connection != null) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "The game server received a move!");
                    doMove((Board.Move) object);
                } else if (object instanceof FrameworkMessage.KeepAlive) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "Received keepAlive message from server");
                } else if (object instanceof StartGameException) {
                    FOKLogger.log(KryoGameConnections.class.getName(), Level.SEVERE, "Received a StartGameException!", (StartGameException) object);
                } else {
                    FOKLogger.severe(KryoGameConnections.class.getName(), "Received illegal object");
                }
            }
        });
    }

    public static void launchGameClient(String clientIdentifier, InetSocketAddress serverAddress, Runnable onConnected) throws IOException {
        if (gameKryoClient != null) {
            throw new IllegalStateException("Game client already running");
        } else {
            gameKryoClient = new Client();
        }

        if (gameConnected) {
            throw new IllegalStateException("Game already connected");
        }

        FOKLogger.info(KryoGameConnections.class.getName(), "Launching the game client...");

        KryoCommon.registerRequiredClasses(gameKryoClient.getKryo());
        registerGameClasses(gameKryoClient.getKryo());
        gameKryoClient.getKryo().setReferences(true);
        gameKryoClient.setKeepAliveTCP(2500);

        gameKryoClient.start();

        gameKryoClient.addListener(new Listener() {
            @SuppressWarnings("Duplicates")
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof StartGameResponse) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "The game client received a StartGameResponse!");
                    if (!gameConnected) {
                        gameConnected = true;
                        FOKLogger.info(KryoGameConnections.class.getName(), "Connecting the game...");
                        onConnected.run();
                    } else {
                        FOKLogger.severe(KryoGameConnections.class.getName(), "Cannot connect the game, game is already connected");
                        connection.sendTCP(new StartGameException("Node already connected"));
                    }
                } else if (object instanceof Board.Move && connection != null) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "The game client received a move!");
                    doMove((Board.Move) object);
                } else if (object instanceof FrameworkMessage.KeepAlive) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "Received keepAlive message from server");
                } else if (object instanceof StartGameException) {
                    FOKLogger.log(KryoGameConnections.class.getName(), Level.SEVERE, "Received a StartGameException!", (StartGameException) object);
                } else {
                    FOKLogger.severe(KryoGameConnections.class.getName(), "Received illegal object");
                }
            }
        });

        gameKryoClient.connect(5000, serverAddress.getHostName(), gameServerTCPPort);

        FOKLogger.info(KryoGameConnections.class.getName(), "Sending the StartGameRequest...");
        gameKryoClient.sendTCP(new StartGameRequest(clientIdentifier));
    }

    private static void doMove(Board.Move move) {
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

    public static void sendMove(Board.Move move) {
        FOKLogger.info(KryoGameConnections.class.getName(), "Sending a move...");
        if (gameKryoClient != null) {
            gameKryoClient.sendTCP(move);
        } else if (gameKryoServer != null) {
            gameConnection.sendTCP(move);
        } else {
            throw new IllegalStateException("Game not connected");
        }
    }

    /**
     * Closes all internet connections and aborts pending opponent requests
     */
    public static void resetConnections() {
        if (relayKryoClient != null) {
            Client oldRelayKryoClient = relayKryoClient;
            abortLastOpponentRequestIfApplicable();
            oldRelayKryoClient.stop();
            relayKryoClient = null;

        }
        if (gameKryoClient != null) {
            Client oldGameKryoClient = gameKryoClient;
            oldGameKryoClient.stop();
            gameKryoClient = null;
        }
        if (gameKryoServer != null) {
            Server oldGameKryoServer = gameKryoServer;
            oldGameKryoServer.stop();
            gameKryoServer = null;
        }
    }
}
