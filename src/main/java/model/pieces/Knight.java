package model.pieces;

import java.util.*;

import controller.Coordinates;
import model.Colors;

final class Knight extends Piece {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2469412138530237398L;

	Knight(Colors color) {
		super(color);
	}

	@Override
	public boolean performActualCheckIfItCanMoveThere(Coordinates from, Coordinates to, boolean capturing) {
		int x1 = from.getCol(), x2 = to.getCol(), y1 = from.getRow(), y2 = to.getRow();
		int d1 = Math.abs(x1 - x2), d2 = Math.abs(y1 - y2);
		if (d1 == 1 && d2 == 2)
			return true;
		else if (d1 == 2 && d2 == 1)
			return true;
		else
			return false;
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
