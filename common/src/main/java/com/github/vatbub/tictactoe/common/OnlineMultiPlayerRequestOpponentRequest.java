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

/**
 * Request sent bz the client to specify that it seeks a opponent
 */
@SuppressWarnings("WeakerAccess")
public class OnlineMultiPlayerRequestOpponentRequest extends Request {
    private final String salt = generateSalt();
    private String clientIdentifier;
    private String desiredOpponentIdentifier;
    private int operation;

    public OnlineMultiPlayerRequestOpponentRequest(String connectionId) {
        super(connectionId, OnlineMultiPlayerRequestOpponentRequest.class.getCanonicalName());
    }

    /**
     * Generates a random string with a length of 8 characters.
     *
     * @return A random string with a length of 8 characters.
     */
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
        return Hashing.md5().newHasher().putString(getClientIdentifier() + getDesiredOpponentIdentifier() + getSalt(), Charsets.UTF_8).hash().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OnlineMultiPlayerRequestOpponentRequest)) {
            return false;
        } else {
            OnlineMultiPlayerRequestOpponentRequest castObj = (OnlineMultiPlayerRequestOpponentRequest) obj;
            return getRequestId().equals(castObj.getRequestId());
        }
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public OnlineMultiPlayerRequestOpponentRequest clone() {
        OnlineMultiPlayerRequestOpponentRequest res = new OnlineMultiPlayerRequestOpponentRequest(this.getConnectionId());
        res.setOperation(this.getOperation());
        res.setDesiredOpponentIdentifier(this.getDesiredOpponentIdentifier());
        res.setClientIdentifier(this.getClientIdentifier());
        return res;
    }
}
