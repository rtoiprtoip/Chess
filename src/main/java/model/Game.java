package model;

import java.io.Serializable;
import java.util.*;

import controller.Coordinates;
import lombok.*;
import model.pieces.Piece;

public class Game implements Serializable {

	public Game() {
		fields = new Piece[9][9];
		for (int i = 0; i <= 8; ++i)
			for (int j = 0; j <= 8; ++j)
				fields[i][j] = null;
	}

	public void newGame() {
		fields[1][1] = Piece.produce(Colors.WHITE, "rook");
		fields[2][1] = Piece.produce(Colors.WHITE, "knight");
		fields[3][1] = Piece.produce(Colors.WHITE, "bishop");
		fields[4][1] = Piece.produce(Colors.WHITE, "queen");
		fields[5][1] = Piece.produce(Colors.WHITE, "king");
		fields[6][1] = Piece.produce(Colors.WHITE, "bishop");
		fields[7][1] = Piece.produce(Colors.WHITE, "knight");
		fields[8][1] = Piece.produce(Colors.WHITE, "rook");

		fields[1][8] = Piece.produce(Colors.BLACK, "rook");
		fields[2][8] = Piece.produce(Colors.BLACK, "knight");
		fields[3][8] = Piece.produce(Colors.BLACK, "bishop");
		fields[4][8] = Piece.produce(Colors.BLACK, "queen");
		fields[5][8] = Piece.produce(Colors.BLACK, "king");
		fields[6][8] = Piece.produce(Colors.BLACK, "bishop");
		fields[7][8] = Piece.produce(Colors.BLACK, "knight");
		fields[8][8] = Piece.produce(Colors.BLACK, "rook");

		for (int i = 1; i < 9; ++i) {
			fields[i][2] = Piece.produce(Colors.WHITE, "pawn");
			fields[i][7] = Piece.produce(Colors.BLACK, "pawn");
			for (int j = 3; j < 7; ++j)
				fields[i][j] = null;
		}

		whoseMove = Colors.WHITE;
		whiteTime = new Time(gameTime);
		blackTime = new Time(gameTime);

		if (timeCounter == null)
			timeCounter = new TimeCounter();
		if (!timeCounter.isAlive())
			timeCounter.start();
	}

	public boolean isThisValidMove(Coordinates from, Coordinates to) throws SpecialMoveException {
		try {
			// check if user wants to move an existing piece owned by him
			if (getPieceAt(from) == null || getPieceAt(from).getColor() != whoseMove)
				return false;

			// check if user tries to capture his own piece
			if (getPieceAt(from).getColor() != whoseMove)
				return false;

			if (!isThisValidMoveForgetCheckAndTurn(from, to))
				return false;

			// check if this move will expose king to check
			// save current state
			Piece one = getPieceAt(from);
			Piece two = getPieceAt(to);
			try {
				// perform a virtual move
				setPieceAt(to, one);
				setPieceAt(from, null);
				if (checkIfIsChecked(whoseMove))
					return false;
			} finally {
				setPieceAt(from, one);
				setPieceAt(to, two);
			}

			return true;
		} catch (EnPassantException e) {
			if (authorizeEnPassant(from, to)) {
				throw e;
			} else
				return false;
		} catch (CastlingException e) {
			if (authorizeCastling(from, to)) {
				throw e;
			} else
				return false;
		} catch (PromotionException e) {
			// check if this move will expose king to check
			// save current state
			Piece one = getPieceAt(from);
			Piece two = getPieceAt(to);
			try {
				// perform a virtual move
				setPieceAt(to, one);
				setPieceAt(from, null);
				if (checkIfIsChecked(whoseMove))
					return false;
			} finally {
				setPieceAt(from, one);
				setPieceAt(to, two);
			}

			return true;
		}
	}

