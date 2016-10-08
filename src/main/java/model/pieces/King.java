package model.pieces;

import controller.domain.Coordinates;
import controller.domain.PieceKind;
import controller.exceptions.CastlingException;
import controller.exceptions.SpecialMoveException;
import lombok.EqualsAndHashCode;
import controller.domain.Colors;

@EqualsAndHashCode(callSuper = true)
final class King extends Piece {
    
    King(Colors color) {
        super(color, PieceKind.KING);
    }
    
    @Override
    public boolean checkIfCouldMoveThereOnEmptyBoard(Coordinates from, Coordinates to, boolean capturing)
    throws SpecialMoveException {
        int x1 = from.getCol(), x2 = to.getCol(), y1 = from.getRow(), y2 = to.getRow();
        if (Math.abs(x1 - x2) <= 1 && Math.abs(y1 - y2) <= 1) {
            return true;
        }
    
        if (y1 == y2 && Math.abs(x1 - x2) == 2 && !hasMoved) {
            throw new CastlingException();
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        return ("" + color + "_king").toLowerCase();
    }
    
    @Override
    public boolean isKing() {
        return true;
    }
    
}
