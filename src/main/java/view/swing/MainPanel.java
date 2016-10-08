package view.swing;

import controller.domain.Coordinates;
import controller.domain.PieceKind;
import controller.domain.Colors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

class MainPanel extends JPanel {
    
    private JLayeredPane extendedBoard;
    // will use two layers, one for displaying board, second for promotion
    // choice
    
    private JPanel board;
    Field[][] fields;
    
    private JToolBar toolbar;
    JButton settingsButton, newGameButton, loadButton, saveButton, saveLogButton, pauseButton, endGameButton,
            legalStuffButton, revertMoveButton;
    
    private JPanel clocks;
    JLabel whiteTimeDisplayer, blackTimeDisplayer;
    
    MainPanel() {
        initializeBoard();
        initializeToolbar();
        initializeClocks();
        
        this.setLayout(new BorderLayout());
        this.add(toolbar, BorderLayout.PAGE_START);
        this.add(extendedBoard, BorderLayout.LINE_START);
        this.add(clocks, BorderLayout.LINE_END);
    }
    
    private void initializeClocks() {
        int fontSize = (int) (CHESSBOARD_SIZE * 0.045);
        
        clocks = new JPanel(new GridLayout(5, 1));
        clocks.setPreferredSize(new Dimension(TIME_PANEL_WIDTH, CHESSBOARD_SIZE));
        clocks.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));
        
        clocks.add(new JLabel());
        
        whiteTimeDisplayer = new JLabel("00:00", SwingConstants.CENTER);
        whiteTimeDisplayer.setFont(new Font("Verdana", Font.PLAIN, fontSize));
        whiteTimeDisplayer.setOpaque(true);
        whiteTimeDisplayer.setBackground(Color.WHITE);
        clocks.add(whiteTimeDisplayer);
        
        blackTimeDisplayer = new JLabel("00:00", SwingConstants.CENTER);
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
        
        extendedBoard = new JLayeredPane();
        extendedBoard.setPreferredSize(new Dimension(CHESSBOARD_SIZE, CHESSBOARD_SIZE));
        board.setBounds(0, 0, CHESSBOARD_SIZE, CHESSBOARD_SIZE);
        extendedBoard.add(board, 1);
    }
    
    void setIconAt(Coordinates c, Integer o) {
        if (o == null) {
            setIconAt(c, null, null);
        } else {
            throw new AssertionError();
        }
    }
    
    void setIconAt(Coordinates c, PieceKind pieceKind, Colors color) {
        if (pieceKind == null && color != null) {
            throw new AssertionError();
        }
        if (pieceKind != null && color == null) {
            throw new AssertionError();
        }
        
        if (pieceKind == null) {
            fields[c.getCol()][c.getRow()].setIcon(emptyIcon);
            return;
        }
        
        Icon icon = emptyIcon;
        String fileName = (color.toString() + "_" + pieceKind.toString()).toLowerCase();
        try {
            URL url = Thread.currentThread().getContextClassLoader()
                    .getResource("resources/icons/" + fileName + ".svg");
            icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(url).getScaledInstance(FIELD_SIZE, FIELD_SIZE,
                    java.awt.Image.SCALE_SMOOTH));
        } catch (NullPointerException e) {
            try {
                URL url = Thread.currentThread().getContextClassLoader().getResource("icons/" + fileName + ".svg");
                icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(url).getScaledInstance(FIELD_SIZE, FIELD_SIZE,
                        java.awt.Image.SCALE_SMOOTH));
            } catch (NullPointerException e1) {
                throw new AssertionError("File not found", e);
            }
        } finally {
            fields[c.getCol()][c.getRow()].setIcon(icon);
        }
    }
    
    @SuppressWarnings("FieldCanBeLocal")
    private final int NUMBER_OF_TILES_IN_CHESSBOARD_ROW = 10;
    private final int CHESSBOARD_SIZE = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 0.67);
    private final int FIELD_SIZE = CHESSBOARD_SIZE / 10;
    private final int TIME_PANEL_WIDTH = (int) (CHESSBOARD_SIZE * 0.25);
    private final Icon emptyIcon =
            new ImageIcon(new BufferedImage(FIELD_SIZE, FIELD_SIZE, BufferedImage.TYPE_INT_ARGB));
    
    void moveIcon(Coordinates arg0, Coordinates arg1) {
        fieldAt(arg1).setIcon(fieldAt(arg0).getIcon());
        fieldAt(arg0).setIcon(emptyIcon);
    }
    
    private Field fieldAt(Coordinates arg1) {
        return fields[arg1.getCol()][arg1.getRow()];
    }
    
    void clear() {
        for (int i = 1; i <= 8; ++i) {
            for (int j = 1; j <= 8; ++j) {
                fields[i][j].setIcon(emptyIcon);
            }
        }
        whiteTimeDisplayer.setText("00:00");
        blackTimeDisplayer.setText("00:00");
    }
    
    void promote(Coordinates moveFrom, Coordinates moveTo, PieceKind promotionChoice, Colors whoseMove) {
        setIconAt(moveFrom, null);
        setIconAt(moveTo, promotionChoice, whoseMove);
    }
    
    PieceKind getPromotionChoice(Colors color) {
        PromotionHandler ph = new PromotionHandler(color);
        extendedBoard.add(ph, 2);
        try {
            return ph.choose();
        } finally {
            extendedBoard.remove(ph);
            board.repaint();
        }
    }
    
    private class PromotionHandler extends JPanel {
        
        volatile PieceKind choice = null;
        final PieceKind[] choiceOptions = {PieceKind.QUEEN, PieceKind.ROOK, PieceKind.BISHOP, PieceKind.KNIGHT};
        
        PromotionHandler(Colors whoseMove) {
            
            super(new GridLayout(1, 4));
            
            for (int i = 0; i < 4; ++i) {
                Field f = new Field(0, 0);
                f.setIcon(new ImageIcon(new ImageIcon(
                        getClass().getResource(
                                ("/icons/" + whoseMove.toString().toLowerCase() + "_" +
                                 choiceOptions[i].toString().toLowerCase() + ".svg").toLowerCase())).getImage()
                        .getScaledInstance(FIELD_SIZE, FIELD_SIZE, java.awt.Image.SCALE_SMOOTH)));
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
