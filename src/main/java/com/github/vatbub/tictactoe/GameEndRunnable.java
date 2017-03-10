package com.github.vatbub.tictactoe;

/**
 * A runnable that is executed when a game ends
 */
public interface GameEndRunnable {
    void run(Player winner);
}
