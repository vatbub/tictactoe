package com.github.vatbub.tictactoe.common;

/*-
 * #%L
 * tictactoe.client
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

/**
 * Sent when one of the online players wishes to cancel the current game.
 *
 * @see CancelGameResponse
 */
@SuppressWarnings("WeakerAccess")
public class CancelGameRequest implements Serializable {
    private String reason;

    public CancelGameRequest() {
        this(null);
    }

    public CancelGameRequest(String reason) {
        setReason(reason);
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
