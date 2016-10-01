package model.gameState.impl;

import controller.Coordinates;
import model.gameState.GameState;
import model.domain.Colors;
import controller.exceptions.PromotionException;
import model.domain.Time;
import controller.exceptions.TwoFieldsPawnAdvanceException;
import model.domain.pieces.Piece;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class GameStateImpl implements GameState, Serializable {
    
    private final Piece[][] fields = new Piece[9][9];
    
    private Time whiteTime;
    private Time blackTime;
    private transient boolean isPaused = true;
    
    private Colors whoseMove = Colors.WHITE;
    
    private Integer lastMoveWasTwoFieldPawnAdvanceAtColumn;
    
    @Override
    public void newGame(Time timePerPlayer) {
        fields[1][1] = Piece.produce(Colors.WHITE, "rook");
        fields[2][1] = Piece.produce(Colors.WHITE, "knight");
        fields[3][1] = Piece.produce(Colors.WHITE, "bishop");
        fields[4][1] = Piece.produce(Colors.WHITE, "queen");
        fields[5][1] = Piece.produce(Colors.WHITE, "king");
        fields[6][1] = Piece.produce(Colors.WHITE, "bishop");
        fields[7][1] = Piece.produce(Colors.WHITE, "knight");
        fields[8][1] = Piece.produce(Colors.WHITE, "rook");
        
        fields[1][8] = Piece.produce(Colors.BLACK, "rook");
        fields[2][8] = Piece.produce(Colors.BLACK, "knight");
        fields[3][8] = Piece.produce(Colors.BLACK, "bishop");
        fields[4][8] = Piece.produce(Colors.BLACK, "queen");
        fields[5][8] = Piece.produce(Colors.BLACK, "king");
        fields[6][8] = Piece.produce(Colors.BLACK, "bishop");
        fields[7][8] = Piece.produce(Colors.BLACK, "knight");
        fields[8][8] = Piece.produce(Colors.BLACK, "rook");
        
        for (int i = 1; i < 9; ++i) {
            fields[i][2] = Piece.produce(Colors.WHITE, "pawn");
            fields[i][7] = Piece.produce(Colors.BLACK, "pawn");
            for (int j = 3; j < 7; ++j) {
                fields[i][j] = null;
            }
        }
        
        whoseMove = Colors.WHITE;
        
        whiteTime = new Time(timePerPlayer);
        blackTime = new Time(timePerPlayer);
    }
    
    @Override
    public void startOrResume() {
        setPaused(false);
    }
    
    @Override
    public void endGame() {
        setPaused(true);
        
        for (int i = 0; i <= 8; ++i) {
            for (int j = 0; j <= 8; ++j) {
                fields[i][j] = null;
            }
        }
    }
    
    @Override
    public void move(Coordinates moveFrom, Coordinates moveTo) throws PromotionException {
        lastMoveWasTwoFieldPawnAdvanceAtColumn = null;
        try {
            getPieceAt(moveFrom).move(moveTo);
        } catch (TwoFieldsPawnAdvanceException e) {
            lastMoveWasTwoFieldPawnAdvanceAtColumn = moveTo.getCol();
        }
        setPieceAt(moveTo, getPieceAt(moveFrom));
        setPieceAt(moveFrom, null);
        toggleWhoseMove();
    }
    
    @Override
    public void promote(Coordinates moveFrom, Coordinates moveTo, String pieceChosen) {
        setPieceAt(moveTo, Piece.produce(getPieceAt(moveFrom).getColor(), pieceChosen));
        setPieceAt(moveFrom, null);
        lastMoveWasTwoFieldPawnAdvanceAtColumn = null;
        toggleWhoseMove();
    }
    
    @Override
    public void castle(Coordinates moveFrom, Coordinates moveTo) {
        Coordinates dir = Coordinates.getDir(moveFrom, moveTo);
        Coordinates rookPos = new Coordinates(dir.getCol() > 0 ? 8 : 1, moveFrom.getRow());
        try {
            getPieceAt(moveFrom).move(moveTo);
            getPieceAt(rookPos).move(moveFrom.plus(dir));
        } catch (PromotionException | TwoFieldsPawnAdvanceException e) {
            throw new AssertionError();
        }
        setPieceAt(moveTo, getPieceAt(moveFrom));
        setPieceAt(moveFrom.plus(dir), getPieceAt(rookPos));
        setPieceAt(moveFrom, null);
        setPieceAt(rookPos, null);
        lastMoveWasTwoFieldPawnAdvanceAtColumn = null;
        toggleWhoseMove();
    }
    
    @Override
    public void enPassant(Coordinates moveFrom, Coordinates moveTo) {
        setPieceAt(moveTo, getPieceAt(moveFrom));
        setPieceAt(moveFrom, null);
        setPieceAt(new Coordinates(moveTo.getCol(), moveFrom.getRow()), null);
        lastMoveWasTwoFieldPawnAdvanceAtColumn = null;
        toggleWhoseMove();
    }
    
    @Override
    public Colors getWhoseMove() {
        return whoseMove;
    }
    
    @Override
    public boolean isPaused() {
        return isPaused;
    }
    
    @Override
    public void setPaused(boolean paused) {
        this.isPaused = paused;
        
    }
    
    @Override
    public Piece getPieceAt(Coordinates coordinates) {
        return fields[coordinates.getCol()][coordinates.getRow()];
    }
    
    @Override
    public void setPieceAt(Coordinates coordinates, Piece piece) {
        fields[coordinates.getCol()][coordinates.getRow()] = piece;
    }
    
    @Override
    public String getPlayerTime(String playerColor) {
        switch (playerColor) {
            case "white":
                return whiteTime.toString();
            case "black":
                return blackTime.toString();
            default:
                throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void addPlayerTime(Time timeToAdd) {
        Time whoseTime = (whoseMove == Colors.WHITE) ? whiteTime : blackTime;
        whoseTime.add(timeToAdd);
    }
    
    @Override
    public void decrementCurrentPlayerTime() {
        Time whoseTime = (whoseMove == Colors.WHITE) ? whiteTime : blackTime;
        whoseTime.decrement();
        
    }
    
    @Override
    public void setTimeForPlayers(Time timePerPlayer) {
        whiteTime = new Time(timePerPlayer);
        blackTime = new Time(timePerPlayer);
        
    }
    
    @Override
    public Coordinates findKing(Colors kingColor) {
        
        for (int i = 1; i <= 8; ++i) {
            for (int j = 1; j <= 8; ++j) {
                if (fields[i][j] != null && fields[i][j].isKing() && fields[i][j].getColor() == kingColor) {
                    return new Coordinates(i, j);
                }
            }
        }
        throw new AssertionError("Could not find the king");
    }
    
    @Override
    public Integer getLastMoveWasTwoFieldPawnAdvanceAtColumn() {
        return lastMoveWasTwoFieldPawnAdvanceAtColumn;
    }
    
    private void toggleWhoseMove() {
        whoseMove = (whoseMove == Colors.WHITE ? Colors.BLACK : Colors.WHITE);
    }
}
