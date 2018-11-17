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

import java.util.List;

public class GetGameDataResponse extends Response {
    private List<Move> moves;
    private boolean gameCancelled;
    private String cancelReason;

    public GetGameDataResponse(String connectionId) {
        this(connectionId, null, false);
    }

    public GetGameDataResponse(String connectionId, List<Move> moves, boolean gameCancelled) {
        super(connectionId, GetGameDataResponse.class.getCanonicalName());
        setMoves(moves);
        setGameCancelled(gameCancelled);
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }

    public boolean isGameCancelled() {
        return gameCancelled;
    }

    public void setGameCancelled(boolean gameCancelled) {
        this.gameCancelled = gameCancelled;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
