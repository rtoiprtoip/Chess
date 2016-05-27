package model.pieces;

import controller.Coordinates;
import model.Colors;

final class Bishop extends Piece {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3711254265228445652L;

	Bishop(Colors color) {
		super(color);
	}

	@Override
	public boolean performActualCheckIfItCanMoveThere(Coordinates from, Coordinates to, boolean capturing) {
		int x1 = from.getCol(), x2 = to.getCol(), y1 = from.getRow(), y2 = to.getRow();
		if (!(Math.abs(x1 - x2) == Math.abs(y1 - y2)))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return ("" + color + "_bishop").toLowerCase();
	}

}
