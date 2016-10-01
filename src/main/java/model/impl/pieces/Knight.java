package model.impl.pieces;

import java.util.*;

import controller.Coordinates;
import lombok.EqualsAndHashCode;
import model.impl.Colors;

@EqualsAndHashCode(callSuper = true)
final class Knight extends Piece {

    private static final long serialVersionUID = -2469412138530237398L;

    Knight(Colors color) {
        super(color);
    }

    @Override
    public boolean performActualCheckIfItCanMoveThere(Coordinates from, Coordinates to, boolean capturing) {
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
