package chess.view.image;

import chess.domain.Colors;
import chess.domain.PieceKind;

import java.awt.*;

public interface ImageLoadingService {
    
    Image getPieceImageFromResources(PieceKind kind, Colors color);
    
}
