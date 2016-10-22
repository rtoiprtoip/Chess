package chess.model.history.impl;

import chess.domain.Coordinates;
import chess.domain.PieceKind;
import chess.model.gameState.GameState;
import chess.model.history.MoveHistory;
import lombok.NonNull;

import java.util.Deque;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;

public class MoveHistoryImpl implements MoveHistory {
    
    // holds clones of GameStates
    private final Deque<GameState> gameStateStack = new LinkedList<>();
    private final LinkedList<String> moveLog = new LinkedList<>();
    
    public MoveHistoryImpl(@NonNull GameState gameState) {
        gameStateStack.addLast(gameState.clone());
        moveLog.add("init");
    }
    
    @Override
    public GameState pop() {
        if (gameStateStack.size() < 2) {
            throw new EmptyStackException();
        }
        moveLog.removeLast();
        return gameStateStack.removeLast().clone();
    }
    
    @Override
    public void push(GameState gameState, Coordinates moveFrom, Coordinates moveTo, PieceKind promotionChoice) {
        gameStateStack.addLast(gameState.clone());
        
        String promotion = promotionChoice == null ? "" : promotionChoice.getName();
        moveLog.addLast(moveFrom + "-" + moveTo + " " + promotion);
    }
    
    @Override
    public List<String> getMoveLog() {
        //noinspection unchecked
        return (List<String>) moveLog.clone();
    }
    
    @Override
    public GameState peek() {
        return gameStateStack.peekLast().clone();
    }
}
