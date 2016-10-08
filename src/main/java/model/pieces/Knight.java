package model.pieces;

import controller.domain.Coordinates;
import controller.domain.PieceKind;
import lombok.EqualsAndHashCode;
import controller.domain.Colors;

import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
final class Knight extends Piece {
    
    Knight(Colors color) {
        super(color, PieceKind.KNIGHT);
    }
    
    @Override
    public boolean checkIfCouldMoveThereOnEmptyBoard(Coordinates from, Coordinates to, boolean capturing) {
        int x1 = from.getCol(), x2 = to.getCol(), y1 = from.getRow(), y2 = to.getRow();
        int d1 = Math.abs(x1 - x2), d2 = Math.abs(y1 - y2);
        return (d1 == 1 && d2 == 2) || (d1 == 2 && d2 == 1);
    }
    
    @Override
    protected List<Coordinates> getPath(Coordinates from, Coordinates to) {
        return new LinkedList<>();
    }
    
    @Override
    public String toString() {
        return ("" + color + "_knight").toLowerCase();
    }
    
}
