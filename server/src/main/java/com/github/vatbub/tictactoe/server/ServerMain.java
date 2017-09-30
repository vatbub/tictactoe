package com.github.vatbub.tictactoe.server;

/*-
 * #%L
 * tictactoe-server
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


import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.github.vatbub.common.core.Common;
import com.github.vatbub.common.core.logging.FOKLogger;
import com.github.vatbub.tictactoe.common.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Launches the server
 */
@SuppressWarnings("WeakerAccess")
public class ServerMain {
    private static final Server server = new Server();
    private static final Map<Connection, Connection> connectionMap = new HashMap<>();
    private static Map<Connection, List<OnlineMultiplayerRequestOpponentRequest>> openRequests;
    private static int currentTcpPort;

    public static void main(String[] args) throws IOException {
        Common.getInstance().setAppName("tictactoeserver");
        List<String> argList = new ArrayList<>(args.length);
        argList.addAll(Arrays.asList(args));
        boolean launchSucceeded = false;
        while (!launchSucceeded) {
            try {
                if (argList.size() == 0) {
                    startServer(Integer.parseInt(System.getenv("PORT")));
                    launchSucceeded = true;
                } else if (argList.size() == 1) {
                    startServer(Integer.parseInt(argList.get(0)));
                    launchSucceeded = true;
                } else {
                    System.out.println("Too many arguments. The first argument must be the tcp port to run the server on. All other arguments are ignored. If no port is specified, the value will be taken from the PORT environment variable.");
                    startServer(Integer.parseInt(argList.get(0)));
                    launchSucceeded = true;
                }
            } catch (Exception e) {
                FOKLogger.log(ServerMain.class.getName(), Level.SEVERE, "An error occurred, probably because of an illegal command line argument", e);
                FOKLogger.log(ServerMain.class.getName(), Level.SEVERE, "Stripping the argument " + argList.get(0));
                launchSucceeded = false;
                argList.remove(0);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(ServerMain::shutDown));
    }

    /**
     * Starts the kyro.net server and binds it to the specified port
     *
     * @param tcpPort The port to use for the tcp protocol
     * @throws IOException If the specified port is already in use by another program
     */
    public static void startServer(int tcpPort) throws IOException {
        FOKLogger.info(ServerMain.class.getName(), "VatbubTicTacToeServer version " + Common.getInstance().getAppVersion());
        resetServer();
        currentTcpPort = tcpPort;
        server.getKryo().setReferences(true);
        KryoCommon.registerRequiredClasses(server.getKryo());
        server.start();
        FOKLogger.info(ServerMain.class.getName(), "Binding to tcpPort " + tcpPort);
        server.bind(tcpPort);
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                connection.setTimeout(100000);
            }

            @Override
            public void disconnected(Connection connection) {
                if (openRequests.containsKey(connection)) {
                    FOKLogger.info(ServerMain.class.getName(), "Removing all open requests from the client that just disconnected...");
                    openRequests.remove(connection);
                }
                if (connectionMap.containsKey(connection)) {
                    // send the opponent the message that the game has been disconnected
                    Connection matchingConnection = connectionMap.get(connection);

                    Thread sendCancelRequestThread = new Thread(() -> {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            FOKLogger.log(ServerMain.class.getName(), Level.SEVERE, "Error while a thread was sleeping", e);
                        }
                        if (matchingConnection.isConnected()) {
                            matchingConnection.sendTCP(new CancelGameRequest("The opponent disconnected from the server."));
                        }
                    });
                    sendCancelRequestThread.setName("sendCancelRequestThread");
                    sendCancelRequestThread.start();

                    connectionMap.remove(connection);
                    connectionMap.remove(matchingConnection);
                }
            }

