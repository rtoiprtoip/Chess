package model.gameState;

import controller.domain.Coordinates;
import controller.domain.PieceKind;
import controller.exceptions.PromotionException;
import controller.domain.Colors;
import controller.domain.Time;
import model.pieces.Piece;

import java.io.Serializable;

public interface GameState extends Cloneable, Serializable {
    
    Colors getWhoseMove();
    
    boolean isPaused();
    
    void move(Coordinates moveFrom, Coordinates moveTo) throws PromotionException;
    
    Piece getPieceAt(Coordinates coordinates);
    
    void startOrResume();
    
    void endGame();
    
    void setPaused(boolean paused);
    
    Time getPlayerTime(Colors playerColor);
    
    void addPlayerTime(Time timeToAdd);
    
    void setPieceAt(Coordinates coordinates, Piece piece);
    
    Coordinates findKing(Colors kingColor);
    
    void decrementCurrentPlayerTime();
    
    Integer getLastMoveWasTwoFieldPawnAdvanceAtColumn();
    
    void promote(Coordinates moveFrom, Coordinates moveTo, PieceKind pieceChosen);
    
    void enPassant(Coordinates from, Coordinates to);
    
    void castle(Coordinates from, Coordinates to);
    
    GameState clone();
}
