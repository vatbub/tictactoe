package com.github.vatbub.tictactoe;

/**
 * A Player entity in the game
 */
public class Player {
    /**
     * A Player that represents a finished game with no winner
     * @see Board#getWinner()
     */
    public static final Player TIE_PLAYER = new Player(false, "tie");

    private String name;
    private boolean ai;

    public Player(boolean ai) {
        this(ai, ai ? NameList.getNextAIName() : NameList.getNextHumanName());
    }

    public Player(boolean ai, String name) {
        this.setAi(ai);
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAi() {
        return ai;
    }

    public void setAi(boolean ai) {
        this.ai = ai;
    }

    public void doAiTurn(Board currentBoard, Player opponent) {
        // will be a mini max algorithm with alpha beta pruning
        int r;
        int c;
        do {
            r = (int) Math.round(Math.random() * (currentBoard.getRowCount() - 1));
            c = (int) Math.round(Math.random() * (currentBoard.getColumnCount() - 1));
        } while (currentBoard.getPlayerAt(r, c) != null);

        currentBoard.doTurn(r, c);
    }

    @Override
    public String toString() {
        return getName();
    }
}
