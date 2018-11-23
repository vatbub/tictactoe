package com.github.vatbub.tictactoe.common;

public class IsEnrolledInGameResponse extends Response {
    private boolean enrolled;

    public IsEnrolledInGameResponse(String connectionId, boolean enrolled) {
        super(connectionId, IsEnrolledInGameResponse.class.getCanonicalName());
        setEnrolled(enrolled);
    }

    public boolean isEnrolled() {
        return enrolled;
    }

    public void setEnrolled(boolean enrolled) {
        this.enrolled = enrolled;
    }
}
