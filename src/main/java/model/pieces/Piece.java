package model.pieces;

import java.io.Serializable;
import java.util.*;

import controller.Coordinates;
import lombok.*;
import model.Colors;
import model.PromotionException;
import model.SpecialMoveException;
import model.TwoFieldsPawnAdvanceException;

@EqualsAndHashCode(exclude = "hasMoved")
public abstract class Piece implements Serializable {

	private static final long serialVersionUID = -883737137096650294L;

	@Getter
	protected final model.Colors color;

	@Getter
	@Setter
	protected boolean hasMoved = false; // Need this for castling

	/**
	 * @param from
	 *            Starting position of the piece
	 * @param to
	 *            Goal of the piece
	 * @return Path on the board, if the piece could move there on an 'empty'
	 *         board, i.e. it doesn't check for any other pieces that would
	 *         stand in the way (but it does check if something is on the ''to''
	 *         field), or if this move would expose king to check. If the piece
	 *         can't move there, it returns null.
	 * @throws model.SpecialMoveException
	 *             if this move is possible as a special move, i.e. castling,
	 *             promotion or en passant
	 * @throws IllegalArgumentException
	 *             if either of the fields is null or if from == to
	 */
	public final List<Coordinates> canMoveThere(Coordinates from, Coordinates to, boolean capturing)
			throws SpecialMoveException {
		if (from == null || to == null)
			throw new IllegalArgumentException();
		if (from.equals(to))
			return null;
		if (performActualCheckIfItCanMoveThere(from, to, capturing))
			return getPath(from, to);
		else
			return null;
	}

	protected abstract boolean performActualCheckIfItCanMoveThere(Coordinates from, Coordinates to, boolean capturing)
			throws model.SpecialMoveException;

	protected List<Coordinates> getPath(Coordinates from, Coordinates to) {
		Coordinates dir = Coordinates.getDir(from, to);
		List<Coordinates> ret = new LinkedList<>();
		for (Coordinates c = from.plus(dir); !c.equals(to); c = c.plus(dir))
			ret.add(c);
		return ret;
	}

	Piece(model.Colors color) {
		if (color == null)
			throw new IllegalArgumentException();
		this.color = color;
	}

	public boolean isKing() {
		return false;
	}

	public void move(Coordinates to) throws PromotionException, TwoFieldsPawnAdvanceException {
		hasMoved = true;
	}

	public static Piece produce(Colors color, String type) {
		switch (type) {
		case "rook":
			return new Rook(color);
		case "knight":
			return new Knight(color);
		case "bishop":
			return new Bishop(color);
		case "queen":
			return new Queen(color);
		case "king":
			return new King(color);
		case "pawn":
			return new Pawn(color);
		default:
			throw new IllegalArgumentException();
		}
	}

	// for the @HashCodeAndEquals to work properly
	private Class<? extends Piece> cls = getClass();

}
