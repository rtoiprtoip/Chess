package model.domain.pieces;

import controller.Coordinates;
import lombok.EqualsAndHashCode;
import model.domain.Colors;
import controller.exceptions.EnPassantException;
import controller.exceptions.PromotionException;
import controller.exceptions.SpecialMoveException;
import controller.exceptions.TwoFieldsPawnAdvanceException;

@EqualsAndHashCode(callSuper = true)
final class Pawn extends Piece {

	private static final long serialVersionUID = -7210606552037995958L;

	Pawn(Colors color) {
		super(color);
	}

	@Override
	@SuppressWarnings("SimplifiableIfStatement")
	public boolean performActualCheckIfItCanMoveThere(Coordinates from, Coordinates to, boolean capturing)
			throws SpecialMoveException {
		final int x1 = from.getCol(), x2 = to.getCol(), y1 = from.getRow(), y2 = to.getRow();
		final int direction = color == Colors.WHITE ? 1 : -1;
		if (x1 == x2)
			if (capturing)
				return false;
			else if (y2 - y1 == direction)
				return true;
			else if (y2 - y1 == 2 * direction)
                return !hasMoved;
			else
				return false;
		else if (Math.abs(x1 - x2) == 1)
			if (y2 - y1 != direction)
				return false;
			else if (capturing)
				return true;
			else if (y1 == (color == Colors.WHITE ? 5 : 4))
				throw new EnPassantException();
			else
				return false;
		else
			return false;
	}

	@Override
	public void move(Coordinates to) throws PromotionException, TwoFieldsPawnAdvanceException {
		try {
			if (to.getRow() == (color == Colors.WHITE ? 8 : 1))
				throw new PromotionException();
			if (!hasMoved && to.getRow() == (color == Colors.WHITE ? 4 : 5))
				throw new TwoFieldsPawnAdvanceException();
		} finally {
			super.move(to);
		}
	}

	@Override
	public String toString() {
		return ("" + color + "_pawn").toLowerCase();
	}

}
