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
import com.github.vatbub.common.core.Common;
import com.github.vatbub.common.core.logging.FOKLogger;
import com.github.vatbub.tictactoe.common.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests the server
 */
@SuppressWarnings("Duplicates")
public class ServerMainTest {
    /**
     * The port to be used to test the server
     */
    private static int port;
    private final String identifierPrefix = "testuser";
    private final String identifier1 = identifierPrefix + Math.round(Math.random() * 1000);
    private final String identifier2 = identifierPrefix + Math.round(Math.random() * 1000);

    private OnlineMultiplayerRequestOpponentRequest request1;

    private OnlineMultiplayerRequestOpponentRequest request2;

    private Client client;

    private Thread shutDownThread;
    private boolean shutServerDown;
    private Throwable throwable;

    @BeforeClass
    public static void doYourOneTimeSetup() throws InterruptedException, IOException {
        Common.setAppName("TicTacToeServerTests");
        Thread.sleep(2000);
        FOKLogger.info(ServerMainTest.class.getName(), "Launching server...");
        launchServer();
        Thread.sleep(5000);
    }

    @AfterClass
    public static void doYourOneTimeTeardown() {
        FOKLogger.info(ServerMainTest.class.getName(), "Shutting server down...");
        ServerMain.shutDown();
    }

    /**
     * Launches an instance of the relay server
     *
     * @throws IOException
     */
    private static void launchServer() throws IOException {
        // launch a server
        if (System.getenv("PORT") == null) {
            // no port defined, launch on port 90
            port = 1025;
        } else {
            // use port in env var
            port = Integer.parseInt(System.getenv("PORT"));
        }
        ServerMain.startServer(port);
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        ServerMain.resetServer();
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
            FOKLogger.info(ServerMainTest.class.getName(), "Sending request without desiredOpponent...");
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

        FOKLogger.info(ServerMainTest.class.getName(), "Checking for response code WaitForOpponent...");
        assert response.getResponseCode().equals(ResponseCode.WaitForOpponent);
        FOKLogger.info(ServerMainTest.class.getName(), "Passed!");
    }

    private void assertOpponentFound(Object object) {
        assert object instanceof OnlineMultiplayerRequestOpponentResponse;
        OnlineMultiplayerRequestOpponentResponse response = (OnlineMultiplayerRequestOpponentResponse) object;

        FOKLogger.info(ServerMainTest.class.getName(), "Checking for response code OpponentFound...");
        assert response.getResponseCode().equals(ResponseCode.OpponentFound);
        FOKLogger.info(ServerMainTest.class.getName(), "Passed!");
    }

    private void assertException(Object object, String expectedErrorMessage) {
        assert object instanceof OnlineMultiplayerRequestOpponentException;
        OnlineMultiplayerRequestOpponentException response = (OnlineMultiplayerRequestOpponentException) object;

        if (expectedErrorMessage != null) {
            FOKLogger.info(ServerMainTest.class.getName(), "Checking for the expected error message...");
            assert response.getMessage().equals(expectedErrorMessage);
            FOKLogger.info(ServerMainTest.class.getName(), "Passed!");
        }
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
            FOKLogger.info(ServerMainTest.class.getName(), "Sending request without desiredOpponent...");
            client.sendTCP(request1);

            FOKLogger.info(ServerMainTest.class.getName(), "Sending second request without desiredOpponent...");
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
            FOKLogger.info(ServerMainTest.class.getName(), "Sending request with desiredOpponent...");
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
    public void multipleRequestsFromSameClient() throws Throwable {
        final int[] responsesReceivedCount = {0};

        client.addListener(new Listener() {
            @SuppressWarnings("Duplicates")
            @Override
            public void received(Connection connection, Object object) {
                FOKLogger.info(ServerMainTest.class.getName(), "Requests received: " + responsesReceivedCount[0] + 1);
                if (responsesReceivedCount[0] < 2) {
                    try {
                        responsesReceivedCount[0]++;
                        assertWaitForOpponent(object);
                    } catch (Error | Exception e) {
                        throwable = e;
                        tearDown();
                    }
                } else {
                    try {
                        assertWaitForOpponent(object);
                        tearDown();
                    } catch (Error | Exception e) {
                        throwable = e;
                        tearDown();
                    }
                }
            }
        });

        try {
            FOKLogger.info(ServerMainTest.class.getName(), "Sending request with desiredOpponent...");
            request1.setDesiredOpponentIdentifier(identifier2);
            client.sendTCP(request1);

            FOKLogger.info(ServerMainTest.class.getName(), "Sending second request with desiredOpponent...");
            client.sendTCP(request1.clone());

            FOKLogger.info(ServerMainTest.class.getName(), "Sending third request with desiredOpponent...");
            client.sendTCP(request1.clone());
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
            FOKLogger.info(ServerMainTest.class.getName(), "Sending request with desiredOpponent...");
            request1.setDesiredOpponentIdentifier(identifier2);
            client.sendTCP(request1);

            FOKLogger.info(ServerMainTest.class.getName(), "Sending second request with desiredOpponent...");
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
    public void sendSameRequestTwice() throws Throwable {
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
                        assertException(object, "Requests may not be sent twice");
                        tearDown();
                    } catch (Error | Exception e) {
                        throwable = e;
                        tearDown();
                    }
                }
            }
        });

        try {
            FOKLogger.info(ServerMainTest.class.getName(), "Sending request with desiredOpponent...");
            request1.setDesiredOpponentIdentifier(identifier2);
            client.sendTCP(request1);

            FOKLogger.info(ServerMainTest.class.getName(), "Sending same request again...");
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

                        FOKLogger.info(ServerMainTest.class.getName(), "Checking for response code RequestAborted...");
                        assert response.getResponseCode().equals(ResponseCode.RequestAborted);
                        FOKLogger.info(ServerMainTest.class.getName(), "Passed!");
                        tearDown();
                    } catch (Error | Exception e) {
                        throwable = e;
                        tearDown();
                    }
                }
            }
        });

        try {
            FOKLogger.info(ServerMainTest.class.getName(), "Sending request without desiredOpponent...");
            client.sendTCP(request1);

            FOKLogger.info(ServerMainTest.class.getName(), "Sending abortion request...");
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

    @Test
    public void abortRequestThatWasNeverSent() throws Throwable {
        client.addListener(new Listener() {
            @SuppressWarnings("Duplicates")
            @Override
            public void received(Connection connection, Object object) {
                try {
                    assertException(object, "No matching request found.");
                    tearDown();
                } catch (Error | Exception e) {
                    throwable = e;
                    tearDown();
                }
            }
        });

        try {
            FOKLogger.info(ServerMainTest.class.getName(), "Sending abortion request...");
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

    /**
     * Prepares the requests to make them ready to be sent
     */
    private void setupRequests() {
        request1 = new OnlineMultiplayerRequestOpponentRequest();
        request2 = new OnlineMultiplayerRequestOpponentRequest();
        request1.setClientIdentifier(identifier1);
        request2.setClientIdentifier(identifier2);
    }

    /**
     * Sets the relay client up
     *
     * @throws IOException If the client cannot connect for any reason
     */
    private void setupClient() throws IOException {
        client = new Client();
        KryoCommon.registerRequiredClasses(client.getKryo());
        client.getKryo().setReferences(true);
        client.start();

        connect();
    }

    /**
     * Connects the relay client
     *
     * @throws IOException If the client cannot connect for any reason
     */
    private void connect() throws IOException {
        client.connect(100000, "localhost", port);
    }

    private void tearDown() {
        FOKLogger.info(ServerMainTest.class.getName(), "Resetting server...");
        shutServerDown = true;
    }
}
