package com.github.vatbub.tictactoe.common;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

/**
 * Request sent bz the client to specify that it seeks a opponent
 */
public class OnlineMultiplayerRequestOpponentRequest {
    private final String salt = random(8);
    private String clientIdentifier;
    private String desiredOpponentIdentifier;
    private Operation operation;

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public OnlineMultiplayerRequestOpponentRequest(String clientIdentifier) {
        this(clientIdentifier, "");
    }

    public OnlineMultiplayerRequestOpponentRequest(String clientIdentifier, String desiredOpponentIdentifier) {
        setClientIdentifier(clientIdentifier);
        setDesiredOpponentIdentifier(desiredOpponentIdentifier);
    }

    private static String random(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < length; i++) {
            res.append(chars.charAt((int) Math.round(Math.random() * (chars.length() - 1))));
        }

        return res.toString();
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

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public OnlineMultiplayerRequestOpponentRequest clone() {
        return new OnlineMultiplayerRequestOpponentRequest(getClientIdentifier(), getDesiredOpponentIdentifier());
    }

    public enum Operation{
        RequestOpponent, AbortRequest
    }
}