	private boolean isThisValidMoveForgetCheckAndTurn(Coordinates from1, Coordinates to1) throws SpecialMoveException {
		if (getPieceAt(from1) == null)
			return false;
		if (getPieceAt(to1) != null && getPieceAt(to1).getColor() == getPieceAt(from1).getColor())
			return false;
		List<Coordinates> path = getPieceAt(from1).canMoveThere(from1, to1, getPieceAt(to1) != null);
		if (path == null)
			return false;
		for (Coordinates c : path)
			if (getPieceAt(c) != null)
				return false;
		return true;
	}

	private boolean authorizeCastling(Coordinates from, Coordinates to) {
		Colors color = getPieceAt(from).getColor();
		assert getPieceAt(from).isKing();
		Coordinates dir = Coordinates.getDir(from, to);
		Coordinates rookPos = new Coordinates(dir.getCol() > 0 ? 8 : 1, from.getRow());
		if (getPieceAt(rookPos) == null || getPieceAt(rookPos).isHasMoved())
			return false;
		if (checkIfIsChecked(color))
			return false;
		for (Coordinates c = from.plus(dir); !c.equals(rookPos); c = c.plus(dir)) {
			if (getPieceAt(c) != null)
				return false;
			Piece king = getPieceAt(from);
			try {
				// perform a virtual move
				setPieceAt(c, king);
				setPieceAt(from, null);
				if (checkIfIsChecked(color))
					return false;
			} finally {
				setPieceAt(c, null);
				setPieceAt(from, king);
			}
		}
		return true;
	}

	private boolean authorizeEnPassant(Coordinates from, Coordinates to) {
		if (lastMoveWasTwoFieldPawnAdvanceAtColumn == null)
			return false;
		if (!lastMoveWasTwoFieldPawnAdvanceAtColumn.equals(to.getCol()))
			return false;

		Piece movingPawn = getPieceAt(from);
		Coordinates capturedPawnCoordinates = new Coordinates(to.getCol(), from.getRow());
		Piece capturedPawn = getPieceAt(capturedPawnCoordinates);

		try {
			// perform a virtual move
			setPieceAt(to, movingPawn);
			setPieceAt(from, null);
			setPieceAt(capturedPawnCoordinates, null);
			if (checkIfIsChecked(whoseMove))
				return false;
		} finally {
			setPieceAt(to, null);
			setPieceAt(from, movingPawn);
			setPieceAt(capturedPawnCoordinates, capturedPawn);
		}
		return true;
	}

	public void castle(Coordinates from, Coordinates to) {
		Coordinates dir = Coordinates.getDir(from, to);
		Coordinates rookPos = new Coordinates(dir.getCol() > 0 ? 8 : 1, from.getRow());
		try {
			getPieceAt(from).move(to);
			getPieceAt(rookPos).move(from.plus(dir));
		} catch (PromotionException | TwoFieldsPawnAdvanceException e) {
			assert false;
		}
		setPieceAt(to, getPieceAt(from));
		setPieceAt(from.plus(dir), getPieceAt(rookPos));
		setPieceAt(from, null);
		setPieceAt(rookPos, null);
		lastMoveWasTwoFieldPawnAdvanceAtColumn = null;
		moveCompleteAction();
	}

	public void enPassant(Coordinates from, Coordinates to) {
		setPieceAt(to, getPieceAt(from));
		setPieceAt(from, null);
		setPieceAt(new Coordinates(to.getCol(), from.getRow()), null);
		lastMoveWasTwoFieldPawnAdvanceAtColumn = null;
		moveCompleteAction();
	}

	public void promote(Coordinates moveFrom, Coordinates moveTo, String pieceChosen) {
		setPieceAt(moveTo, Piece.produce(getPieceAt(moveFrom).getColor(), pieceChosen));
		setPieceAt(moveFrom, null);
		lastMoveWasTwoFieldPawnAdvanceAtColumn = null;
		moveCompleteAction();
	}

