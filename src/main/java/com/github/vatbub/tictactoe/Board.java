package com.github.vatbub.tictactoe;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A classic tic tac toe board.
 */
public class Board {
    private static final int gemsInARowToWin = 3;

    private Cell[][] board;
    private int dimRows;
    private int dimCols;
    private Player player1;
    private Player player2;
    private ObjectProperty<Player> currentPlayer = new SimpleObjectProperty<>();

    /**
     * Initializes a new 3*3 game board
     */
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

        Player opponent = getCurrentPlayer();

        if (getPlayerAt(row, col) != null) {
            throw new IllegalStateException("Cell is already taken by a player");
        }

        this.setPlayerAt(row, col, getCurrentPlayer());
        if (getCurrentPlayer() == getPlayer1()) {
            currentPlayer.set(getPlayer2());
        } else {
            currentPlayer.set(getPlayer1());
        }

        if (getCurrentPlayer().isAi()) {
            getCurrentPlayer().doAiTurn(this, opponent);
        }
    }

    /**
     * Determines the winner of the game
     *
     * @param startRow The row of the cell where the winner search shall start. This should be the row of the last turn.
     * @param startCol The column of the cell where the winner search shall start. This should be the column of the last turn.
     * @return The winner of the game, {@code Player.TIE_PLAYER} if the game is finished but there is no winner or {@code null} if the game is not finished yet.
     */
    public Player getWinner(int startRow, int startCol) {
        int gemsFound = 1;
        int r = startRow;
        int c = startCol;
        Player res = getPlayerAt(r, c);

        // go to the up
        while (gemsFound < gemsInARowToWin && r >= 0 && r < getRowCount()) {
            r = r - 1;
            if (getPlayerAt(r, c) == res) {
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go to the down
        r = startRow;
        while (gemsFound < gemsInARowToWin && r >= 0 && r < getRowCount()) {
            r = r + 1;
            if (getPlayerAt(r, c) == res) {
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go left
        r = startRow;
        gemsFound = 0;
        while (gemsFound < gemsInARowToWin && c >= 0 && c < getColumnCount()) {
            c = c - 1;
            if (getPlayerAt(r, c) == res) {
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go right
        c = startCol;
        while (gemsFound < gemsInARowToWin && c >= 0 && c < getColumnCount()) {
            c = c + 1;
            if (getPlayerAt(r, c) == res) {
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go up-left
        c = startCol;
        gemsFound = 0;
        while (gemsFound < gemsInARowToWin && c >= 0 && c < getColumnCount() && r >= 0 && r < getRowCount()) {
            c = c - 1;
            r = r - 1;
            if (getPlayerAt(r, c) == res) {
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go down-right
        c = startCol;
        r = startRow;
        while (gemsFound < gemsInARowToWin && c >= 0 && c < getColumnCount() && r >= 0 && r < getRowCount()) {
            c = c + 1;
            r = r + 1;
            if (getPlayerAt(r, c) == res) {
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go up-right
        c = startCol;
        r=startRow;
        gemsFound = 0;
        while (gemsFound < gemsInARowToWin && c >= 0 && c < getColumnCount() && r >= 0 && r < getRowCount()) {
            c = c + 1;
            r = r - 1;
            if (getPlayerAt(r, c) == res) {
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // go down-left
        c = startCol;
        r = startRow;
        while (gemsFound < gemsInARowToWin && c >= 0 && c < getColumnCount() && r >= 0 && r < getRowCount()) {
            c = c - 1;
            r = r + 1;
            if (getPlayerAt(r, c) == res) {
                gemsFound = gemsFound + 1;
            }
        }
        if (gemsFound >= gemsInARowToWin) {
            return res;
        }

        // either tie or not finished

        // check if there is any space left
        for (int row = 0; row<getRowCount(); row++){
            for (int col = 0; col<getColumnCount(); col++){
                if (getPlayerAt(row, col)==null){
                    // we've found an empty space
                    return null;
                }
            }
        }

        // it's a tie
        return Player.TIE_PLAYER;
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
}
