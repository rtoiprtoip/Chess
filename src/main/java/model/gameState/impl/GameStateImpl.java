package model.gameState.impl;

import controller.domain.Coordinates;
import controller.domain.PieceKind;
import controller.exceptions.PromotionException;
import controller.exceptions.TwoFieldsPawnAdvanceException;
import controller.domain.Colors;
import controller.domain.Time;
import model.pieces.Piece;
import model.gameState.GameState;

import static controller.domain.PieceKind.*;

public class GameStateImpl implements GameState {
    
    private Piece[][] fields;
    
    private Time whiteTime;
    private Time blackTime;
    private transient boolean isPaused = true;
    
    private Colors whoseMove = Colors.WHITE;
    
    private Integer lastMoveWasTwoFieldPawnAdvanceAtColumn;
    
    public GameStateImpl(Time timePerPlayer) {
        fields = new Piece[9][9];
        newGame(timePerPlayer);
    }
    
    private void newGame(Time timePerPlayer) {
        fields[1][1] = Piece.produce(Colors.WHITE, ROOK);
        fields[2][1] = Piece.produce(Colors.WHITE, KNIGHT);
        fields[3][1] = Piece.produce(Colors.WHITE, BISHOP);
        fields[4][1] = Piece.produce(Colors.WHITE, QUEEN);
        fields[5][1] = Piece.produce(Colors.WHITE, KING);
        fields[6][1] = Piece.produce(Colors.WHITE, BISHOP);
        fields[7][1] = Piece.produce(Colors.WHITE, KNIGHT);
        fields[8][1] = Piece.produce(Colors.WHITE, ROOK);
        
        fields[1][8] = Piece.produce(Colors.BLACK, ROOK);
        fields[2][8] = Piece.produce(Colors.BLACK, KNIGHT);
        fields[3][8] = Piece.produce(Colors.BLACK, BISHOP);
        fields[4][8] = Piece.produce(Colors.BLACK, QUEEN);
        fields[5][8] = Piece.produce(Colors.BLACK, KING);
        fields[6][8] = Piece.produce(Colors.BLACK, BISHOP);
        fields[7][8] = Piece.produce(Colors.BLACK, KNIGHT);
        fields[8][8] = Piece.produce(Colors.BLACK, ROOK);
        
        for (int i = 1; i < 9; ++i) {
            fields[i][2] = Piece.produce(Colors.WHITE, PAWN);
            fields[i][7] = Piece.produce(Colors.BLACK, PAWN);
            for (int j = 3; j < 7; ++j) {
                fields[i][j] = null;
            }
        }
        
        whoseMove = Colors.WHITE;
        
        setTimeForPlayers(timePerPlayer);
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
    public void promote(Coordinates moveFrom, Coordinates moveTo, PieceKind pieceChosen) {
        setPieceAt(moveTo, Piece.produce(getPieceAt(moveFrom).getColor(), pieceChosen));
        setPieceAt(moveFrom, null);
        lastMoveWasTwoFieldPawnAdvanceAtColumn = null;
        toggleWhoseMove();
    }
    
    @Override
    public void castle(Coordinates moveFrom, Coordinates moveTo) {
        Coordinates dir = Coordinates.getDir(moveFrom, moveTo);
        Coordinates rookPos = Coordinates.of(dir.getCol() > 0 ? 8 : 1, moveFrom.getRow());
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
        setPieceAt(Coordinates.of(moveTo.getCol(), moveFrom.getRow()), null);
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
    public Time getPlayerTime(Colors playerColor) {
        switch (playerColor) {
            case WHITE:
                return whiteTime.clone();
            case BLACK:
                return blackTime.clone();
            default:
                throw new AssertionError();
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
    
    private void setTimeForPlayers(Time timePerPlayer) {
        whiteTime = new Time(timePerPlayer);
        blackTime = new Time(timePerPlayer);
        
    }
    
    @Override
    public GameState clone() {
        try {
            GameStateImpl copy = (GameStateImpl) super.clone();
            copy.fields = copyFields();
            copy.whiteTime = whiteTime.clone();
            copy.blackTime = blackTime.clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    
    @Override
    public Coordinates findKing(Colors kingColor) {
        
        for (int i = 1; i <= 8; ++i) {
            for (int j = 1; j <= 8; ++j) {
                if (fields[i][j] != null && fields[i][j].isKing() && fields[i][j].getColor() == kingColor) {
                    return Coordinates.of(i, j);
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
    
    private Piece[][] copyFields() {
        Piece[][] copy = new Piece[9][9];
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (fields[i][j] == null) {
                    continue;
                }
                copy[i][j] = fields[i][j].clone();
            }
        }
        return copy;
    }
}
