package model.pieces;

import controller.Coordinates;
import lombok.EqualsAndHashCode;
import model.Colors;

@EqualsAndHashCode(callSuper = true)
final class Rook extends Piece {

	private static final long serialVersionUID = 5200867402070785492L;

	Rook(Colors color) {
		super(color);
	}

	@Override
	public boolean performActualCheckIfItCanMoveThere(Coordinates from, Coordinates to, boolean capturing) {
		int x1 = from.getCol(), x2 = to.getCol(), y1 = from.getRow(), y2 = to.getRow();
        return x1 == x2 || y1 == y2;

    }

	@Override
	public String toString() {
		return ("" + color + "_rook").toLowerCase();
	}

}
