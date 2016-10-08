package controller.domain;

import lombok.Value;

@Value
public class Coordinates {
    
    int col, row;
    
    public static Coordinates getDir(Coordinates from, Coordinates to) {
        int dirX = to.getCol() - from.getCol();
        if (dirX != 0) {
            dirX /= Math.abs(dirX);
        }
        int dirY = to.getRow() - from.getRow();
        if (dirY != 0) {
            dirY /= Math.abs(dirY);
        }
        return new Coordinates(dirX, dirY);
    }
    
    public Coordinates plus(Coordinates dir) {
        return new Coordinates(col + dir.col, row + dir.row);
    }
    
    @Override
    public String toString() {
        return Character.toString((char) ('a' - 1 + col)) + row;
    }
}
