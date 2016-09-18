package model.pieces;

import controller.Coordinates;
import lombok.EqualsAndHashCode;
import model.Colors;

@EqualsAndHashCode(callSuper = true)
final class Bishop extends Piece {

	private static final long serialVersionUID = 3711254265228445652L;

	Bishop(Colors color) {
		super(color);
	}

	@Override
	public boolean performActualCheckIfItCanMoveThere(Coordinates from, Coordinates to, boolean capturing) {
		int x1 = from.getCol(), x2 = to.getCol(), y1 = from.getRow(), y2 = to.getRow();
        return Math.abs(x1 - x2) == Math.abs(y1 - y2);

    }

	@Override
	public String toString() {
		return ("" + color + "_bishop").toLowerCase();
	}

}
