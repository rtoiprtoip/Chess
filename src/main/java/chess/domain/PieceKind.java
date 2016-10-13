package chess.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumerates piece kinds. Class chess.model.pieces.Piece can't be transformed to enum, because it can't be a singleton
 */
@AllArgsConstructor
public enum PieceKind {
    PAWN("Pawn"),
    ROOK("Rook"),
    KNIGHT("Knight"),
    BISHOP("Bishop"),
    QUEEN("Queen"),
    KING("King");
    
    @Getter
    private final String name;
    
}
