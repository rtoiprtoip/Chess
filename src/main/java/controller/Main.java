package controller;

import controller.exceptions.CastlingException;
import controller.exceptions.EnPassantException;
import controller.exceptions.PromotionException;
import controller.exceptions.SpecialMoveException;
import lombok.NonNull;
import model.domain.pieces.Piece;
import model.history.MoveHistory;
import model.history.impl.MoveHistoryImpl;
import model.logic.GameLogic;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import view.View;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.*;

@ComponentScan(basePackages = "model, view")
@SpringBootApplication
public class Main {
    
    private final View view;
    private final GameLogic model;
    
    Main(@NonNull GameLogic model, @NonNull View view) {
        this.model = model;
        this.view = view;
        
        SettingsHandler settingsHandler = new SettingsHandler();
        
        Thread timeCounter = new Thread() {
            @Override
            @SuppressWarnings("InfiniteLoopStatement")
            public void run() {
                synchronized (this) {
                    while (true) {
                        view.setTime("white", model.getPlayerTime("white"));
                        view.setTime("black", model.getPlayerTime("black"));
                        try {
                            wait(50);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        };
        timeCounter.setDaemon(true);
        timeCounter.start();
        
        view.addSettingsListeners(e -> model.setPaused(model.isNotPaused()), settingsHandler, settingsHandler);
        
        view.addNewGameStarter(e -> {
            model.newGame();
            updateChessboard();
            model.startOrResume();
        });
        
        view.addGameLoader(e -> {
            File inputFile = view.getFileToReadFrom();
            if (inputFile == null) {
                System.err.println("Load cancelled");
                return;
            }
            
            try (FileInputStream fileInput = new FileInputStream(inputFile);
                 ObjectInputStream objIn = new ObjectInputStream(fileInput)) {
                
                MoveHistory moveHistory = (MoveHistoryImpl) objIn.readObject();
                model.loadGame(moveHistory);
                
                updateChessboard();
                model.startOrResume();
            } catch (StreamCorruptedException s) {
                System.out.println("File corrupted");
            } catch (IOException | ClassNotFoundException i) {
                i.printStackTrace();
            }
        });
        
        view.addGameSaver(e -> {
            File outputFile = view.getFileToSaveIn();
            if (outputFile == null) {
                System.err.println("Save failed");
                return;
            }
            
            System.err.println("Saving");
            try (FileOutputStream fileOut = new FileOutputStream(outputFile);
                 ObjectOutputStream objOut = new ObjectOutputStream(fileOut)) {
                objOut.writeObject(model.getMoveHistory());
            } catch (IOException i) {
                i.printStackTrace();
            }
            
        });
        
        view.addLogSaver(e -> {
            File outputFile = view.getFileToSaveIn();
            if (outputFile == null) {
                System.err.println("Save failed");
                return;
            }
            
            System.err.println("Saving");
            try (PrintWriter writer = new PrintWriter(outputFile)) {
                model.getMoveHistory().getMoveLog().forEach(writer::println);
            } catch (IOException i) {
                i.printStackTrace();
            }
        });
        
        view.addPauseListener(e -> model.setPaused(model.isNotPaused()));
        
        view.addEndGameListener(e -> {
            model.endGame();
            view.clearGUI();
        });
        
        view.addLegalStuffDisplayer(e -> model.setPaused(model.isNotPaused()));
        
        view.addRevertMoveListener(e -> {
            System.err.println("revert");
            model.revertMove();
            updateChessboard();
            model.startOrResume();
        });
        
        view.addMoveHandler((moveFrom, moveTo) -> new Thread(() -> {
            try {
                boolean moveAuthorized = model.isThisValidMove(moveFrom, moveTo);
                if (moveAuthorized) {
                    model.move(moveFrom, moveTo);
                    view.move(moveFrom, moveTo);
                    
                    System.err.println(moveFrom + "-" + moveTo);
                }
            } catch (PromotionException e) {
                String color = model.getWhoseMove().toString();
                String promotionChoice = view.getPromotionChoice(color);
                model.promote(moveFrom, moveTo, promotionChoice);
                promotionChoice = (color + "_" + promotionChoice).toLowerCase();
                view.promote(moveFrom, moveTo, promotionChoice);
                
                System.err.println(moveFrom + "-" + moveTo);
                
            } catch (CastlingException e) {
                model.castle(moveFrom, moveTo);
                view.castle(moveFrom, moveTo);
                
                System.err.println(moveFrom + "-" + moveTo);
                
            } catch (EnPassantException e) {
                model.enPassant(moveFrom, moveTo);
                view.enPassant(moveFrom, moveTo);
                
                System.err.println(moveFrom + "-" + moveTo);
                
            } catch (SpecialMoveException e) {
                throw new AssertionError();
            }
        }).start());
    }
    
    public static void main(String[] args) {
        new SpringApplicationBuilder(Main.class).headless(false).run(args);
    }
    
    private void updateChessboard() {
        for (int i = 1; i < 9; ++i) {
            for (int j = 1; j < 9; ++j) {
                Coordinates c = new Coordinates(i, j);
                Piece piece = model.getPieceAt(c);
                view.setIconAt(c, piece == null ? null : piece.toString());
            }
        }
    }
    
    private class SettingsHandler implements VetoableChangeListener, PropertyChangeListener {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null) {
                return;
            }
            if (evt.getPropertyName().equals("gameTime")) {
                String[] numbers = ((String) evt.getNewValue()).split(":");
                model.setGameTime(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]));
            } else if (evt.getPropertyName().equals("timeAdded")) {
                model.setTimeToAddAfterMove(Integer.parseInt((String) evt.getNewValue()));
            } else {
                throw new AssertionError();
            }
        }
        
        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            String newVal = ((String) evt.getNewValue()).trim();
            if (evt.getPropertyName().equals("gameTime")) {
                if (!newVal.matches("\\d+:[0-5]\\d")) {
                    throw new PropertyVetoException("Invalid format, use mm:ss", evt);
                }
            } else if (evt.getPropertyName().equals("timeAdded")) {
                if (!((String) evt.getNewValue()).trim().matches("\\d+")) {
                    throw new PropertyVetoException("invalid format", evt);
                }
            } else {
                throw new AssertionError();
            }
        }
        
    }
    
}
