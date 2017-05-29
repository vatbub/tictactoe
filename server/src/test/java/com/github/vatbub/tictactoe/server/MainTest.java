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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests the server
 */
@SuppressWarnings("Duplicates")
public class MainTest {
    private static int port;
    private String identifierPrefix = "testuser";
    private String identifier1 = identifierPrefix + Math.round(Math.random() * 1000);
    private String identifier2 = identifierPrefix + Math.round(Math.random() * 1000);

    private OnlineMultiplayerRequestOpponentRequest request1;

    private OnlineMultiplayerRequestOpponentRequest request2;

    private Client client;

    private Thread shutDownThread;
    private boolean shutServerDown;
    private Throwable throwable;

    @BeforeClass
    public static void doYourOneTimeSetup() throws InterruptedException, IOException {
        Thread.sleep(2000);
        launchServer();
        Thread.sleep(5000);
    }

    @AfterClass
    public static void doYourOneTimeTeardown() {
        Main.shutDown();
    }

    private static void launchServer() throws IOException {
        Common.setAppName("TicTacToeServerTests");

        // launch a server
        if (System.getenv("PORT") == null) {
            // no port defined, launch on port 90
            port = 1025;
        } else {
            // use port in env var
            port = Integer.parseInt(System.getenv("PORT"));
        }
        Main.startServer(port);
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        Main.resetServer();
        setupClient();
        setupRequests();

        shutDownThread = new Thread(() -> {
            boolean isShutDown = false;
            while (!isShutDown) {
                System.out.print("");
                if (shutServerDown) {
                    try {
                        // shut client down
                        client.close();
                        Thread.sleep(5000);
                        isShutDown = true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        shutDownThread.setName("shutDownThread");
        shutDownThread.start();
    }

    @Test
    public void singleRequestNoDesiredOpponent() throws Throwable {
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                try {
                    assertWaitForOpponent(object);
                    tearDown();
                } catch (Error | Exception e) {
                    throwable = e;
                    tearDown();
                }
            }
        });

        try {
            FOKLogger.info(MainTest.class.getName(), "Sending request without desiredOpponent...");
            client.sendTCP(request1);
        } catch (Error | Exception e) {
            throwable = e;
            tearDown();
        }
        shutDownThread.join();
        if (throwable != null) {
            throw throwable;
        }
    }

    private void assertWaitForOpponent(Object object) {
        assert object instanceof OnlineMultiplayerRequestOpponentResponse;
        OnlineMultiplayerRequestOpponentResponse response = (OnlineMultiplayerRequestOpponentResponse) object;

        FOKLogger.info(MainTest.class.getName(), "Checking for response code WaitForOpponent...");
        assert response.getResponseCode().equals(ResponseCode.WaitForOpponent);
        assert response.getOpponentInetSocketAddress() == null;
        FOKLogger.info(MainTest.class.getName(), "Passed!");
    }

    private void assertOpponentFound(Object object) {
        assert object instanceof OnlineMultiplayerRequestOpponentResponse;
        OnlineMultiplayerRequestOpponentResponse response = (OnlineMultiplayerRequestOpponentResponse) object;

        FOKLogger.info(MainTest.class.getName(), "Checking for response code OpponentFound...");
        assert response.getResponseCode().equals(ResponseCode.OpponentFound);
        FOKLogger.info(MainTest.class.getName(), "Passed!");
        FOKLogger.info(MainTest.class.getName(), "Checking for a opponent ip...");
        assert response.getOpponentInetSocketAddress() != null;
        FOKLogger.info(MainTest.class.getName(), "Passed!");
    }

    @Test
    public void twoRequestsNoDesiredOpponent() throws Throwable {
        final boolean[] firstPassed = {false};

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (!firstPassed[0]) {
                    try {
                        firstPassed[0] = true;
                        assertWaitForOpponent(object);
                    } catch (Error | Exception e) {
                        throwable = e;
                        tearDown();
                    }
                } else {
                    try {
                        assertOpponentFound(object);
                        tearDown();
                    } catch (Error | Exception e) {
                        throwable = e;
                        tearDown();
                    }
                }

            }
        });

        try {
            FOKLogger.info(MainTest.class.getName(), "Sending request without desiredOpponent...");
            client.sendTCP(request1);

            FOKLogger.info(MainTest.class.getName(), "Sending second request without desiredOpponent...");
            client.sendTCP(request2);
        } catch (Error | Exception e) {
            throwable = e;
            tearDown();
        }

