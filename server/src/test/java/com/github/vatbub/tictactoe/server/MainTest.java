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


import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.github.vatbub.tictactoe.common.*;
import common.Common;
import logging.FOKLogger;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests the server
 */
public class MainTest {
    @Test
    public void serverTest() throws IOException {
        Common.setAppName("TicTacToeServerTests");
        int port;

        // launch a server
        if (System.getenv("PORT") == null) {
            // no port defined, launch on port 90
            port = 90;
        } else {
            // use port in env var
            port = Integer.parseInt(System.getenv("PORT"));
        }
        //Main.startServer(port);

        // do testing
        Client client = new Client();
        KryoCommon.registerRequiredClasses(client.getKryo());
        client.getKryo().setReferences(true);
        client.start();

        String identifierPrefix = "testuser";
        String identifier1 = identifierPrefix + Math.round(Math.random() * 1000);
        String identifier2 = identifierPrefix + Math.round(Math.random() * 1000);

        OnlineMultiplayerRequestOpponentRequest request1 = new OnlineMultiplayerRequestOpponentRequest();
        request1.setClientIdentifier(identifier1);
        OnlineMultiplayerRequestOpponentRequest request2 = new OnlineMultiplayerRequestOpponentRequest();
        request2.setClientIdentifier(identifier2);

        client.addListener(new Listener(){
            @Override
            public void received(Connection connection, Object object) {
                assert object instanceof OnlineMultiplayerRequestOpponentResponse;
                OnlineMultiplayerRequestOpponentResponse response = (OnlineMultiplayerRequestOpponentResponse) object;

                FOKLogger.info(MainTest.class.getName(), "Checking for response code WaitForOpponent...");
                assert response.getResponseCode().equals(ResponseCode.WaitForOpponent);
                assert response.getOpponentInetSocketAddress()==null;
                FOKLogger.info(MainTest.class.getName(), "Passed!");

                client.removeListener(this);
                client.addListener(new Listener(){
                    @Override
                    public void received(Connection connection, Object object) {
                        assert object instanceof OnlineMultiplayerRequestOpponentResponse;
                        OnlineMultiplayerRequestOpponentResponse response = (OnlineMultiplayerRequestOpponentResponse) object;

                        FOKLogger.info(MainTest.class.getName(), "Checking for response code OpponentFound...");
                        assert response.getResponseCode().equals(ResponseCode.OpponentFound);
                        FOKLogger.info(MainTest.class.getName(), "Passed!");
                        FOKLogger.info(MainTest.class.getName(), "Checking for a opponent ip...");
                        assert response.getOpponentInetSocketAddress()!=null;
                        FOKLogger.info(MainTest.class.getName(), "Passed!");
                        client.removeListener(this);
                        client.addListener(new Listener(){
                            @Override
                            public void received(Connection connection, Object object) {
                                assert object instanceof OnlineMultiplayerRequestOpponentResponse;
                                OnlineMultiplayerRequestOpponentResponse response = (OnlineMultiplayerRequestOpponentResponse) object;

                                FOKLogger.info(MainTest.class.getName(), "Checking for response code WaitForOpponent...");
                                assert response.getResponseCode().equals(ResponseCode.WaitForOpponent);
                                assert response.getOpponentInetSocketAddress()==null;
                                FOKLogger.info(MainTest.class.getName(), "Passed!");

                                client.removeListener(this);
                                client.addListener(new Listener(){
                                    @Override
                                    public void received(Connection connection, Object object) {
                                        assert object instanceof OnlineMultiplayerRequestOpponentResponse;
                                        OnlineMultiplayerRequestOpponentResponse response = (OnlineMultiplayerRequestOpponentResponse) object;

                                        FOKLogger.info(MainTest.class.getName(), "Checking for response code RequestAborted...");
                                        assert response.getResponseCode().equals(ResponseCode.RequestAborted);
                                        FOKLogger.info(MainTest.class.getName(), "Passed!");
                                        assert response.getOpponentInetSocketAddress()==null;

                                        // shut server down
                                        Main.shutDown();
                                        client.close();
                                    }
                                });
                                FOKLogger.info(MainTest.class.getName(), "Sending abortion request...");
                                request1.setOperation(Operation.AbortRequest);
                                client.sendTCP(request1);
                            }
                        });
                        // abort
                        FOKLogger.info(MainTest.class.getName(), "Sending request without desiredOpponent to be aborted...");
                        client.sendTCP(request1);
                    }
                });
                // with desired opponent
                FOKLogger.info(MainTest.class.getName(), "Sending request with desiredOpponent...");
                client.sendTCP(request2);
            }
        });

        //client.getKryo().setReferences(true);
        client.connect(5000, "localhost", port);
        client.setKeepAliveTCP(250);

        // no desired opponent
        FOKLogger.info(MainTest.class.getName(), "Sending request without desiredOpponent...");
        client.sendTCP(request1);
    }
}
