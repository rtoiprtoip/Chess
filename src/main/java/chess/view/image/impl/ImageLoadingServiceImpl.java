package chess.view.image.impl;

import chess.domain.Colors;
import chess.domain.PieceKind;
import chess.view.image.ImageLoadingService;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

@Service
public class ImageLoadingServiceImpl implements ImageLoadingService {
    
    private static final Image emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    
    @Override
    public Image getPieceImageFromResources(PieceKind pieceKind, Colors color) {
        if (pieceKind == null && color != null) {
            throw new IllegalArgumentException();
        }
        if (pieceKind != null && color == null) {
            throw new IllegalArgumentException();
        }
    
        if (pieceKind == null) {
            return emptyImage;
        }
        
        String fileName = (color.toString() + "_" + pieceKind.toString() + ".svg").toLowerCase();
        URL url = Thread.currentThread().getContextClassLoader().getResource("icons/" + fileName);
        Image ret = Toolkit.getDefaultToolkit().getImage(url);
        
        if (ret == null) {
            throw new RuntimeException("Image for " + color + ' ' + pieceKind + " not found");
        }
        
        return ret;
    }
}
