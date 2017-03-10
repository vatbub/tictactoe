package com.github.vatbub.tictactoe;

/**
 * A Player entity in the game
 */
public class Player {
    public static Player PLAYER_1;
    public static Player PLAYER_2;

    private String name;
    private boolean ai;

    public Player(boolean ai){
        this(ai, ai ? NameList.getNextAIName() : NameList.getNextHumanName());
    }

    public Player(boolean ai, String name){
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

    public void doAiTurn(Board currentBoard, Player opponent){
        // will be a mini max algorithm with alpha beta pruning
        int r;
        int c;
        do {
            r  = (int) Math.round(Math.random() * currentBoard.getRowCount());
            c = (int) Math.round(Math.random() * currentBoard.getColumnCount());
        }while(currentBoard.getPlayerAt(r, c)!=null);

        currentBoard.setPlayerAt(r, c, this);
    }
}
