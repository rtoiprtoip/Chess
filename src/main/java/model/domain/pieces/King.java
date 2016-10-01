package model.domain.pieces;

import controller.Coordinates;
import lombok.EqualsAndHashCode;
import controller.exceptions.CastlingException;
import model.domain.Colors;
import controller.exceptions.SpecialMoveException;

@EqualsAndHashCode(callSuper = true)
final class King extends Piece {

	private static final long serialVersionUID = -2415467353802390479L;

	King(Colors color) {
		super(color);
	}

	@Override
	public boolean performActualCheckIfItCanMoveThere(Coordinates from, Coordinates to, boolean capturing)
			throws SpecialMoveException {
		int x1 = from.getCol(), x2 = to.getCol(), y1 = from.getRow(), y2 = to.getRow();
		if (Math.abs(x1 - x2) <= 1 && Math.abs(y1 - y2) <= 1)
			return true;

		if (y1 == y2 && Math.abs(x1 - x2) == 2 && !hasMoved)
			throw new CastlingException();

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
