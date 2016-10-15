package chess.view.swing;

import chess.domain.Colors;
import chess.domain.Coordinates;
import chess.domain.PieceKind;
import chess.domain.Time;
import chess.view.View;
import chess.view.image.ImageLoadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.util.function.BiConsumer;

@Service
public class SwingView implements View {
    
    private JFrame mainFrame;
    private final MainPanel mainPanel;
    private final SettingsPanel settingsPanel;
    private final PausePanel pauseScreen = new PausePanel();
    private final LegalStuffDisplayer legalStuffPanel = new LegalStuffDisplayer();
    private BiConsumer<Coordinates, Coordinates> moveConsumer;
    
    private final ImageLoadingService imageService;
    
    @Autowired
    public SwingView(MainPanel mainPanel, SettingsPanel settingsPanel, ImageLoadingService imageService) {
        this.settingsPanel = settingsPanel;
        this.imageService = imageService;
        this.mainPanel = mainPanel;
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }
    
    private void createAndShowGUI() {
        mainFrame = new JFrame("Chess");
        mainFrame.setVisible(true);
        mainFrame.setSize(500, 500);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setResizable(false);
        mainFrame.pack();
        
        settingsPanel.setPreferredSize(mainFrame.getContentPane().getSize());
        pauseScreen.setPreferredSize(mainFrame.getContentPane().getSize());
        legalStuffPanel.setPreferredSize(mainFrame.getContentPane().getSize());
        
        mainPanel.settingsButton.addActionListener(e -> displaySettingsScreen());
        mainPanel.pauseButton.addActionListener(e -> displayPauseScreen());
        mainPanel.legalStuffButton.addActionListener(e -> displayLegalStuff());
        
        ActionListener mainViewDisplayer = e -> displayMainView();
        
        settingsPanel.okButton.addActionListener(mainViewDisplayer);
        pauseScreen.resumeButton.addActionListener(mainViewDisplayer);
        legalStuffPanel.okButton.addActionListener(mainViewDisplayer);
        
        addClicksHandlerToFields();
    }
    
    @Override
    public void addSettingsListeners(ActionListener actionListener, VetoableChangeListener vcl,
                                     PropertyChangeListener pcl) {
        mainPanel.settingsButton.addActionListener(actionListener);
        settingsPanel.okButton.addActionListener(actionListener);
        settingsPanel.addChangeListeners(vcl, pcl);
    }
    
    @Override
    public void addNewGameStarter(ActionListener actionListener) {
        mainPanel.newGameButton.addActionListener(actionListener);
    }
    
    @Override
    public void addGameLoader(ActionListener actionListener) {
        mainPanel.loadButton.addActionListener(actionListener);
    }
    
    @Override
    public void addGameSaver(ActionListener actionListener) {
        mainPanel.saveButton.addActionListener(actionListener);
    }
    
    @Override
    public void addPauseListener(ActionListener actionListener) {
        mainPanel.pauseButton.addActionListener(actionListener);
        pauseScreen.resumeButton.addActionListener(actionListener);
    }
    
    @Override
    public void addEndGameListener(ActionListener actionListener) {
        mainPanel.endGameButton.addActionListener(actionListener);
    }
    
    @Override
    public void addLegalStuffDisplayer(ActionListener actionListener) {
        mainPanel.legalStuffButton.addActionListener(actionListener);
        legalStuffPanel.okButton.addActionListener(actionListener);
    }
    
    @Override
    public void setIconAt(Coordinates c, PieceKind pieceKind, Colors color) {
        Image image = imageService.getPieceImageFromResources(pieceKind, color);
        mainPanel.setIconAt(c, image);
    }
    
    @Override
    public void addMoveHandler(BiConsumer<Coordinates, Coordinates> biConsumer) {
        moveConsumer = biConsumer;
    }
    
    @Override
    public void move(Coordinates arg0, Coordinates arg1) {
        mainPanel.moveIcon(arg0, arg1);
    }
    
    @Override
    public void clearGUI() {
        mainPanel.clear();
        displayMainView();
    }
    
    @Override
    public void castle(Coordinates from, Coordinates to) {
        Coordinates dir = Coordinates.getDir(from, to);
        Coordinates rookPos = Coordinates.of(dir.getCol() > 0 ? 8 : 1, from.getRow());
        mainPanel.moveIcon(from, to);
        mainPanel.moveIcon(rookPos, from.plus(dir));
    }
    
