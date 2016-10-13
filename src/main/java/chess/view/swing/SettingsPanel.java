package chess.view.swing;

import chess.domain.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.*;

@Service
class SettingsPanel extends JPanel {
    
    @SuppressWarnings("FieldCanBeLocal")
    private final JPanel optionsPanel;
    final JButton okButton;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final VetoableChangeSupport vcs = new VetoableChangeSupport(this);
    
    @Autowired
    SettingsPanel(Time defaultGameTime) {
        super(new BorderLayout());
        
        int x = 20, y = 6;
        optionsPanel = new JPanel(new GridLayout(x, y));
        
        optionsPanel.add(new JLabel("time per player (mm:ss):"));
        SettingsInputField timePerPlayer = new SettingsInputField("gameTime", defaultGameTime.toString());
        timePerPlayer.setHorizontalAlignment(JTextField.CENTER);
        optionsPanel.add(timePerPlayer);
        
        for (int i = 0; i < 4; ++i) {
            optionsPanel.add(new JLabel());
        }
        
        optionsPanel.add(new JLabel("time added per move (s)"));
        SettingsInputField timeAddedPerMove = new SettingsInputField("timeAdded", "00");
        timeAddedPerMove.setHorizontalAlignment(JTextField.CENTER);
        optionsPanel.add(timeAddedPerMove);
        
        while (optionsPanel.getComponentCount() != x * y) {
            optionsPanel.add(new JLabel(""));
        }
        
        this.add(optionsPanel, BorderLayout.CENTER);
        
        okButton = new JButton("OK");
        
        this.add(okButton, BorderLayout.PAGE_END);
    }
    
    void addChangeListeners(VetoableChangeListener vcl, PropertyChangeListener pcl) {
        vcs.addVetoableChangeListener(vcl);
        pcs.addPropertyChangeListener(pcl);
    }
    
    private class SettingsInputField extends JTextField implements FocusListener {
        
        private String propertyName = "";
        private final String value = "";
        
        public SettingsInputField(String propertyName, String defaultText) {
            super(defaultText);
            this.addFocusListener(this);
            this.propertyName = propertyName;
        }
        
        @Override
        public void focusGained(FocusEvent arg0) {
        }
        
        @Override
        public void focusLost(FocusEvent arg0) {
            String newValue = ((SettingsInputField) arg0.getSource()).getText();
            try {
                vcs.fireVetoableChange(propertyName, value, newValue);
            } catch (PropertyVetoException e) {
                this.setBackground(Color.RED);
                okButton.setEnabled(false);
                return;
            }
            pcs.firePropertyChange(propertyName, value, newValue);
            setText(newValue);
            this.setBackground(Color.WHITE);
            okButton.setEnabled(true);
        }
        
    }
}
