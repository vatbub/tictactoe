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


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A classic tic tac toe board.
 */
@SuppressWarnings("WeakerAccess")
public class Board {
    private static final int gemsInARowToWin = 3;

    private Cell[][] board;
    private int dimRows;
    private int dimCols;
    private Player player1;
    private Player player2;
    private ObjectProperty<Player> currentPlayer = new SimpleObjectProperty<>();
    private ObjectProperty<GameEndRunnable> gameEndCallback = new SimpleObjectProperty<>();

    /**
     * Initializes a new 3*3 game board
     */
    @SuppressWarnings("unused")
    public Board() {
        this(3);
    }

    /**
     * Initializes a new quadratic game board with the given dimension
     *
     * @param quadraticDim The edge length of the board
     */
    public Board(int quadraticDim) {
        this(quadraticDim, quadraticDim);
    }

    /**
     * Initializes a new board with the given dimensions
     *
     * @param dimRows The desired width of the board
     * @param dimCols The desired height of the board.
     */
    public Board(int dimRows, int dimCols) {
        this(dimRows, dimCols, null, null);
    }

    /**
     * Initializes a new board with the given dimensions
     *
     * @param dimRows The desired width of the board
     * @param dimCols The desired height of the board.
     */
    public Board(int dimRows, int dimCols, Player player1, Player player2) {
        this.dimCols = dimCols;
        this.dimRows = dimRows;
        setPlayer1(player1);
        setPlayer2(player2);
        board = new Cell[dimRows][dimCols];
        for (int r = 0; r < dimRows; r++) {
            for (int c = 0; c < dimCols; c++) {
                board[r][c] = new Cell();
            }
        }
    }

    public void setPlayerAt(int row, int col, Player player) {
        board[row][col].setCurrentPlayer(player);
    }

    public Player getPlayerAt(int row, int col) {
        return board[row][col].getCurrentPlayer();
    }

    public int getRowCount() {
        return this.dimRows;
    }

