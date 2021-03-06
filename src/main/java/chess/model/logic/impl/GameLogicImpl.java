package chess.model.logic.impl;

import chess.domain.Colors;
import chess.domain.Coordinates;
import chess.domain.PieceKind;
import chess.domain.Time;
import chess.domain.exceptions.CastlingException;
import chess.domain.exceptions.EnPassantException;
import chess.domain.exceptions.PromotionException;
import chess.domain.exceptions.SpecialMoveException;
import chess.model.gameState.GameState;
import chess.model.gameState.impl.GameStateImpl;
import chess.model.history.MoveHistory;
import chess.model.history.impl.MoveHistoryImpl;
import chess.model.logic.GameLogic;
import chess.model.pieces.Piece;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EmptyStackException;
import java.util.List;
import java.util.Objects;

@Service
public class GameLogicImpl implements GameLogic {
    
    private MoveHistory moveHistory;
    private GameState gameState;
    
    private Time timeToAddAfterMove;
    private final TimeCounter timeCounter = new TimeCounter();
    
    private PromotionMoveData promotionMoveData;
    
    @Autowired
    public GameLogicImpl(Time defaultGameTime, Time defaultTimeAddedPerMove) {
        newGame(defaultGameTime, defaultTimeAddedPerMove);
        timeCounter.start();
    }
    
    @Override
    public void newGame(Time gameTime, Time timeAddedPerMove) {
        gameState = new GameStateImpl(new Time(gameTime));
        this.timeToAddAfterMove = new Time(timeAddedPerMove);
        moveHistory = new MoveHistoryImpl(gameState);
    }
    
    @Override
    public void promote(PieceKind pieceChosen) {
        if (promotionMoveData == null) {
            throw new IllegalStateException("Service is not awaiting promotion choice");
        }
        gameState.promote(promotionMoveData.moveFrom, promotionMoveData.moveTo, pieceChosen);
        actionToPerformAfterMove(promotionMoveData.moveFrom, promotionMoveData.moveTo, pieceChosen);
    }
    
    @Override
    public Piece getPieceAt(Coordinates coordinates) {
        return gameState.getPieceAt(coordinates);
    }
    
    @Override
    public void startOrResume() {
        gameState.startOrResume();
        
        synchronized (timeCounter) {
            timeCounter.notifyAll();
        }
    }
    
    @Override
    public void endGame() {
        gameState.endGame();
        promotionMoveData = null;
    }
    
    @Override
    public Time getPlayerTime(Colors color) {
        return gameState.getPlayerTime(color);
    }
    
    @Override
    public void loadGame(MoveHistory moveHistory) {
        this.moveHistory = moveHistory;
        gameState = moveHistory.peek();
        promotionMoveData = null;
    }
    
    @Override
    public MoveHistory getMoveHistory() {
        return moveHistory;
    }
    
    @Override
    public boolean tryToMove(Coordinates moveFrom, Coordinates moveTo) throws SpecialMoveException {
        if (promotionMoveData != null) {
            throw new IllegalStateException("Service is awaiting promotion choice");
        }
        try {
            if (isThisValidMove(moveFrom, moveTo)) {
                move(moveFrom, moveTo);
                return true;
            } else {
                return false;
            }
        } catch (CastlingException e) {
            castle(moveFrom, moveTo);
            throw e;
        } catch (EnPassantException e) {
            enPassant(moveFrom, moveTo);
            throw e;
        } catch (PromotionException e) {
            promotionMoveData = new PromotionMoveData(moveFrom, moveTo);
            throw e;
        }
    }
    
    @Override
    public void revertMove() {
        try {
            moveHistory.pop();
            gameState = moveHistory.peek();
        } catch (EmptyStackException e) {
            throw new IllegalStateException("This is the initial game state", e);
        }
    }
    
    @Override
    public boolean isNotPaused() {
        return !gameState.isPaused();
    }
    
    @Override
    public void setPaused(boolean paused) {
        gameState.setPaused(paused);
    }
    
    @Override
    public Colors getWhoseMove() {
        return gameState.getWhoseMove();
    }
    
    
    @SuppressWarnings({"SimplifiableIfStatement", "RedundantIfStatement"})
    private boolean isThisValidMove(Coordinates moveFrom, Coordinates moveTo) throws SpecialMoveException {
        try {
            // check if user wants to move an existing piece owned by him
            if (getPieceAt(moveFrom) == null || getPieceAt(moveFrom).getColor() != gameState.getWhoseMove()) {
                return false;
            }
            
            // check if user tries to capture his own piece
            if (getPieceAt(moveTo) != null && getPieceAt(moveTo).getColor() == gameState.getWhoseMove()) {
                return false;
            }
            
            if (!isThisValidMoveForgetCheckAndTurn(moveFrom, moveTo)) {
                return false;
            }
            
            if (checkIfKingWillBeCheckedAfterMove(moveFrom, moveTo)) {
                return false;
            }
            
            return true;
            
        } catch (EnPassantException e) {
            if (authorizeEnPassant(moveFrom, moveTo)) {
                throw e;
            } else {
                return false;
            }
        } catch (CastlingException e) {
            if (authorizeCastling(moveFrom, moveTo)) {
                throw e;
            } else {
                return false;
            }
        } catch (PromotionException e) {
            return !checkIfKingWillBeCheckedAfterMove(moveFrom, moveTo);
        }
    }
    
    private void move(Coordinates moveFrom, Coordinates moveTo) throws PromotionException {
        gameState.move(moveFrom, moveTo);
        actionToPerformAfterMove(moveFrom, moveTo);
    }
    
    private void castle(Coordinates moveFrom, Coordinates moveTo) {
        gameState.castle(moveFrom, moveTo);
        actionToPerformAfterMove(moveFrom, moveTo);
    }
    
