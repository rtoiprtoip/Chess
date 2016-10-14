package chess.view.swing;

import javax.swing.*;
import java.awt.*;

class ExtendedBoard extends JLayeredPane {
    
    private final static int TOP_LEVEL = 2, BOTTOM_LEVEL = 1;
    
    void addToTopLayer(Component component) {
        if (getComponentCountInLayer(TOP_LEVEL) > 0) {
            throw new IllegalStateException("Top layer is already occupied");
        }
        this.add(component, TOP_LEVEL, 0);
    }
    
    void removeComponentFromTopLayer() {
        Component topLayerComponent = getFromLayer(TOP_LEVEL);
        if (topLayerComponent == null) {
            throw new IllegalStateException("Top layer is empty");
        } else {
            remove(topLayerComponent);
        }
    }
    
    void addToBottomLayer(Component component) {
        if (getComponentCountInLayer(BOTTOM_LEVEL) > 0) {
            throw new IllegalStateException("Bottom layer is already occupied");
        }
        this.add(component, BOTTOM_LEVEL, 0);
    }
    
    @Override
    public void repaint() {
        Component topLevelComponent = getFromLayer(TOP_LEVEL);
        Component bottomLevelComponent = getFromLayer(BOTTOM_LEVEL);
        
        if (topLevelComponent != null && bottomLevelComponent != null) {
            remove(topLevelComponent);
            remove(bottomLevelComponent);
            addToTopLayer(topLevelComponent);
            addToBottomLayer(bottomLevelComponent);
        }
        
        super.repaint();
    }
    
    
    void removeComponentFromTopLayerIfExists() {
        Component topLevelComponent = getFromLayer(TOP_LEVEL);
        if (topLevelComponent != null) {
            remove(topLevelComponent);
            repaint();
        }
    }
    
    private Component getFromLayer(int layer) {
        Component[] component = getComponentsInLayer(layer);
        if (component.length == 0) {
            return null;
        } else if (component.length > 1) {
            throw new AssertionError();
        } else {
            return component[0];
        }
    }
}
