package com.github.vatbub.tictactoe.common;

/*-
 * #%L
 * tictactoe.common
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


import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Created by Frederik on 27/05/2017.
 */
@SuppressWarnings("WeakerAccess")
public class OnlineMultiplayerRequestOpponentResponse implements Serializable{
    private ResponseCode responseCode;
    private InetSocketAddress opponentInetSocketAddress;
    public OnlineMultiplayerRequestOpponentResponse(ResponseCode responseCode) {
        this(responseCode, null);
        if (responseCode.equals(ResponseCode.OpponentFound)) {
            throw new IllegalArgumentException("ResponseCode.OpponentFound requires a opponentInetSocketAddress to be specified!");
        }
    }

    public OnlineMultiplayerRequestOpponentResponse(ResponseCode responseCode, InetSocketAddress opponentInetSocketAddress) {
        setResponseCode(responseCode);
        setOpponentInetSocketAddress(opponentInetSocketAddress);
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public InetSocketAddress getOpponentInetSocketAddress() {
        return opponentInetSocketAddress;
    }

    public void setOpponentInetSocketAddress(InetSocketAddress opponentInetSocketAddress) {
        this.opponentInetSocketAddress = opponentInetSocketAddress;
    }
}