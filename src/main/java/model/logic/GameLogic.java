package model.logic;

import controller.domain.Colors;
import controller.domain.Coordinates;
import controller.domain.PieceKind;
import controller.domain.Time;
import controller.exceptions.CastlingException;
import controller.exceptions.EnPassantException;
import controller.exceptions.PromotionException;
import controller.exceptions.SpecialMoveException;
import model.history.MoveHistory;
import model.pieces.Piece;

public interface GameLogic {
    
    Time getPlayerTime(Colors playerColor);
    
    boolean isNotPaused();
    
    void setPaused(boolean paused);
    
    void newGame();
    
    void startOrResume();
    
    void endGame();
    
    Colors getWhoseMove();
    
    /**
     * @throws IllegalStateException, if the service is not awaiting promotion choice
     *                                (see {@link #tryToMove(Coordinates, Coordinates) tryToMove}
     */
    void promote(PieceKind promotionChoice);
    
    Piece getPieceAt(Coordinates coordinates);
    
    void setGameTime(int minutes, int seconds);
    
    void setTimeToAddAfterMove(int seconds);
    
    void loadGame(MoveHistory moveHistory);
    
    MoveHistory getMoveHistory();
    
    void revertMove();
    
    /**
     * Checks if this move is correct. If not, returns false and leaves the game state unchanged. If the move
     * is castling or en passant, the move is performed and a corresponding exception is thrown. If the move involves
     * promotion, game state remains unchanged and PromotionException is thrown. In this case
     * {@link #promote(PieceKind) promote} function should be called before next tryToMove.
     * If the move is allowed and is not special, game state is changed and the method returns true.
     *
     * @throws PromotionException,    if the move requires promotion
     * @throws CastlingException,     if the move involves castling
     * @throws EnPassantException,    if the move involves en passant capture
     * @throws IllegalStateException, if the service is awaiting promotion choice
     */
    boolean tryToMove(Coordinates moveFrom, Coordinates moveTo) throws SpecialMoveException;
    
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
}
