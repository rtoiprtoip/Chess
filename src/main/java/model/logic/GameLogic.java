package model.logic;

import controller.Coordinates;
import controller.exceptions.PromotionException;
import controller.exceptions.SpecialMoveException;
import model.domain.Colors;
import model.domain.Time;
import model.domain.pieces.Piece;
import model.history.MoveHistory;

public interface GameLogic {
    
    String getPlayerTime(String playerColor);
    
    boolean isNotPaused();
    
    void setPaused(boolean paused);
    
    void newGame();
    
    void startOrResume();
    
    void endGame();
    
    boolean isThisValidMove(Coordinates moveFrom, Coordinates moveTo) throws SpecialMoveException;
    
    void move(Coordinates moveFrom, Coordinates moveTo) throws PromotionException;
    
    Colors getWhoseMove();
    
    void promote(Coordinates moveFrom, Coordinates moveTo, String promotionChoice);
    
    void castle(Coordinates moveFrom, Coordinates moveTo);
    
    void enPassant(Coordinates moveFrom, Coordinates moveTo);
    
    Piece getPieceAt(Coordinates coordinates);
    
    void setGameTime(int minutes, int seconds);
    
    void setTimeToAddAfterMove(int seconds);
    
    void loadGame(MoveHistory moveHistory);
    
    MoveHistory getMoveHistory();
    
    Time immutableDefaultGameTime = new Time(15, 0) {
        
        @Override
        public void add(Time timeAdded) {
            throw new UnsupportedOperationException("This instance is immutable");
        }
        
        @Override
        public void decrement() {
            throw new UnsupportedOperationException("This instance is immutable");
        }
    };
    
    void revertMove();
}