	public void move(Coordinates from, Coordinates to) throws PromotionException {
		lastMoveWasTwoFieldPawnAdvanceAtColumn = null;
		try {
			getPieceAt(from).move(to);
		} catch (TwoFieldsPawnAdvanceException e) {
			lastMoveWasTwoFieldPawnAdvanceAtColumn = to.getCol();
		}
		setPieceAt(to, getPieceAt(from));
		setPieceAt(from, null);
		moveCompleteAction();
	}

	public Piece getPieceAt(Coordinates c) {
		return fields[c.getCol()][c.getRow()];
	}

	public void setPieceAt(Coordinates c, Piece p) {// just for setting up tests
		fields[c.getCol()][c.getRow()] = p;
	}

	public void startOrResume() {
		isPaused = false;

		if (timeCounter == null)
			timeCounter = new TimeCounter();
		if (!timeCounter.isAlive())
			timeCounter.start();

		synchronized (timeCounter) {
			timeCounter.notifyAll();
		}
	}

	public void endGame() {
		setPaused(true);
		for (int i = 0; i <= 8; ++i)
			for (int j = 0; j <= 8; ++j)
				fields[i][j] = null;
	}

	private void moveCompleteAction() {
		(whoseMove == Colors.WHITE ? whiteTime : blackTime).add(timeAdded);
		whoseMove = (whoseMove == Colors.WHITE ? Colors.BLACK : Colors.WHITE);
		synchronized (timeCounter) {
			timeCounter.notifyAll();
		}
	}

	private boolean checkIfIsChecked(Colors c) {
		// find the king
		Coordinates kingPos = null;
		for (int i = 1; i <= 8; ++i)
			for (int j = 1; j <= 8; ++j)
				if (fields[i][j] != null && fields[i][j].isKing() && fields[i][j].getColor() == c)
					kingPos = new Coordinates(i, j);
		assert kingPos != null;

		// check for check
		for (int i = 1; i <= 8; ++i)
			for (int j = 1; j <= 8; ++j)
				try {
					if (fields[i][j] != null && fields[i][j].getColor() != c
							&& isThisValidMoveForgetCheckAndTurn(new Coordinates(i, j), kingPos))
						return true;
				} catch (PromotionException e) {
					return true;
				} catch (SpecialMoveException e) {
					return false;
				}
		return false;
	}

	public String getTime(String color) {
		switch (color) {
			case "white":
				return whiteTime.toString();
			case "black":
				return blackTime.toString();
			default:
				throw new IllegalArgumentException();
		}
	}

	public void setGameTime(int minutes, int seconds) {
		gameTime = new Time(minutes, seconds);
	}

	public void setTimeAddedPerMove(int timeAddedInSeconds) {
		timeAdded = new Time(0, timeAddedInSeconds);
	}

	public void setPaused(boolean b) {
		isPaused = b;
		synchronized (timeCounter) {
			timeCounter.notifyAll();
		}
	}

	@Deprecated //for testing only
	public void setWhoseMove(Colors color) {
		whoseMove = color;
	}

	private class TimeCounter extends Thread {
		public TimeCounter() {
			super();
			setDaemon(true);
		}

		@Override
		public void run() {
			synchronized (this) {
				while (true) {
					while (isPaused)
						try {
							this.wait();
						} catch (InterruptedException e) {
						}
					Time timeToChange = (whoseMove == Colors.WHITE ? whiteTime : blackTime);
					timeToChange.decrement();
					try {
						this.wait(Time.precision.toMillis());
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	private static final long serialVersionUID = -1017537983982485719L;
	private final Piece[][] fields;
	@Getter
	private transient boolean isPaused;

	@Getter
	private Colors whoseMove;

	private Integer lastMoveWasTwoFieldPawnAdvanceAtColumn = null;

	private Time gameTime = new Time(10, 0);// time per one player
	private Time timeAdded = new Time();
	private transient TimeCounter timeCounter = new TimeCounter();
	private Time whiteTime = new Time(), blackTime = new Time();
}
