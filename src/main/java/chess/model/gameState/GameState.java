package chess.model.gameState;

import chess.domain.Coordinates;
import chess.domain.PieceKind;
import chess.domain.exceptions.PromotionException;
import chess.domain.Colors;
import chess.domain.Time;
import chess.model.pieces.Piece;

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
