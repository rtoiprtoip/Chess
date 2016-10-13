package controller;

import controller.domain.Colors;
import controller.domain.Coordinates;
import controller.domain.PieceKind;
import controller.exceptions.CastlingException;
import controller.exceptions.EnPassantException;
import controller.exceptions.PromotionException;
import controller.exceptions.SpecialMoveException;
import lombok.NonNull;
import model.history.MoveHistory;
import model.history.impl.MoveHistoryImpl;
import model.logic.GameLogic;
import model.pieces.Piece;
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
    
    private boolean gameInProgress;
    // true, if there's something on the board, false otherwise (no game has started yet or a game was ended
    
    Main(@NonNull GameLogic model, @NonNull View view) {
        this.model = model;
        this.view = view;
        
        Thread timeCounter = new Thread() {
            @Override
            @SuppressWarnings("InfiniteLoopStatement")
            public void run() {
                synchronized (this) {
                    while (true) {
                        view.setTime(Colors.WHITE, model.getPlayerTime(Colors.WHITE));
                        view.setTime(Colors.BLACK, model.getPlayerTime(Colors.BLACK));
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
        
        SettingsHandler settingsHandler = new SettingsHandler();
        view.addSettingsListeners(e -> model.setPaused(model.isNotPaused()), settingsHandler, settingsHandler);
        
        view.addNewGameStarter(e -> {
            model.newGame();
            updateChessboard();
            model.startOrResume();
            setGameInProgressToTrueAndUpdateView();
        });
        
        view.addGameLoader(e -> {
            File inputFile = view.getFileToReadFrom();
            if (inputFile == null) {
                System.err.println("Load cancelled");
                return;
            }
            MoveHistory moveHistory;
            try (FileInputStream fileInput = new FileInputStream(inputFile);
                 ObjectInputStream objIn = new ObjectInputStream(fileInput)) {
                moveHistory = (MoveHistoryImpl) objIn.readObject();
            } catch (IOException | ClassNotFoundException i) {
                i.printStackTrace();
                throw new RuntimeException("Error while loading game", i);
            }
            model.loadGame(moveHistory);
            
            updateChessboard();
            model.startOrResume();
            setGameInProgressToTrueAndUpdateView();
        });
        
        view.addGameSaver(e -> {
            validateThatGameInProgessHasValue(true);
            
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
            validateThatGameInProgessHasValue(true);
            
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
        
        view.addPauseListener(e -> {
            validateThatGameInProgessHasValue(true);
            
            model.setPaused(model.isNotPaused());
        });
        
        view.addEndGameListener(e -> {
            validateThatGameInProgessHasValue(true);
            
            model.endGame();
            view.clearGUI();
            setGameInProgressToFalseAndUpdateView();
        });
        
        view.addLegalStuffDisplayer(e -> model.setPaused(model.isNotPaused()));
        
        view.addRevertMoveListener(e -> {
            validateThatGameInProgessHasValue(true);
            
            System.err.println("revert");
            model.revertMove();
            updateChessboard();
            model.startOrResume();
        });
        
        view.addMoveHandler((moveFrom, moveTo) -> new Thread(() -> {
            validateThatGameInProgessHasValue(true);
            
            boolean moveSuccessful = true;
            try {
                moveSuccessful = model.tryToMove(moveFrom, moveTo);
                if (moveSuccessful) {
                    view.move(moveFrom, moveTo);
                }
            } catch (PromotionException e) {
                Colors whoseMove = model.getWhoseMove();
                PieceKind promotionChoice = view.getPromotionChoice(whoseMove);
                model.promote(promotionChoice);
                view.promote(moveFrom, moveTo, promotionChoice, whoseMove);
            } catch (CastlingException e) {
                view.castle(moveFrom, moveTo);
            } catch (EnPassantException e) {
                view.enPassant(moveFrom, moveTo);
            } catch (SpecialMoveException e) {
                throw new AssertionError();
            } finally {
                if (moveSuccessful) {
                    System.err.println(moveFrom + "-" + moveTo);
                }
            }
        }).start());
        
        setGameInProgressToFalseAndUpdateView();
    }
    
    private void setGameInProgressToTrueAndUpdateView() {
        gameInProgress = true;
        view.disableOrEnableButtonsCharacteristicForGameInProgressEqualTo(true);
    }
    
    private void setGameInProgressToFalseAndUpdateView() {
        gameInProgress = false;
        view.disableOrEnableButtonsCharacteristicForGameInProgressEqualTo(false);
    }
    
    public static void main(String[] args) {
        new SpringApplicationBuilder(Main.class).headless(false).run(args);
    }
    
    private void updateChessboard() {
        for (int i = 1; i < 9; ++i) {
            for (int j = 1; j < 9; ++j) {
                Coordinates c = Coordinates.of(i, j);
                Piece piece = model.getPieceAt(c);
                if (piece == null) {
                    view.setIconAt(c, null, null);
                } else {
                    view.setIconAt(c, piece.getKind(), piece.getColor());
                }
            }
        }
    }
    
    private void validateThatGameInProgessHasValue(boolean expectedValue) {
        if (gameInProgress != expectedValue) {
            throw new IllegalStateException("This action is not permitted right now");
        }
    }
    
    private class SettingsHandler implements VetoableChangeListener, PropertyChangeListener {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            validateThatGameInProgessHasValue(false);
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
            validateThatGameInProgessHasValue(false);
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
