package chess.view.swing;

import chess.domain.Colors;
import chess.domain.Coordinates;
import chess.domain.PieceKind;
import chess.view.image.ImageLoadingService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

@org.springframework.stereotype.Component
class MainPanel extends JPanel {
    
    private final ImageLoadingService imageService;
    
    private ExtendedBoard extendedBoard;
    // will use two layers, one for displaying board, second for promotion choice
    
    @SuppressWarnings("FieldCanBeLocal")
    private JPanel board;
    Field[][] fields;
    
    private JToolBar toolbar;
    JButton settingsButton, newGameButton, loadButton, saveButton, saveLogButton, pauseButton, endGameButton,
            legalStuffButton, revertMoveButton;
    
    private JPanel clocks;
    JLabel whiteTimeDisplayer, blackTimeDisplayer;
    
    @Autowired
    MainPanel(ImageLoadingService imageService) {
        this.imageService = imageService;
        initializeBoard();
        initializeToolbar();
        initializeClocks();
        
        this.setLayout(new BorderLayout());
        this.add(toolbar, BorderLayout.PAGE_START);
        this.add(extendedBoard, BorderLayout.LINE_START);
        this.add(clocks, BorderLayout.LINE_END);
    
        ToolTipManager.sharedInstance().setInitialDelay(0);
    }
    
    void setIconAt(Coordinates c, Image image) {
        
        Icon icon;
        if (image != null) {
            icon = scaleImageToFieldSize(image);
        } else {
            icon = emptyIcon;
        }
        
        fields[c.getCol()][c.getRow()].setIcon(icon);
    }
    
    void moveIcon(Coordinates moveFrom, Coordinates moveTo) {
        fieldAt(moveTo).setIcon(fieldAt(moveFrom).getIcon());
        fieldAt(moveFrom).setIcon(emptyIcon);
    }
    
    void clear() {
        for (int i = 1; i <= 8; ++i) {
            for (int j = 1; j <= 8; ++j) {
                fields[i][j].setIcon(emptyIcon);
            }
        }
    }
    
    void promote(Coordinates moveFrom, Coordinates moveTo, Image pieceImage) {
        fieldAt(moveFrom).setIcon(emptyIcon);
        setIconAt(moveTo, pieceImage);
    }
    
    PieceKind getPromotionChoice(Colors color) {
        PromotionHandler ph = new PromotionHandler(color);
        extendedBoard.addToTopLayer(ph);
        try {
            return ph.choose();
        } finally {
            extendedBoard.removeComponentFromTopLayer();
            extendedBoard.repaint();
        }
    }
    
    void disableOrEnableButtonsCharacteristicForGameInProgress(boolean gameInProgress) {
        settingsButton.setEnabled(!gameInProgress);
        if (gameInProgress) {
            settingsButton.setToolTipText("This is only allowed when the game is finished");
        } else {
            settingsButton.setToolTipText(null);
        }
        
        saveButton.setEnabled(gameInProgress);
        saveLogButton.setEnabled(gameInProgress);
        revertMoveButton.setEnabled(gameInProgress);
        endGameButton.setEnabled(gameInProgress);
        pauseButton.setEnabled(gameInProgress);
        if (gameInProgress) {
            saveButton.setToolTipText(null);
            saveLogButton.setToolTipText(null);
            revertMoveButton.setToolTipText(null);
            endGameButton.setToolTipText(null);
            pauseButton.setToolTipText(null);
        } else {
            String message = "This is only allowed while the game is in progress";
            saveButton.setToolTipText(message);
            saveLogButton.setToolTipText(message);
            revertMoveButton.setToolTipText(message);
            endGameButton.setToolTipText(message);
            pauseButton.setToolTipText(message);
        }
    }
    
    void removePromotionChoicePanelIfExists() {
        extendedBoard.removeComponentFromTopLayerIfExists();
    }
    
    @Override
    public void repaint() {
        super.repaint();
        if (extendedBoard != null) {
            extendedBoard.repaint();
        }
    }
    
