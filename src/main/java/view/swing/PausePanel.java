package view.swing;

import javax.swing.*;
import java.awt.*;

class PausePanel extends JPanel {
    
    final JButton resumeButton;
    
    PausePanel() {
        super();
        this.setLayout(new BorderLayout());
        resumeButton = new JButton("Resume");
        this.add(resumeButton, BorderLayout.PAGE_END);
    }
}