    @Override
    public void enPassant(Coordinates from, Coordinates to) {
        mainPanel.moveIcon(from, to);
        mainPanel.setIconAt(Coordinates.of(to.getCol(), from.getRow()), null);
    }
    
    @Override
    public PieceKind getPromotionChoice(Colors whoseMove) {
        return mainPanel.getPromotionChoice(whoseMove);
    }
    
    @Override
    public void promote(Coordinates moveFrom, Coordinates moveTo, PieceKind promotionChoice, Colors color) {
        Image image = imageService.getPieceImageFromResources(promotionChoice, color);
        mainPanel.promote(moveFrom, moveTo, image);
    }
    
    @Override
    public void setTime(Colors color, Time time) {
        if (mainPanel == null) {
            return;
        }
        
        if (color == Colors.WHITE) {
            if (mainPanel.whiteTimeDisplayer != null) {
                mainPanel.whiteTimeDisplayer.setText(time.toString());
            }
        } else if (mainPanel.blackTimeDisplayer != null) {
            mainPanel.blackTimeDisplayer.setText(time.toString());
        }
    }
    
    @Override
    public File getFileToReadFrom() {
        JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
        while (true) {
            int ok = fc.showOpenDialog(mainFrame);
            if (ok == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (file.exists() && file.isFile()) {
                    return file;
                }
            }
            if (ok == JFileChooser.CANCEL_OPTION) {
                return null;
            }
        }
    }
    
    @Override
    public File getFileToSaveIn() {
        JFileChooser fc = new JFileChooser(System.getProperty("user.home")) {
            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        int ok = fc.showSaveDialog(mainFrame);
        if (ok == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        }
        return null;
    }
    
    @Override
    public void addRevertMoveListener(ActionListener actionListener) {
        mainPanel.revertMoveButton.addActionListener(actionListener);
    }
    
    @Override
    public void addLogSaver(ActionListener actionListener) {
        mainPanel.saveLogButton.addActionListener(actionListener);
    }
    
    @Override
    public void disableOrEnableButtonsCharacteristicForGameInProgressEqualTo(boolean gameInProgress) {
        mainPanel.disableOrEnableButtonsCharacteristicForGameInProgress(gameInProgress);
    }
    
    @Override
    public void removePromotionChoicePanelIfExists() {
        mainPanel.removePromotionChoicePanelIfExists();
        
    }
    
    private void displayPauseScreen() {
        mainFrame.getContentPane().remove(mainPanel);
        mainFrame.getContentPane().add(pauseScreen);
        mainFrame.repaint();
        mainFrame.pack();
    }
    
    private void displaySettingsScreen() {
        settingsPanel.setPreferredSize(mainFrame.getContentPane().getSize());
        mainFrame.getContentPane().remove(mainPanel);
        mainFrame.getContentPane().add(settingsPanel);
        mainFrame.repaint();
        mainFrame.pack();
    }
    
    private void addClicksHandlerToFields() {
        ActionListener clicksHandler = new ActionListener() {
            
            Field fieldOne = null;
            Field fieldTwo = null;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                assert fieldTwo == null;
                Field fieldClicked = (Field) e.getSource();
                if (fieldOne == null) {
                    fieldOne = fieldClicked;
                    fieldClicked.setBackground(Color.LIGHT_GRAY);
                } else {
                    fieldTwo = fieldClicked;
                    moveConsumer.accept(fieldOne.getCoordinates(), fieldTwo.getCoordinates());
                    fieldOne.setBackground(fieldOne.naturalColor());
                    fieldOne = fieldTwo = null;
                }
            }
        };
        
        for (int i = 1; i <= 8; ++i) {
            for (int j = 1; j <= 8; ++j) {
                mainPanel.fields[i][j].addActionListener(clicksHandler);
            }
        }
    }
    
    private void displayLegalStuff() {
        mainFrame.getContentPane().remove(mainPanel);
        mainFrame.getContentPane().add(legalStuffPanel);
        mainFrame.repaint();
        mainFrame.pack();
    }
    
    private void displayMainView() {
        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.repaint();
        mainPanel.repaint();
        mainFrame.pack();
    }
    
}