            @Override
            public void received(Connection connection, Object object) {
                try {
                    if (object instanceof OnlineMultiplayerRequestOpponentRequest) {
                        OnlineMultiplayerRequestOpponentRequest receivedRequest = (OnlineMultiplayerRequestOpponentRequest) object;

                        FOKLogger.info(ServerMain.class.getName(), "Received OnlineMultiplayerRequestOpponentRequest");
                        FOKLogger.info(ServerMain.class.getName(), "Requested operation: " + receivedRequest.getOperation());
                        if (receivedRequest.getDesiredOpponentIdentifier() == null) {
                            FOKLogger.info(ServerMain.class.getName(), "The request contains no desired opponent");
                        } else {
                            FOKLogger.info(ServerMain.class.getName(), "The request contains a desired opponent");
                        }

                        OnlineMultiplayerRequestOpponentResponse response;

                        FOKLogger.info(ServerMain.class.getName(), "Checking for matching requests...");
                        if (receivedRequest.getOperation().equals(Operation.RequestOpponent)) {
                            // check if any of the open requests has a matching desiredOpponentIdentifier
                            Connection matchingConnection = null;
                            String matchingOpponentIdentifier = null;
                            for (Map.Entry<Connection, List<OnlineMultiplayerRequestOpponentRequest>> entry : openRequests.entrySet()) {
                                for (OnlineMultiplayerRequestOpponentRequest comparedRequest : entry.getValue()) {
                                    if (receivedRequest.getDesiredOpponentIdentifier() == null && comparedRequest.getDesiredOpponentIdentifier() == null) {
                                        // found two requests that both don't wish a particular opponent
                                        FOKLogger.info(ServerMain.class.getName(), "Found matching request!");
                                        matchingConnection = entry.getKey();
                                        matchingOpponentIdentifier = comparedRequest.getClientIdentifier();
                                        break;
                                    } else if (comparedRequest.getDesiredOpponentIdentifier() != null && receivedRequest.getClientIdentifier().equals(comparedRequest.getDesiredOpponentIdentifier())) {
                                        FOKLogger.info(ServerMain.class.getName(), "Found matching request!");
                                        matchingConnection = entry.getKey();
                                        matchingOpponentIdentifier = comparedRequest.getClientIdentifier();
                                        break;
                                    }
                                }
                            }

                            if (matchingConnection != null) {
                                openRequests.remove(matchingConnection);
                                // send a response to the client that just requested an opponent
                                response = new OnlineMultiplayerRequestOpponentResponse(ResponseCode.OpponentFound, matchingOpponentIdentifier);
                                connectionMap.put(connection, matchingConnection);
                                connectionMap.put(matchingConnection, connection);

                                // send request to the opponent too
                                matchingConnection.sendTCP(new OnlineMultiplayerRequestOpponentResponse(ResponseCode.OpponentFound, receivedRequest.getClientIdentifier()));
                            } else {
                                // add the request to the openRequests-map
                                FOKLogger.info(ServerMain.class.getName(), "No matching request found, adding the request to the open requests list...");
                                response = new OnlineMultiplayerRequestOpponentResponse(ResponseCode.WaitForOpponent);

                                if (!openRequests.containsKey(connection)) {
                                    // First request through this connection
                                    List<OnlineMultiplayerRequestOpponentRequest> tempRequestList = new ArrayList<>();
                                    tempRequestList.add(receivedRequest);
                                    openRequests.put(connection, tempRequestList);
                                } else {
                                    // client has already sent a request
                                    List<OnlineMultiplayerRequestOpponentRequest> clientRequestList = openRequests.get(connection);
                                    if (!clientRequestList.contains(receivedRequest)) {
                                        // request was only sent once so add it to the list
                                        clientRequestList.add(receivedRequest);
                                    } else {
                                        // request was sent twice so send a exception
                                        FOKLogger.severe(ServerMain.class.getName(), "The identical request was already sent once, cannot add it to the list again!");
                                        OnlineMultiplayerRequestOpponentException exception = new OnlineMultiplayerRequestOpponentException("Requests may not be sent twice");
                                        connection.sendTCP(exception);
                                        return;
                                    }
                                }
                            }
                        } else {
                            // operation is AbortRequest
                            List<OnlineMultiplayerRequestOpponentRequest> clientRequestList = openRequests.get(connection);
                            if (clientRequestList != null && clientRequestList.contains(receivedRequest)) {
                                clientRequestList.remove(receivedRequest);
                                if (clientRequestList.size() == 0) {
                                    // delete the inet address from the map
                                    FOKLogger.info(ServerMain.class.getName(), "Matching request found, aborting request...");
                                    openRequests.remove(connection);
                                }
                                response = new OnlineMultiplayerRequestOpponentResponse(ResponseCode.RequestAborted);
                            } else {
                                FOKLogger.severe(ServerMain.class.getName(), "Could not abort request, no matching request found!");
                                OnlineMultiplayerRequestOpponentException exception = new OnlineMultiplayerRequestOpponentException("No matching request found.");
                                connection.sendTCP(exception);
                                return;
                            }
                        }

                        FOKLogger.info(ServerMain.class.getName(), "Sending response to client...");
                        connection.sendTCP(response);
                    } else if (object instanceof Move || object instanceof CancelGameRequest || object instanceof CancelGameResponse) {
                        // forward the move to the right client
                        Connection matchingConnection = connectionMap.get(connection);
                        if (matchingConnection == null) {
                            connection.sendTCP(new GameException("No opponent found for this connection"));
                        } else {
                            matchingConnection.sendTCP(object);
                            if (object instanceof CancelGameRequest) {
                                connectionMap.remove(connection);
                                connectionMap.remove(matchingConnection);
                            }
                        }
                    }
                } catch (Exception e) {
                    FOKLogger.log(ServerMain.class.getName(), Level.SEVERE, "A internal server error occurred", e);
                    OnlineMultiplayerRequestOpponentException e2 = new OnlineMultiplayerRequestOpponentException(e.getClass().getName() + ", " + e.getMessage());
                    connection.sendTCP(e2);
                }
            }
        });
    }

    /**
     * Shuts the server down
     */
    public static void shutDown() {
        FOKLogger.info(ServerMain.class.getName(), "Shutting server down...");
        server.close();
    }

    /**
     * Clears the internal server memory (like a restart) but without a actual restart
     */
    public static void resetServer() {
        openRequests = new HashMap<>();
    }

    @SuppressWarnings("unused")
    public static int getCurrentTcpPort() {
        return currentTcpPort;
    }
}
