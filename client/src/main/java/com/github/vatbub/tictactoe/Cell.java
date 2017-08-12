package com.github.vatbub.tictactoe;

/*-
 * #%L
 * tictactoe
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


import org.jetbrains.annotations.Nullable;

/**
 * A cell on a game {@link Board}
 */
@SuppressWarnings("WeakerAccess")
public class Cell {
    private Player currentPlayer;

    public Cell() {
        this(null);
    }

    public Cell(@SuppressWarnings("SameParameterValue") @Nullable Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /**
     * Returns the {@link Player} that currently occupies this cell or {@code null} if the cell is free.
     *
     * @return The {@link Player} that currently occupies this cell or {@code null} if the cell is free.
     */
    @Nullable
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Set the {@link Player} that currently occupies this cell. Set it to {@code null} to free this cell.
     *
     * @param currentPlayer The {@link Player} that currently occupies this cell. Set it to {@code null} to free this cell.
     */
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
