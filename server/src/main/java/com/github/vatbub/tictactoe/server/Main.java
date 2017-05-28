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
import com.github.vatbub.tictactoe.common.*;
import common.Common;
import logging.FOKLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Launches the server
 */
@SuppressWarnings("WeakerAccess")
public class Main {
    private static Map<InetSocketAddress, List<OnlineMultiplayerRequestOpponentRequest>> openRequests = new HashMap<>();
    private static Server server = new Server();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            startServer(Integer.parseInt(System.getenv("PORT")));
        } else if (args.length == 1) {
            startServer(Integer.parseInt(args[0]));
        } else {
            System.out.println("Too many arguments. The first argument must be the tcp port to run the server on. All other arguments are ignored. If no port is specified, the value will be taken from the PORT environment variable.");
            startServer(Integer.parseInt(args[0]));
        }
    }

    public static void startServer(int tcpPort) throws IOException {
        Common.setAppName("tictactoeserver");
        server.getKryo().setReferences(true);
        KryoCommon.registerRequiredClasses(server.getKryo());
        server.start();
        FOKLogger.info(Main.class.getName(), "Binding to tcpPort " + tcpPort);
        server.bind(tcpPort);
        server.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                try {
                    if (object instanceof OnlineMultiplayerRequestOpponentRequest) {
                        OnlineMultiplayerRequestOpponentRequest receivedRequest = (OnlineMultiplayerRequestOpponentRequest) object;

                        OnlineMultiplayerRequestOpponentResponse response;

                        if (receivedRequest.getOperation().equals(Operation.RequestOpponent)) {
                        /*
                        If a client does not specify a desiredOpponent, the desiredOpponentIdentifier will be null.
                        In that case, we will only give him another opponent who did not specify a desiredOpponent neither,
                        thus, they will both have specified the same desiredOpponentIdentifier = null
                         */

                            // check if any of the open requests has a matching desiredOpponentIdentifier
                            InetSocketAddress matchingAddress = null;
                            for (Map.Entry<InetSocketAddress, List<OnlineMultiplayerRequestOpponentRequest>> entry : openRequests.entrySet()) {
                                for (OnlineMultiplayerRequestOpponentRequest comparedRequest : entry.getValue()) {
                                    if ((receivedRequest.getClientIdentifier() == null && comparedRequest.getDesiredOpponentIdentifier() == null) || (receivedRequest.getClientIdentifier().equals(comparedRequest.getDesiredOpponentIdentifier()))) {
                                        matchingAddress = entry.getKey();
                                        break;
                                    }
                                }
                            }

                            if (matchingAddress != null) {
                                // send a response to the client that just requested an opponent
                                response = new OnlineMultiplayerRequestOpponentResponse(ResponseCode.OpponentFound, matchingAddress);
                            } else {
                                // add the request to the openRequests-map
                                response = new OnlineMultiplayerRequestOpponentResponse(ResponseCode.WaitForOpponent);

                                if (!openRequests.containsKey(connection.getRemoteAddressTCP())) {
                                    // First request of the client
                                    List<OnlineMultiplayerRequestOpponentRequest> tempRequestList = new ArrayList<>();
                                    tempRequestList.add(receivedRequest);
                                    openRequests.put(connection.getRemoteAddressTCP(), tempRequestList);
                                } else {
                                    // client has already sent a request
                                    List<OnlineMultiplayerRequestOpponentRequest> clientRequestList = openRequests.get(connection.getRemoteAddressTCP());
                                    if (!clientRequestList.contains(receivedRequest)) {
                                        // request was only sent once so add it to the list
                                        clientRequestList.add(receivedRequest);
                                    } else {
                                        // request was sent twice so send a exception
                                        OnlineMultiplayerRequestOpponentException exception = new OnlineMultiplayerRequestOpponentException("Requests may not be sent twice");
                                        connection.sendTCP(exception);
                                        return;
                                    }
                                }
                            }
                        } else {
                            // operation is AbortRequest
                            List<OnlineMultiplayerRequestOpponentRequest> clientRequestList = openRequests.get(connection.getRemoteAddressTCP());
                            if (clientRequestList.contains(receivedRequest)) {
                                clientRequestList.remove(receivedRequest);
                                if (clientRequestList.size() == 0) {
                                    // delete the inet address from the map
                                    openRequests.remove(connection.getRemoteAddressTCP());
                                }
                                response = new OnlineMultiplayerRequestOpponentResponse(ResponseCode.RequestAborted);
                            } else {
                                OnlineMultiplayerRequestOpponentException exception = new OnlineMultiplayerRequestOpponentException("No matching request found.");
                                connection.sendTCP(exception);
                                return;
                            }
                        }

                        connection.sendTCP(response);
                    }
                } catch (Exception e) {
                    OnlineMultiplayerRequestOpponentException e2 = new OnlineMultiplayerRequestOpponentException(e.getClass().getName() + ", " + e.getMessage());
                    connection.sendTCP(e2);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Shuts the server down
     */
    public static void shutDown() {
        server.close();
    }
}
