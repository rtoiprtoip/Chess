package model.history;

import controller.domain.Coordinates;
import controller.domain.PieceKind;
import model.gameState.GameState;

import java.io.Serializable;
import java.util.List;

public interface MoveHistory extends Serializable {
    
    void push(GameState gameState, Coordinates moveFrom, Coordinates moveTo, PieceKind promotionChoice);
    
    GameState pop();
    
    List<String> getMoveLog();
    
    GameState peek();
}
