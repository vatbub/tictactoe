package com.github.vatbub.tictactoe.server;

/*-
 * #%L
 * tictactoe-server-updater
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
import com.github.vatbub.tictactoe.common.KryoCommon;
import com.github.vatbub.tictactoe.common.UpdateServerRequest;
import com.github.vatbub.tictactoe.common.UpdateServerResponse;
import common.Common;
import de.taimos.totp.TOTP;
import logging.FOKLogger;

import java.util.logging.Level;

/**
 * Created by Frederik on 31/05/2017.
 */
@SuppressWarnings("WeakerAccess")
public class UpdaterMain {
    public static void main(String[] args) {
        try {
            Common.setAppName("tictactoeserverupdater");
            @SuppressWarnings("unused") String key = System.getenv("updateServerTOTPKey");

            Client client = new Client();
            KryoCommon.registerRequiredClasses(client.getKryo());
            client.getKryo().setReferences(true);
            client.start();
            KryoCommon.registerRequiredClasses(client.getKryo());

            client.addListener(new Listener(){
                @Override
                public void received(Connection connection, Object object) {
                    if (object instanceof UpdateServerResponse){
                        UpdateServerResponse response = (UpdateServerResponse) object;
                        FOKLogger.info(UpdaterMain.class.getName(), "Received response from server: " + response.getResponseText());
                        client.close();
                    }
                }
            });

            client.connect(100000, args[0], Integer.parseInt(args[1]));

            UpdateServerRequest request = new UpdateServerRequest();
            request.setTotpPassword(TOTP.getOTP("123456789"));
            client.sendTCP(request);
        } catch (Exception e) {
            printHelpMessage();
            FOKLogger.log(UpdaterMain.class.getName(), Level.SEVERE, "An error occurred.", e);
        }
    }

    private static void printHelpMessage() {
        FOKLogger.info(UpdaterMain.class.getName(), "TicTacToe Server Updater");
        FOKLogger.info(UpdaterMain.class.getName(), "Sends a request to the specified server to update itself");
        FOKLogger.info(UpdaterMain.class.getName(), "Required arguments:");
        FOKLogger.info(UpdaterMain.class.getName(), "serverIp: The ip or host name of the server to update");
        FOKLogger.info(UpdaterMain.class.getName(), "typPort: The tcp port on which the server is listening");
    }
}
