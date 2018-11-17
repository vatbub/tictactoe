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


/**
 * Response to a {@link OnlineMultiPlayerRequestOpponentRequest} that tells the requesting client if it has to wait or not
 * and if not, who the opponent is.
 */
@SuppressWarnings("WeakerAccess")
public class OnlineMultiPlayerRequestOpponentResponse extends Response {
    private ResponseCode responseCode;
    private String opponentIdentifier;
    private boolean hasFirstTurn;

    public OnlineMultiPlayerRequestOpponentResponse(String connectionId, ResponseCode responseCode) {
        this(connectionId, responseCode, null);
    }

    public OnlineMultiPlayerRequestOpponentResponse(String connectionId, ResponseCode responseCode, String opponentIdentifier) {
        super(connectionId, OnlineMultiPlayerRequestOpponentResponse.class.getCanonicalName());
        setResponseCode(responseCode);
        setOpponentIdentifier(opponentIdentifier);
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public String getOpponentIdentifier() {
        return opponentIdentifier;
    }

    public void setOpponentIdentifier(String opponentIdentifier) {
        this.opponentIdentifier = opponentIdentifier;
    }

    public boolean hasFirstTurn() {
        return hasFirstTurn;
    }

    public void setHasFirstTurn(boolean hasFirstTurn) {
        this.hasFirstTurn = hasFirstTurn;
    }
}
