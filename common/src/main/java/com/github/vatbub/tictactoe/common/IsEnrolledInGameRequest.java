package com.github.vatbub.tictactoe.common;

public class IsEnrolledInGameRequest extends Request {
    public IsEnrolledInGameRequest(String connectionId) {
        super(connectionId, IsEnrolledInGameRequest.class.getCanonicalName());
    }
}
