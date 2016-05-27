package view.swing;

import java.awt.Color;

import javax.swing.JButton;

import controller.Coordinates;
import lombok.Getter;

@SuppressWarnings("serial")
class Field extends JButton {
	@Getter
	final int row;
	@Getter
	final int col;

	Field(int x, int y) {
		super();
		row = x;
		col = y;
	}

	Coordinates getCoordinates() {
		return new Coordinates(col, row);
	}

	Color naturalColor() {
		return row % 2 == col % 2 ? new Color(153, 76, 0) : new Color(204, 102, 0);
	}

	@Override
	public String toString() {
		return Character.toString((char) ('a' + col - 1)) + row;
	}
}
