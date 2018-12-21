package com.github.vatbub.tictactoe.common;

/*-
 * #%L
 * tictactoe.common
 * %%
 * Copyright (C) 2016 - 2018 Frederik Kammel
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

import org.jetbrains.annotations.NotNull;

public class ServerInteractionImpl implements ServerInteraction {
    private String className;
    private String connectionId;
    public static final String PROTOCOL_VERSION = "2.0";

    public ServerInteractionImpl() {
        this(null, null);
    }

    public ServerInteractionImpl(String connectionId, String className) {
        setConnectionId(connectionId);
        this.className = className;
    }

    @NotNull
    @Override
    public String getProtocolVersion() {
        return PROTOCOL_VERSION;
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public String getClassName() {
        return className;
    }
}
