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
import com.github.vatbub.tictactoe.common.KryoCommon;
import com.github.vatbub.tictactoe.common.OnlineMultiplayerRequestOpponentResponse;
import common.internet.Internet;
import logging.FOKLogger;

import java.io.IOException;
import java.net.URL;

/**
 * Does all the networking tasks for the online multiplayer game.
 */
public class KryoGameConnections {
    private static Client kryoClient;
    private static Server kryoServer;

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
        connect("52.59.117.143", onConnected);
    }

    public static void connect(String host, Runnable onConnected) throws IOException {
        connect(host, 90, onConnected);
    }

    public static void connect(String host, int tcpPort, Runnable onConnected) throws IOException {
        if (kryoClient != null && kryoClient.isConnected()) {
            throw new IllegalStateException("Already connected");
        }

        if (kryoClient == null) {
            kryoClient = new Client();
        }

        // ping the host to wake him up (e. g. om heroku)
        String pingAddress = "http://" + host;
        FOKLogger.info(KryoGameConnections.class.getName(), "Pinging the host " + pingAddress + " to wake him up...");
        try {
            String pingResponse = Internet.webread(new URL(pingAddress));
            FOKLogger.info(KryoGameConnections.class.getName(), "Ping response: " + pingResponse);
        } catch (Exception e) {
            FOKLogger.severe(KryoGameConnections.class.getName(), "Ping failed. Still trying to connect using KryoNet, reason for the failed ping: " + e.getLocalizedMessage());
        }

        kryoClient.start();
        KryoCommon.registerRequiredClasses(kryoClient.getKryo());
        kryoClient.getKryo().setReferences(true);
        //registerGameClasses(kryoClient.getKryo());
        kryoClient.setKeepAliveTCP(2500);

        kryoClient.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof OnlineMultiplayerRequestOpponentResponse) {
                    OnlineMultiplayerRequestOpponentResponse response = (OnlineMultiplayerRequestOpponentResponse) object;
                    FOKLogger.info(KryoGameConnections.class.getName(), "Received OnlineMultiplayerRequestOpponentResponse");
                    FOKLogger.info(KryoGameConnections.class.getName(), "Response code: " + response.getResponseCode());
                    FOKLogger.info(KryoGameConnections.class.getName(), "Opponents inet address");
                } else if (object instanceof FrameworkMessage.KeepAlive) {
                    FOKLogger.info(KryoGameConnections.class.getName(), "Received keepAlive message from server");
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

    private static void registerGameClasses(Kryo kryo) {
        kryo.register(Board.Move.class, new JavaSerializer());
    }
}
