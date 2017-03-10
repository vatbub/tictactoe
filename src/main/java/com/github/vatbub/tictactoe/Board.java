package com.github.vatbub.tictactoe;

/**
 *A classic tic tac toe board.
 */
public class Board {
    private Cell[][] board;
    private int dimRows;
    private int dimCols;

    /**
     * Initializes a new 3*3 game board
     */
    public Board(){
        this(3);
    }

    /**
     * Initializes a new quadratic game board with the given dimension
     * @param quadraticDim The edge length of the board
     */
    public Board(int quadraticDim){
        this(quadraticDim, quadraticDim);
    }

    /**
     * Initializes a new board with the given dimensions
     * @param dimRows The desired width of the board
     * @param dimCols The desired height of the board.
     */
    public Board(int dimRows,int dimCols){
        this.dimCols = dimCols;
        this.dimRows = dimRows;
        board = new Cell[dimRows][dimCols];
        for (int r = 0; r<dimRows; r++){
            for (int c = 0;c<dimCols;c++){
                board[r][c] = new Cell();
            }
        }
    }

    public void setPlayerAt(int row, int col, Player player){
        board[row][col].setCurrentPlayer(player);
    }

    public Player getPlayerAt(int row, int col){
        return board[row][col].getCurrentPlayer();
    }

    public int getRowCount(){
        return this.dimRows;
    }

    public int getColumnCount(){
        return this.dimCols;
    }
}
