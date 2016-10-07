package model.domain.pieces;

import controller.Coordinates;
import controller.exceptions.PromotionException;
import controller.exceptions.SpecialMoveException;
import controller.exceptions.TwoFieldsPawnAdvanceException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import model.domain.Colors;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(exclude = "hasMoved")
public abstract class Piece implements Serializable, Cloneable {
    
    @Getter
    protected final Colors color;
    
    @Getter
    @Setter
    protected boolean hasMoved = false; // Need this for castling
    
    /**
     * @param from Starting position of the piece
     * @param to   Goal of the piece
     * @return Path on the board, if the piece could move there on an 'empty'
     * board, i.e. it doesn't check for any other pieces that would
     * stand in the way (but it does check if something is on the ''to''
     * field), or if this move would expose king to check. If the piece
     * can't move there, it returns null.
     * @throws SpecialMoveException     if this move is possible as a special move, i.e. castling,
     *                                  promotion or en passant
     * @throws IllegalArgumentException if either of the fields is null or if from == to
     */
    public final List<Coordinates> getPath(Coordinates from, Coordinates to, boolean capturing)
    throws SpecialMoveException {
        if (from == null || to == null) {
            throw new IllegalArgumentException();
        }
        if (from.equals(to)) {
            return null;
        }
        if (checkIfCouldMoveThereOnEmptyBoard(from, to, capturing)) {
            return getPath(from, to);
        } else {
            return null;
        }
    }
    
    protected abstract boolean checkIfCouldMoveThereOnEmptyBoard(Coordinates from, Coordinates to, boolean capturing)
    throws SpecialMoveException;
    
    @SuppressWarnings("WeakerAccess")
    protected List<Coordinates> getPath(Coordinates from, Coordinates to) {
        Coordinates dir = Coordinates.getDir(from, to);
        List<Coordinates> ret = new LinkedList<>();
        for (Coordinates c = from.plus(dir); !c.equals(to); c = c.plus(dir)) {
            ret.add(c);
        }
        return ret;
    }
    
    Piece(Colors color) {
        if (color == null) {
            throw new IllegalArgumentException();
        }
        this.color = color;
    }
    
    public boolean isKing() {
        return false;
    }
    
    public void move(Coordinates to) throws PromotionException, TwoFieldsPawnAdvanceException {
        hasMoved = true;
    }
    
    @Override
    public Piece clone() {
        try {
            return (Piece) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    
    public static Piece produce(Colors color, String type) {
        switch (type) {
            case "rook":
                return new Rook(color);
            case "knight":
                return new Knight(color);
            case "bishop":
                return new Bishop(color);
            case "queen":
                return new Queen(color);
            case "king":
                return new King(color);
            case "pawn":
                return new Pawn(color);
            default:
                throw new IllegalArgumentException();
        }
    }
    
}
