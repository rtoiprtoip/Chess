package controller.domain;

import lombok.Getter;

/**
 * Represents coordinates on the chessboard. This class is immutable and there's no public constructor. The static
 * factory method returns cached objects, so it's safe to compare instances with == operator.
 */
public final class Coordinates {
    
    private static final Coordinates[][] cache;
    @Getter
    private final int col, row;
    
    static {
        // should also contain negative coordinates for Coordinates.dir() to work correctly
        cache = new Coordinates[10][10];
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                cache[i][j] = new Coordinates(i - 1, j - 1);
            }
        }
    }
    
    public static Coordinates of(int col, int row) {
        if (col < -1 || col > 8 || row < -1 || row > 8) {
            throw new IllegalArgumentException("Coordinated exceeding boundaries: col = " + col + ", row = " + row);
        }
        return cache[col + 1][row + 1];
    }
    
    public static Coordinates getDir(Coordinates from, Coordinates to) {
        int dirX = to.getCol() - from.getCol();
        if (dirX != 0) {
            dirX /= Math.abs(dirX);
        }
        int dirY = to.getRow() - from.getRow();
        if (dirY != 0) {
            dirY /= Math.abs(dirY);
        }
        return Coordinates.of(dirX, dirY);
    }
    
    private Coordinates(int i, int j) {
        if (Coordinates.of(i, j) != null) {
            throw new AssertionError("Tried to duplicate Coordinates: col = " + i + ", row = " + j);
        }
        col = i;
        row = j;
    }
    
    public Coordinates plus(Coordinates dir) {
        return Coordinates.of(col + dir.col, row + dir.row);
    }
    
    @Override
    public String toString() {
        return Character.toString((char) ('a' - 1 + col)) + row;
    }
}
