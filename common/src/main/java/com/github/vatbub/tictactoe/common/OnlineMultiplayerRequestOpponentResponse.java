package com.github.vatbub.tictactoe.common;

import java.net.InetSocketAddress;

/**
 * Created by Frederik on 27/05/2017.
 */
public class OnlineMultiplayerRequestOpponentResponse {
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

    public enum ResponseCode {
        OpponentFound, WaitForOpponent, RequestAborted
    }
}
