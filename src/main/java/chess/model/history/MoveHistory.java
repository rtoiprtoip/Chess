package chess.model.history;

import chess.domain.Coordinates;
import chess.domain.PieceKind;
import chess.model.gameState.GameState;

import java.io.Serializable;
import java.util.List;

public interface MoveHistory extends Serializable {
    
    void push(GameState gameState, Coordinates moveFrom, Coordinates moveTo, PieceKind promotionChoice);
    
    @SuppressWarnings("UnusedReturnValue")
    GameState pop();
    
    List<String> getMoveLog();
    
    GameState peek();
}