    private void initializeClocks() {
        int fontSize = (int) (CHESSBOARD_SIZE * 0.045);
        
        clocks = new JPanel(new GridLayout(5, 1));
        clocks.setPreferredSize(new Dimension(TIME_PANEL_WIDTH, CHESSBOARD_SIZE));
        clocks.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));
        
        clocks.add(new JLabel());
        
        whiteTimeDisplayer = new JLabel("", SwingConstants.CENTER);
        whiteTimeDisplayer.setFont(new Font("Verdana", Font.PLAIN, fontSize));
        whiteTimeDisplayer.setOpaque(true);
        whiteTimeDisplayer.setBackground(Color.WHITE);
        clocks.add(whiteTimeDisplayer);
        
        blackTimeDisplayer = new JLabel("", SwingConstants.CENTER);
        blackTimeDisplayer.setFont(new Font("Verdana", Font.PLAIN, fontSize));
        blackTimeDisplayer.setOpaque(true);
        blackTimeDisplayer.setBackground(Color.BLACK);
        blackTimeDisplayer.setForeground(Color.WHITE);
        clocks.add(blackTimeDisplayer);
    }
    
    private void initializeToolbar() {
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        settingsButton = new JButton("Settings");
        toolbar.add(settingsButton);
        
        toolbar.addSeparator();
        
        newGameButton = new JButton("New");
        toolbar.add(newGameButton);
        
        loadButton = new JButton("Load");
        toolbar.add(loadButton);
        
        saveButton = new JButton("Save");
        toolbar.add(saveButton);
        
        saveLogButton = new JButton("Save log");
        toolbar.add(saveLogButton);
        
        toolbar.addSeparator();
        
        pauseButton = new JButton("Pause");
        toolbar.add(pauseButton);
        
        endGameButton = new JButton("End");
        endGameButton.addActionListener(e -> removePromotionChoicePanelIfExists());
        toolbar.add(endGameButton);
        
        toolbar.addSeparator();
        
        legalStuffButton = new JButton("Legal stuff");
        toolbar.add(legalStuffButton);
        
        toolbar.addSeparator();
        
        revertMoveButton = new JButton("Revert move");
        toolbar.add(revertMoveButton);
    }
    
    private void initializeBoard() {
        fields = new Field[9][9];
        board = new JPanel(new GridLayout(10, 10));
        
        board.setBorder(new LineBorder(Color.BLACK));
        
        // create the chess board squares
        Insets buttonMargin = new Insets(0, 0, 0, 0);
        for (int i = 0; i <= 8; ++i) {
            for (int j = 0; j <= 8; ++j) {
                Field b = new Field(i, j);
                b.setMargin(buttonMargin);
                b.setBackground(b.naturalColor());
                fields[j][i] = b;
            }
        }
        
        // top row
        for (int i = 0; i < NUMBER_OF_TILES_IN_CHESSBOARD_ROW; ++i) {
            board.add(new JLabel(""));
        }
        
        // middle rows
        for (int i = 8; i > 0; --i) {
            board.add(new JLabel("" + i, SwingConstants.CENTER));
            for (int j = 1; j < 9; ++j) {
                board.add(fields[j][i]);
            }
            board.add(new JLabel(""));
        }
        
        // bottom row
        board.add(new JLabel(""));
        for (int i = 1; i < NUMBER_OF_TILES_IN_CHESSBOARD_ROW - 1; ++i) {
            board.add(new JLabel(Character.toString((char) (64 + i)), SwingConstants.CENTER));
        }
        board.add(new JLabel(""));
        
        board.setPreferredSize(new Dimension(CHESSBOARD_SIZE, CHESSBOARD_SIZE));
        
        extendedBoard = new ExtendedBoard();
        extendedBoard.setPreferredSize(new Dimension(CHESSBOARD_SIZE, CHESSBOARD_SIZE));
        board.setBounds(0, 0, CHESSBOARD_SIZE, CHESSBOARD_SIZE);
        extendedBoard.addToBottomLayer(board);
    }
    
    private Icon scaleImageToFieldSize(Image image) {
        return new ImageIcon(image.getScaledInstance(FIELD_SIZE, FIELD_SIZE, java.awt.Image.SCALE_SMOOTH));
    }
    
    private Field fieldAt(Coordinates arg1) {
        return fields[arg1.getCol()][arg1.getRow()];
    }
    
    @SuppressWarnings("FieldCanBeLocal")
    private final int NUMBER_OF_TILES_IN_CHESSBOARD_ROW = 10;
    private final int CHESSBOARD_SIZE = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 0.67);
    private final int FIELD_SIZE = CHESSBOARD_SIZE / 10;
    private final int TIME_PANEL_WIDTH = (int) (CHESSBOARD_SIZE * 0.25);
    private final Icon emptyIcon =
            new ImageIcon(new BufferedImage(FIELD_SIZE, FIELD_SIZE, BufferedImage.TYPE_INT_ARGB));
    
    private class PromotionHandler extends JPanel {
        
        volatile PieceKind choice = null;
        final PieceKind[] choiceOptions = {PieceKind.QUEEN, PieceKind.ROOK, PieceKind.BISHOP, PieceKind.KNIGHT};
        
        PromotionHandler(Colors whoseMove) {
            
            super(new GridLayout(1, 4));
            
            for (int i = 0; i < 4; ++i) {
                Field f = new Field(0, 0);
                f.setIcon(scaleImageToFieldSize(imageService.getPieceImageFromResources(choiceOptions[i], whoseMove)));
                final int i1 = i;
                f.addActionListener(e -> {
                    synchronized (PromotionHandler.this) {
                        choice = choiceOptions[i1];
                        PromotionHandler.this.notifyAll();
                    }
                });
                f.setBackground(f.naturalColor());
                this.add(f);
            }
            setBounds(3 * FIELD_SIZE + 1, 0, 4 * FIELD_SIZE, FIELD_SIZE);
        }
        
        PieceKind choose() {
            
            synchronized (this) {
                while (choice == null) {
                    try {
                        this.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            return choice;
        }
        
    }
    
}