    private void enPassant(Coordinates moveFrom, Coordinates moveTo) {
        gameState.enPassant(moveFrom, moveTo);
        actionToPerformAfterMove(moveFrom, moveTo);
    }
    
    private void actionToPerformAfterMove(Coordinates moveFrom, Coordinates moveTo) {
        actionToPerformAfterMove(moveFrom, moveTo, null);
    }
    
    private void actionToPerformAfterMove(Coordinates moveFrom, Coordinates moveTo, PieceKind promotionChoice) {
        gameState.addPlayerTime(timeToAddAfterMove);
        moveHistory.push(gameState, moveFrom, moveTo, promotionChoice);
        promotionMoveData = null;
        synchronized (timeCounter) {
            timeCounter.notifyAll();
        }
    }
    
    private boolean checkIfKingWillBeCheckedAfterMove(Coordinates moveFrom, Coordinates moveTo) {
        
        // save current state
        Piece one = getPieceAt(moveFrom);
        Piece two = getPieceAt(moveTo);
        
        try {
            // perform a virtual move
            setPieceAt(moveTo, one);
            setPieceAt(moveFrom, null);
            if (checkIfKingIsChecked(gameState.getWhoseMove())) {
                return true;
            }
        } finally {
            //undo virtual move
            setPieceAt(moveFrom, one);
            setPieceAt(moveTo, two);
        }
        
        return false;
    }
    
    private boolean isThisValidMoveForgetCheckAndTurn(Coordinates moveFrom, Coordinates moveTo)
    throws SpecialMoveException {
        if (getPieceAt(moveFrom) == null) {
            return false;
        }
        if (getPieceAt(moveTo) != null && getPieceAt(moveTo).getColor() == getPieceAt(moveFrom).getColor()) {
            return false;
        }
        List<Coordinates> path = getPieceAt(moveFrom).getPath(moveFrom, moveTo, getPieceAt(moveTo) != null);
        if (path == null) {
            return false;
        }
        for (Coordinates c : path) {
            if (getPieceAt(c) != null) {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkIfKingIsChecked(Colors kingColor) {
        Coordinates kingPos = gameState.findKing(kingColor);
        
        // check for check
        for (int i = 1; i <= 8; ++i) {
            for (int j = 1; j <= 8; ++j) {
                try {
                    if (getPieceAt(i, j) != null && getPieceAt(i, j).getColor() != kingColor
                        && isThisValidMoveForgetCheckAndTurn(Coordinates.of(i, j), kingPos)) {
                        return true;
                    }
                } catch (PromotionException e) {
                    return true;
                } catch (SpecialMoveException e) {
                    return false;
                }
            }
        }
        return false;
    }
    
    private boolean authorizeCastling(Coordinates moveFrom, Coordinates moveTo) {
        Colors color = getPieceAt(moveFrom).getColor();
        assert getPieceAt(moveFrom).isKing();
        Coordinates dir = Coordinates.getDir(moveFrom, moveTo);
        Coordinates rookPos = Coordinates.of(dir.getCol() > 0 ? 8 : 1, moveFrom.getRow());
        if (getPieceAt(rookPos) == null || getPieceAt(rookPos).isHasMoved()) {
            return false;
        }
        if (checkIfKingIsChecked(color)) {
            return false;
        }
        for (Coordinates c = moveFrom.plus(dir); c != rookPos; c = c.plus(dir)) {
            if (getPieceAt(c) != null) {
                return false;
            }
            Piece king = getPieceAt(moveFrom);
            try {
                // perform a virtual move
                setPieceAt(c, king);
                setPieceAt(moveFrom, null);
                if (checkIfKingIsChecked(color)) {
                    return false;
                }
            } finally {
                setPieceAt(c, null);
                setPieceAt(moveFrom, king);
            }
        }
        return true;
    }
    
    private boolean authorizeEnPassant(Coordinates moveFrom, Coordinates moveTo) {
        if (!Objects.equals(gameState.getLastMoveWasTwoFieldPawnAdvanceAtColumn(), moveTo.getCol())) {
            return false;
        }
        
        Piece movingPawn = getPieceAt(moveFrom);
        Coordinates capturedPawnCoordinates = Coordinates.of(moveTo.getCol(), moveFrom.getRow());
        Piece capturedPawn = getPieceAt(capturedPawnCoordinates);
        
        try {
            // perform a virtual move
            setPieceAt(moveTo, movingPawn);
            setPieceAt(moveFrom, null);
            setPieceAt(capturedPawnCoordinates, null);
            if (checkIfKingIsChecked(gameState.getWhoseMove())) {
                return false;
            }
        } finally {
            //undo virtual move
            setPieceAt(moveTo, null);
            setPieceAt(moveFrom, movingPawn);
            setPieceAt(capturedPawnCoordinates, capturedPawn);
        }
        return true;
    }
    
    private void setPieceAt(Coordinates coordinates, Piece piece) {
        gameState.setPieceAt(coordinates, piece);
    }
    
    private Piece getPieceAt(int i, int j) {
        return getPieceAt(Coordinates.of(i, j));
    }
    
    private class TimeCounter extends Thread {
        
        private TimeCounter() {
            super();
            if (timeCounter != null) {
                throw new AssertionError("Tried to create a second instance of TimeCounter");
            }
            setDaemon(true);
        }
        
        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            synchronized (this) {
                while (true) {
                    while (gameState.isPaused()) {
                        try {
                            this.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                    gameState.decrementCurrentPlayerTime();
                    try {
                        this.wait(Time.precision.toMillis());
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }
    
    @Value
    private class PromotionMoveData {
        
        Coordinates moveFrom;
        Coordinates moveTo;
    }
}
