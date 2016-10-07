package model.gameState;

import controller.Coordinates;
import model.domain.Colors;
import controller.exceptions.PromotionException;
import model.domain.Time;
import model.domain.pieces.Piece;

import java.io.Serializable;

public interface GameState extends Cloneable, Serializable {
    
    void newGame(Time timePerPlayer);
    
    Colors getWhoseMove();
    
    boolean isPaused();
    
    void move(Coordinates moveFrom, Coordinates moveTo) throws PromotionException;
    
    Piece getPieceAt(Coordinates coordinates);
    
    void startOrResume();
    
    void endGame();
    
    void setPaused(boolean paused);
    
    String getPlayerTime(String playerColor);
    
    void addPlayerTime(Time timeToAdd);
    
    void setPieceAt(Coordinates coordinates, Piece piece);
    
    Coordinates findKing(Colors kingColor);
    
    void decrementCurrentPlayerTime();
    
    Integer getLastMoveWasTwoFieldPawnAdvanceAtColumn();
    
    void promote(Coordinates moveFrom, Coordinates moveTo, String pieceChosen);
    
    void enPassant(Coordinates from, Coordinates to);
    
    void castle(Coordinates from, Coordinates to);
    
    GameState clone();
}
