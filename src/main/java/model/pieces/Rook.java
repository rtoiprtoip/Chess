package model.pieces;

import controller.domain.Colors;
import controller.domain.Coordinates;
import controller.domain.PieceKind;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
final class Rook extends Piece {
    
    Rook(Colors color) {
        super(color, PieceKind.ROOK);
    }
    
    @Override
    public boolean checkIfCouldMoveThereOnEmptyBoard(Coordinates from, Coordinates to, boolean capturing) {
        int x1 = from.getCol(), x2 = to.getCol(), y1 = from.getRow(), y2 = to.getRow();
        return x1 == x2 || y1 == y2;
        
    }
    
    @Override
    public String toString() {
        return ("" + color + "_rook").toLowerCase();
    }
    
}
