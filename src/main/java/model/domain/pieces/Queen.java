package model.domain.pieces;

import controller.Coordinates;
import lombok.EqualsAndHashCode;
import model.domain.Colors;

@EqualsAndHashCode(callSuper = true)
final class Queen extends Piece {
    
    Queen(Colors color) {
        super(color);
    }
    
    @Override
    public boolean checkIfCouldMoveThereOnEmptyBoard(Coordinates from, Coordinates to, boolean capturing) {
        int x1 = from.getCol(), x2 = to.getCol(), y1 = from.getRow(), y2 = to.getRow();
        return Math.abs(x1 - x2) == Math.abs(y1 - y2) || x1 == x2 || y1 == y2;
        
    }
    
    @Override
    public String toString() {
        return ("" + color + "_queen").toLowerCase();
    }
    
}
