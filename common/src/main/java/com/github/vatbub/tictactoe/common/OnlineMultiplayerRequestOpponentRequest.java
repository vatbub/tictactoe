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


import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import java.io.Serializable;

/**
 * Request sent bz the client to specify that it seeks a opponent
 */
@SuppressWarnings("WeakerAccess")
public class OnlineMultiplayerRequestOpponentRequest implements Serializable{
    private final String salt = generateSalt();
    private String clientIdentifier;
    private String desiredOpponentIdentifier;
    private int operation;

    /**
     * For Kryo only
     *
     * @deprecated
     */
    /*public OnlineMultiplayerRequestOpponentRequest() {

    }

    public OnlineMultiplayerRequestOpponentRequest(String clientIdentifier) {
        this(clientIdentifier, "");
    }

    public OnlineMultiplayerRequestOpponentRequest(String clientIdentifier, String desiredOpponentIdentifier) {
        this(clientIdentifier, desiredOpponentIdentifier, Operation.RequestOpponent);
    }

    public OnlineMultiplayerRequestOpponentRequest(String clientIdentifier, String desiredOpponentIdentifier, Operation operation) {
        setClientIdentifier(clientIdentifier);
        setDesiredOpponentIdentifier(desiredOpponentIdentifier);
        setOperation(operation);
    }*/

    private static String generateSalt() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        int length = 8;
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < length; i++) {
            res.append(chars.charAt((int) Math.round(Math.random() * (chars.length() - 1))));
        }

        return res.toString();
    }

    public Operation getOperation() {
        return Operation.values()[operation];
    }

    public void setOperation(Operation operation) {
        this.operation = operation.ordinal();
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public String getDesiredOpponentIdentifier() {
        return desiredOpponentIdentifier;
    }

    public void setDesiredOpponentIdentifier(String desiredOpponentIdentifier) {
        this.desiredOpponentIdentifier = desiredOpponentIdentifier;
    }

    public String getSalt() {
        return salt;
    }

    public String getRequestId() {
        return Hashing.md5().newHasher().putString(getClientIdentifier() + getDesiredOpponentIdentifier() + getSalt(), Charsets.UTF_8).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof OnlineMultiplayerRequestOpponentRequest)) {
            return false;
        } else {
            OnlineMultiplayerRequestOpponentRequest castObj = (OnlineMultiplayerRequestOpponentRequest) obj;
            return getRequestId().equals(castObj.getRequestId());
        }
    }

    // @SuppressWarnings("MethodDoesntCallSuperMethod")
    /*public OnlineMultiplayerRequestOpponentRequest clone() {
        return new OnlineMultiplayerRequestOpponentRequest(getClientIdentifier(), getDesiredOpponentIdentifier());
    }*/
}