    public int getColumnCount() {
        return this.dimCols;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public Player getCurrentPlayer() {
        return currentPlayer.get();
    }

    public ObjectProperty<Player> currentPlayerProperty() {
        return currentPlayer;
    }

    public void doTurn(int row, int col) {
        if (getCurrentPlayer() == null) {
            currentPlayerProperty().set(getPlayer1());
        }

        if (getPlayerAt(row, col) != null) {
            throw new IllegalStateException("Cell is already taken by a player");
        }

        this.setPlayerAt(row, col, getCurrentPlayer());

        WinnerInfo winnerInfo = getWinner(row, col);
        if (winnerInfo.winningPlayer != null) {
            if (getGameEndCallback() != null) {
                getGameEndCallback().run(winnerInfo);
            }
            return;
        }

        currentPlayerProperty().set(getOpponent(getCurrentPlayer()));

        if (getCurrentPlayer().isAi()) {
            getCurrentPlayer().doAiTurn(this, getOpponent(getCurrentPlayer()));
        }
    }

    /**
     * Returns the opponent of the specified player
     *
     * @param player The player whose opponent needs to be determined. {@code player} must be either Player1 or Player2 on this board, otherwise a {@code IllegalArgumentException} is thrown
     * @return The opponent of the specified player
     */
    public Player getOpponent(Player player) {
        if (player == getPlayer1()) {
            return getPlayer2();
        } else if (player == getPlayer2()) {
            return getPlayer1();
        } else {
            throw new IllegalArgumentException(player.getName() + " is not part of the game and thus cannot have an opponent");
        }
    }

    /**
     * Determines the winner of the game
     *
     * @param startRow The row of the cell where the winner search shall start. This should be the row of the last turn.
     * @param startCol The column of the cell where the winner search shall start. This should be the column of the last turn.
     * @return The winner of the game, {@code Player.TIE_PLAYER} if the game is finished but there is no winner or {@code null} if the game is not finished yet.
     */
    public WinnerInfo getWinner(int startRow, int startCol) {
        int gemsFound = 1;
        int r = startRow;
        int c = startCol;
        WinnerInfo res = new WinnerInfo();
        res.winningPlayer = getPlayerAt(r, c);
        res.winLineStartRow = r;
        res.winLineStartColumn = c;
        res.winLineEndRow = r;
        res.winLineEndColumn = c;

        // go up
        while (gemsFound < gemsInARowToWin && r - 1 >= 0 && r - 1 < getRowCount()) {
            r = r - 1;
            if (getPlayerAt(r, c) == res.winningPlayer) {
                gemsFound = gemsFound + 1;
                res.winLineStartRow = r;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go down
        r = startRow;
        while (gemsFound < gemsInARowToWin && r + 1 >= 0 && r + 1 < getRowCount()) {
            r = r + 1;
            if (getPlayerAt(r, c) == res.winningPlayer) {
                res.winLineEndRow = r;
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go left
        r = startRow;
        res.winLineStartRow = r;
        res.winLineEndRow = r;
        gemsFound = 1;
        while (gemsFound < gemsInARowToWin && c - 1 >= 0 && c - 1 < getColumnCount()) {
            c = c - 1;
            if (getPlayerAt(r, c) == res.winningPlayer) {
                res.winLineStartColumn = c;
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go right
        c = startCol;
        while (gemsFound < gemsInARowToWin && c + 1 >= 0 && c + 1 < getColumnCount()) {
            c = c + 1;
            if (getPlayerAt(r, c) == res.winningPlayer) {
                res.winLineEndColumn = c;
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go up-left
        c = startCol;
        res.winLineStartColumn = c;
        res.winLineEndColumn = c;
        gemsFound = 1;
        while (gemsFound < gemsInARowToWin && c - 1 >= 0 && c - 1 < getColumnCount() && r - 1 >= 0 && r - 1 < getRowCount()) {
            c = c - 1;
            r = r - 1;
            if (getPlayerAt(r, c) == res.winningPlayer) {
                res.winLineStartRow = r;
                res.winLineStartColumn = c;
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go down-right
        c = startCol;
        r = startRow;
        while (gemsFound < gemsInARowToWin && c + 1 >= 0 && c + 1 < getColumnCount() && r + 1 >= 0 && r + 1 < getRowCount()) {
            c = c + 1;
            r = r + 1;
            if (getPlayerAt(r, c) == res.winningPlayer) {
                res.winLineEndRow = r;
                res.winLineEndColumn = c;
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go up-right
        c = startCol;
        r = startRow;
        res.winLineStartColumn = c;
        res.winLineEndColumn = c;
        res.winLineStartRow = r;
        res.winLineEndRow = r;
        gemsFound = 1;
        while (gemsFound < gemsInARowToWin && c + 1 >= 0 && c + 1 < getColumnCount() && r - 1 >= 0 && r - 1 < getRowCount()) {
            c = c + 1;
            r = r - 1;
            if (getPlayerAt(r, c) == res.winningPlayer) {
                res.winLineStartRow = r;
                res.winLineStartColumn = c;
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go down-left
        c = startCol;
        r = startRow;
        while (gemsFound < gemsInARowToWin && c - 1 >= 0 && c - 1 < getColumnCount() && r + 1 >= 0 && r + 1 < getRowCount()) {
            c = c - 1;
            r = r + 1;
            if (getPlayerAt(r, c) == res.winningPlayer) {
                res.winLineEndRow = r;
                res.winLineEndColumn = c;
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // either tie or not finished

        // check if there is any space left
        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColumnCount(); col++) {
                if (getPlayerAt(row, col) == null) {
                    // we've found an empty space
                    res.winningPlayer = null;
                    return res;
                }
            }
        }

        // it's a tie
        res.winningPlayer = Player.TIE_PLAYER;
        return res;
    }

    @Override
    public String toString() {
        String res = "";

        for (int r = 0; r < this.getRowCount(); r++) {
            for (int c = 0; c < this.getColumnCount(); c++) {
                if (getPlayerAt(r, c) == null) {
                    res = res + "-";
                } else if (getPlayerAt(r, c) == getPlayer1()) {
                    res = res + "1";
                } else if (getPlayerAt(r, c) == getPlayer2()) {
                    res = res + "2";
                }

                if (c < getColumnCount() - 1) {
                    res = res + ", ";
                }
            }

            if (r < getRowCount() - 1) {
                res = res + "; ";
            }
        }

        return "[" + res + "]";
    }

    public GameEndRunnable getGameEndCallback() {
        return gameEndCallback.get();
    }

    public void setGameEndCallback(GameEndRunnable gameEndCallback) {
        this.gameEndCallback.set(gameEndCallback);
    }

    @SuppressWarnings("unused")
    public ObjectProperty<GameEndRunnable> gameEndCallbackProperty() {
        return gameEndCallback;
    }

    public class WinnerInfo {
        /**
         * The player who won the game, {@code null} if the game is not finished yet or {@link Player#TIE_PLAYER} if the game ended in a tie.
         */
        public Player winningPlayer;

        /**
         * The row coordinate of the starting point of the line of gems that caused the {@link #winningPlayer} to win
         */
        public int winLineStartRow;

        /**
         * The column coordinate of the starting point of the line of gems that caused the {@link #winningPlayer} to win
         */
        public int winLineStartColumn;

        /**
         * The row coordinate of the end point of the line of gems that caused the {@link #winningPlayer} to win
         */
        public int winLineEndRow;

        /**
         * The column coordinate of the end point of the line of gems that caused the {@link #winningPlayer} to win
         */
        public int winLineEndColumn;

        public boolean isFinished() {
            return winningPlayer != null;
        }

        public boolean isTie() {
            return winningPlayer == Player.TIE_PLAYER;
        }
    }
}
