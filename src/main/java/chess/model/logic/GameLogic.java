package chess.model.logic;

import chess.domain.Colors;
import chess.domain.Coordinates;
import chess.domain.PieceKind;
import chess.domain.Time;
import chess.domain.exceptions.CastlingException;
import chess.domain.exceptions.EnPassantException;
import chess.domain.exceptions.PromotionException;
import chess.domain.exceptions.SpecialMoveException;
import chess.model.history.MoveHistory;
import chess.model.pieces.Piece;

public interface GameLogic {
    
    Time getPlayerTime(Colors playerColor);
    
    boolean isNotPaused();
    
    void setPaused(boolean paused);
    
    void newGame(Time gameTime, Time timeAddedPerMove);
    
    void startOrResume();
    
    void endGame();
    
    Colors getWhoseMove();
    
    /**
     * @throws IllegalStateException, if the service is not awaiting promotion choice
     *                                (see {@link #tryToMove(Coordinates, Coordinates) tryToMove}
     */
    void promote(PieceKind promotionChoice);
    
    Piece getPieceAt(Coordinates coordinates);
    
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
}
