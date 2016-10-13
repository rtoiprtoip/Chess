package chess.model.pieces;

import chess.domain.Coordinates;
import chess.domain.PieceKind;
import lombok.EqualsAndHashCode;
import chess.domain.Colors;

@EqualsAndHashCode(callSuper = true)
final class Bishop extends Piece {
    
    Bishop(Colors color) {
        super(color, PieceKind.BISHOP);
    }
    
    @Override
    public boolean checkIfCouldMoveThereOnEmptyBoard(Coordinates from, Coordinates to, boolean capturing) {
        int x1 = from.getCol(), x2 = to.getCol(), y1 = from.getRow(), y2 = to.getRow();
        return Math.abs(x1 - x2) == Math.abs(y1 - y2);
        
    }
    
    @Override
    public String toString() {
        return ("" + color + "_bishop").toLowerCase();
    }
    
}
