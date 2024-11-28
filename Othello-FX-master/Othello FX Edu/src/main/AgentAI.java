package main;

import com.eudycontreras.othello.capsules.AgentMove;
import com.eudycontreras.othello.capsules.MoveWrapper;
import com.eudycontreras.othello.capsules.ObjectiveWrapper;
import com.eudycontreras.othello.controllers.Agent;
import com.eudycontreras.othello.enumerations.BoardCellState;
import com.eudycontreras.othello.enumerations.PlayerTurn;
import com.eudycontreras.othello.models.GameBoardState;
import com.eudycontreras.othello.controllers.AgentController;

public class AgentAI extends Agent {
    private static final int MAX_DEPTH = 5;

    public AgentAI(String name) {
        super(name, PlayerTurn.PLAYER_ONE);
    }

    AgentAI(PlayerTurn playerTurn) {
        super(playerTurn);
    }

    @Override
    public AgentMove getMove(GameBoardState gameState) {
        return alphaBeta(gameState, playerTurn, 0, Integer.MIN_VALUE, Integer.MAX_VALUE).getMove();
    }

    private Result alphaBeta(GameBoardState board, PlayerTurn player, int depth, int alpha, int beta) {
        setSearchDepth(depth);
        nodesExamined++;
        setNodesExamined(nodesExamined);
        if (depth == MAX_DEPTH || board.isTerminal()) {
            reachedLeafNodes++;
            setReachedLeafNodes(reachedLeafNodes);
            return new Result(evaluateBoard(board, player), null);
        }

        AgentMove bestMove = null;
        for (ObjectiveWrapper move : AgentController.getAvailableMoves(board, player)) {
            setSearchDepth(depth);
            GameBoardState newBoard = AgentController.getNewState(board, move);
            Result result = null;

            if (player == PlayerTurn.PLAYER_ONE) {
                result = alphaBeta(newBoard, PlayerTurn.PLAYER_TWO, depth + 1, alpha, beta);
                if (result.getScore().getValue() > alpha) {
                    alpha = result.getScore().getValue();
                    bestMove = new MoveWrapper(move);
                }
            } else {
                result = alphaBeta(newBoard, PlayerTurn.PLAYER_ONE, depth + 1, alpha, beta);
                if (result.getScore().getValue() < beta) {
                    beta = result.getScore().getValue();
                    bestMove = new MoveWrapper(move);
                }
            }

            if (alpha >= beta) {
                prunedCounter++;
                setPrunedCounter(prunedCounter);
                break;
            }
        }

        return new Result(new Score(player == PlayerTurn.PLAYER_ONE ? alpha : beta), bestMove);
    }


    private Score evaluateBoard(GameBoardState board, PlayerTurn player) {
        BoardCellState playerState = player == PlayerTurn.PLAYER_ONE ? BoardCellState.WHITE : BoardCellState.BLACK;
        long score = board.getStaticScore(playerState);
        return new Score((int) score);
    }

    private class Result {
        private final Score score;
        private final AgentMove move;

        public Result(Score score, AgentMove move) {
            this.score = score;
            this.move = move;
        }

        public Score getScore() {
            return score;
        }

        public AgentMove getMove() {
            return move;
        }
    }

    private class Score {
        private final int value;

        public Score(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}