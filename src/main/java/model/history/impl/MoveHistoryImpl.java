package model.history.impl;

import controller.Coordinates;
import lombok.NonNull;
import model.gameState.GameState;
import model.history.MoveHistory;

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
        gameStateStack.removeLast();
        return gameStateStack.peekLast().clone();
    }
    
    @Override
    public void push(GameState gameState, Coordinates moveFrom, Coordinates moveTo, String promotionChoice) {
        gameStateStack.addLast(gameState.clone());
        
        String promotion = promotionChoice == null ? "" : promotionChoice.split("_")[1];
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
