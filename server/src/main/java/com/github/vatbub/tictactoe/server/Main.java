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
import com.github.vatbub.tictactoe.common.KryoCommon;
import com.github.vatbub.tictactoe.common.OnlineMultiplayerRequest;
import com.github.vatbub.tictactoe.common.OnlineMultiplayerResponse;
import common.Common;
import logging.FOKLogger;

import java.io.IOException;

/**
 * Launches the server
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Common.setAppName("tictactoeserver");
        Server server = new Server();
        server.start();
        FOKLogger.info(Main.class.getName(), "Binding to tcpPort " + System.getenv("PORT"));
        server.bind(Integer.parseInt(System.getenv("PORT")));
        KryoCommon.registerRequiredClasses(server.getKryo());
        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof OnlineMultiplayerRequest) {
                    OnlineMultiplayerRequest request = (OnlineMultiplayerRequest)object;
                    System.out.println(request.text);

                    OnlineMultiplayerResponse response = new OnlineMultiplayerResponse();
                    response.text = "Thanks";
                    connection.sendTCP(response);
                }
            }
        });
    }
}
