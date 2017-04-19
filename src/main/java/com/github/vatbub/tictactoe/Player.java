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


import com.github.vatbub.tictactoe.view.AILevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Player entity in the game
 */
public class Player {
    /**
     * A Player that represents a finished game with no winner
     *
     * @see Board#getWinner(int, int)
     */
    public static final Player TIE_PLAYER = new Player(false, "tie");

    private String name;
    private boolean ai;

    public Player(boolean ai) {
        this(ai, NameList.getNextName());
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

    public void doAiTurn(Board currentBoard) {
        doAiTurn(currentBoard, currentBoard.getAiLevel());
    }

    public void doAiTurn(Board currentBoard, AILevel aiLevel) {
        switch (aiLevel) {
            case COMPLETELY_STUPID:
                int r;
                int c;
                do {
                    r = (int) Math.round(Math.random() * (currentBoard.getRowCount() - 1));
                    c = (int) Math.round(Math.random() * (currentBoard.getColumnCount() - 1));
                } while (currentBoard.getPlayerAt(r, c) != null);

                currentBoard.doTurn(new Board.Move(r, c));
                break;
            case SOMEWHAT_GOOD:
                AlphaBetaResult alphaBetaResult2 = alphaBeta(currentBoard, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true);
                currentBoard.doTurn(alphaBetaResult2.getBestMoveWithRandomness());
                break;
            case GOOD:
                AlphaBetaResult alphaBetaResult1 = alphaBeta(currentBoard, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 5, true);
                currentBoard.doTurn(alphaBetaResult1.bestMove);
                break;
            case UNBEATABLE:
                AlphaBetaResult alphaBetaResult3 = alphaBeta(currentBoard, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true);
                currentBoard.doTurn(alphaBetaResult3.bestMove);
                break;
        }
    }

    /**
     * Performs the MiniMax algorithm with alpha-beta-pruning on the specified game board.
     *
     * @param node             The node to evaluate
     * @param alpha            The current alpha value
     * @param beta             The current beta value
     * @param maximizingPlayer {@code true} if it is currently the maximizing player's turn
     * @return Either the heuristic value of the node if the node is a terminal node (Someone won, lost or it's a tie), {@code alpha} from the maximizing player or {@code beta} from the minimizing player.
     */
    private AlphaBetaResult alphaBeta(Board node, double alpha, double beta, double maxDepth, boolean maximizingPlayer) {
        AlphaBetaResult res = new AlphaBetaResult();
        res.alpha = alpha;
        res.beta = beta;

        // evaluate the current situation
        Board.Move lastMove = node.getLastMove();
        if (lastMove == null) {
            lastMove = new Board.Move(0, 0);
        }
        Board.WinnerInfo winnerInfo = node.getWinner(lastMove.getRow(), lastMove.getColumn());

        // base case
        if (winnerInfo.isFinished() || maxDepth == 0) {
            if (winnerInfo.isFinished()) {
                res.heuristicNodeValue = winnerInfo.getHeuristicValue(this);
            } else {
                res.heuristicNodeValue = 0;
            }
            res.returnType = ReturnType.heuristicValue;
            return res;
        }

        if (maximizingPlayer) {
            for (Board.Move move : node.getAvailableMoves()) {
                Board child = node.clone();
                child.doTurn(move, true);
                double previousAlpha = alpha;
                alpha = Math.max(alpha, alphaBeta(child, alpha, beta, maxDepth - 1, false).getResultValue());
                if (previousAlpha < alpha) {
                    res.bestMove = move;
                }
                res.moveScores.put(move, alpha);
                if (alpha >= beta) {
                    // prune
                    break;
                }
            }

            res.alpha = alpha;
            res.returnType = ReturnType.alpha;
            return res;
        } else {
            for (Board.Move move : node.getAvailableMoves()) {
                Board child = node.clone();
                child.doTurn(move, true);
                double previousBeta = beta;
                beta = Math.min(beta, alphaBeta(child, alpha, beta, maxDepth - 1, true).getResultValue());
                if (previousBeta > beta) {
                    res.bestMove = move;
                }
                res.moveScores.put(move, beta);
                if (alpha >= beta) {
                    // prune
                    break;
                }
            }

            res.beta = beta;
            res.returnType = ReturnType.beta;
            return res;
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    private enum ReturnType {
        alpha, beta, heuristicValue
    }

    private class AlphaBetaResult {
        final Map<Board.Move, Double> moveScores = new HashMap<>();
        Board.Move bestMove;
        double heuristicNodeValue;
        double alpha;
        double beta;
        ReturnType returnType;

        double getResultValue() {
            switch (returnType) {
                case alpha:
                    return alpha;
                case beta:
                    return beta;
                case heuristicValue:
                    return heuristicNodeValue;
                default:
                    // nothing was specified as a return type, not going to happen except if I forgot to specify it somewhere
                    throw new IllegalStateException("No return type specified. This is a bug of the AI, not your fault.");
            }
        }

        /*
         * Makes the ai less perfect
         */
        Board.Move getBestMoveWithRandomness() {
            double optimalScore;
            System.out.print(returnType.toString() + ", ");
            switch (returnType) {
                default:
                case alpha:
                    optimalScore = Double.NEGATIVE_INFINITY;
                    for (Map.Entry<Board.Move, Double> entry : moveScores.entrySet()) {
                        if (entry.getValue() > optimalScore) {
                            optimalScore = entry.getValue();
                        }
                    }
                    break;
                case beta:
                    optimalScore = Double.POSITIVE_INFINITY;
                    for (Map.Entry<Board.Move, Double> entry : moveScores.entrySet()) {
                        if (entry.getValue() < optimalScore) {
                            optimalScore = entry.getValue();
                        }
                    }
            }

            // get all the moves that have the optimal score and pick a random one among those
            List<Board.Move> optimalMoves = new ArrayList<>();

            for (Map.Entry<Board.Move, Double> entry : moveScores.entrySet()) {
                if (entry.getValue() == optimalScore) {
                    optimalMoves.add(entry.getKey());
                }
            }

            int random = (int) Math.round(Math.random() * (optimalMoves.size() - 1));
            System.out.println(random);
            return optimalMoves.get(random);
        }
    }
}
