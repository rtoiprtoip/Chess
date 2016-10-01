package model;

import controller.Coordinates;
import model.impl.Colors;
import model.impl.PromotionException;
import model.impl.SpecialMoveException;
import model.impl.pieces.Piece;

public interface GameLogic {
    
    String getTime(String color);
    
    boolean isPaused();
    
    void setPaused(boolean b);
    
    void newGame();
    
    void startOrResume();
    
    void endGame();
    
    boolean isThisValidMove(Coordinates moveFrom, Coordinates moveTo) throws SpecialMoveException;
    
    void move(Coordinates moveFrom, Coordinates moveTo) throws PromotionException;
    
    Colors getWhoseMove();
    
    void promote(Coordinates moveFrom, Coordinates moveTo, String promotionChoice);
    
    void castle(Coordinates moveFrom, Coordinates moveTo);
    
    void enPassant(Coordinates moveFrom, Coordinates moveTo);
    
    Piece getPieceAt(Coordinates c);
    
    void setGameTime(int minutes, int seconds);
    
    void setTimeAddedPerMove(int seconds);
}
