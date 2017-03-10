package com.github.vatbub.tictactoe;

import org.jetbrains.annotations.Nullable;

/**
 * A cell on a game {@link Board}
 */
public class Cell {
    private Player currentPlayer;

    public Cell(){
        this(null);
    }

    public Cell(@Nullable Player currentPlayer){
        this.currentPlayer = currentPlayer;
    }

    /**
     * Returns the {@link Player} that currently occupies this cell or {@code null} if the cell is free.
     * @return The {@link Player} that currently occupies this cell or {@code null} if the cell is free.
     */
    @Nullable
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Set the {@link Player} that currently occupies this cell. Set it to {@code null} to free this cell.
     * @param currentPlayer The {@link Player} that currently occupies this cell. Set it to {@code null} to free this cell.
     */
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