        shutDownThread.join();
        if (throwable != null) {
            throw throwable;
        }
    }

    @Test
    public void singleRequestWithDesiredOpponent() throws Throwable {
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                try {
                    assertWaitForOpponent(object);
                    tearDown();
                } catch (Error | Exception e) {
                    throwable = e;
                    tearDown();
                }
            }
        });

        try {
            FOKLogger.info(MainTest.class.getName(), "Sending request with desiredOpponent...");
            request1.setDesiredOpponentIdentifier(identifier2);
            client.sendTCP(request1);
        } catch (Error | Exception e) {
            throwable = e;
            tearDown();
        }

        shutDownThread.join();
        if (throwable != null) {
            throw throwable;
        }
    }

    @Test
    public void twoRequestsWithDesiredOpponent() throws Throwable {
        final boolean[] firstPassed = {false};

        client.addListener(new Listener() {
            @SuppressWarnings("Duplicates")
            @Override
            public void received(Connection connection, Object object) {
                if (!firstPassed[0]) {
                    try {
                        firstPassed[0] = true;
                        assertWaitForOpponent(object);
                    } catch (Error | Exception e) {
                        throwable = e;
                        tearDown();
                    }
                } else {
                    try {
                        assertOpponentFound(object);
                        tearDown();
                    } catch (Error | Exception e) {
                        throwable = e;
                        tearDown();
                    }
                }
            }
        });

        try {
            FOKLogger.info(MainTest.class.getName(), "Sending request with desiredOpponent...");
            request1.setDesiredOpponentIdentifier(identifier2);
            client.sendTCP(request1);

            FOKLogger.info(MainTest.class.getName(), "Sending second request with desiredOpponent...");
            request2.setDesiredOpponentIdentifier(identifier1);
            client.sendTCP(request2);
        } catch (Error | Exception e) {
            throwable = e;
            tearDown();
        }

        shutDownThread.join();
        if (throwable != null) {
            throw throwable;
        }
    }

    @Test
    public void abortedRequestTest() throws Throwable {
        final boolean[] firstPassed = {false};

        client.addListener(new Listener() {
            @SuppressWarnings("Duplicates")
            @Override
            public void received(Connection connection, Object object) {
                if (!firstPassed[0]) {
                    try {
                        firstPassed[0] = true;
                        assertWaitForOpponent(object);
                    } catch (Error | Exception e) {
                        throwable = e;
                        tearDown();
                    }
                } else {
                    try {
                        assert object instanceof OnlineMultiplayerRequestOpponentResponse;
                        OnlineMultiplayerRequestOpponentResponse response = (OnlineMultiplayerRequestOpponentResponse) object;

                        FOKLogger.info(MainTest.class.getName(), "Checking for response code RequestAborted...");
                        assert response.getResponseCode().equals(ResponseCode.RequestAborted);
                        FOKLogger.info(MainTest.class.getName(), "Passed!");
                        assert response.getOpponentInetSocketAddress() == null;
                        tearDown();
                    } catch (Error | Exception e) {
                        throwable = e;
                        tearDown();
                    }
                }
            }
        });

        try {
            FOKLogger.info(MainTest.class.getName(), "Sending request without desiredOpponent...");
            client.sendTCP(request1);

            FOKLogger.info(MainTest.class.getName(), "Sending abortion request...");
            request1.setOperation(Operation.AbortRequest);
            client.sendTCP(request1);
        } catch (Error | Exception e) {
            throwable = e;
            tearDown();
        }

        shutDownThread.join();
        if (throwable != null) {
            throw throwable;
        }
    }

    private void setupRequests() {
        request1 = new OnlineMultiplayerRequestOpponentRequest();
        request2 = new OnlineMultiplayerRequestOpponentRequest();
        request1.setClientIdentifier(identifier1);
        request2.setClientIdentifier(identifier2);
    }

    private void setupClient() throws IOException {
        client = new Client();
        KryoCommon.registerRequiredClasses(client.getKryo());
        client.getKryo().setReferences(true);
        client.start();

        connect();
    }

    private void connect() throws IOException {
        client.connect(100000, "localhost", port);
    }

    private void tearDown() {
        FOKLogger.info(MainTest.class.getName(), "Tearing down...");
        shutServerDown = true;
    }
}
