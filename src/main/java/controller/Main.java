package controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.*;

import view.View;
import model.Game;
import model.PromotionException;
import model.SpecialMoveException;
import model.pieces.Piece;
import model.CastlingException;
import model.EnPassantException;

public class Main {

    private static final View view = new view.swing.SwingView();
    private static Game model;
    private static MoveHistory moveHistory;

    public static void main(String[] args) {
        model = new Game();
        SettingsHandler settingsHandler = new SettingsHandler();

        Thread timeCounter = new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    while (true) {
                        view.setTime("white", getModel().getTime("white"));
                        view.setTime("black", getModel().getTime("black"));
                        try {
                            wait(50);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        };
        timeCounter.setDaemon(true);
        timeCounter.start();

        view.addSettingsListeners(e -> getModel().setPaused(!getModel().isPaused()), settingsHandler, settingsHandler);

        view.addNewGameStarter(e -> {
            getModel().newGame();
            updateChessboard();
            moveHistory = new MoveHistory(model);
            getModel().startOrResume();
        });

        view.addGameLoader(e -> {
            File inputFile = view.getFileToReadFrom();
            if (inputFile == null) {
                System.err.println("Load cancelled");
                return;
            }

            try (FileInputStream fileInput = new FileInputStream(inputFile);
                 ObjectInputStream objIn = new ObjectInputStream(fileInput)) {

                moveHistory = (MoveHistory) objIn.readObject();
                model = moveHistory.pop();

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
                objOut.writeObject(moveHistory);
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
                moveHistory.getMoveLog().forEach(writer::println);
            } catch (IOException i) {
                i.printStackTrace();
            }
        });

        view.addPauseListener(e -> getModel().setPaused(!getModel().isPaused()));

        view.addEndGameListener(e -> {
            getModel().endGame();
            view.clearGUI();
        });

        view.addLegalStuffDisplayer(e -> getModel().setPaused(!getModel().isPaused()));

        view.addRevertMoveListener(e -> {
            model = moveHistory.pop();
            updateChessboard();
            model.startOrResume();
        });

        view.addMoveHandler((moveFrom, moveTo) -> new Thread(() -> {
            try {
                boolean moveAuthorized = getModel().isThisValidMove(moveFrom, moveTo);
                if (moveAuthorized) {
                    getModel().move(moveFrom, moveTo);
                    view.move(moveFrom, moveTo);

                    moveHistory.push(model, moveFrom, moveTo);
                    System.err.println(moveFrom + "-" + moveTo);

                }
            } catch (PromotionException e) {
                String color = getModel().getWhoseMove().toString();
                String promotionChoice = view.getPromotionChoice(color);
                getModel().promote(moveFrom, moveTo, promotionChoice);
                promotionChoice = (color + "_" + promotionChoice).toLowerCase();
                view.promote(moveFrom, moveTo, promotionChoice);

                moveHistory.push(model, moveFrom, moveTo, promotionChoice);
                System.err.println(moveFrom + "-" + moveTo);

            } catch (CastlingException e) {
                getModel().castle(moveFrom, moveTo);
                view.castle(moveFrom, moveTo);

                moveHistory.push(model, moveFrom, moveTo);
                System.err.println(moveFrom + "-" + moveTo);

            } catch (EnPassantException e) {
                getModel().enPassant(moveFrom, moveTo);
                view.enPassant(moveFrom, moveTo);

                moveHistory.push(model, moveFrom, moveTo);
                System.err.println(moveFrom + "-" + moveTo);

            } catch (SpecialMoveException e) {
                assert false;
            }
        }).start());
    }

    private static void updateChessboard() {
        for (int i = 1; i < 9; ++i)
            for (int j = 1; j < 9; ++j) {
                Coordinates c = new Coordinates(i, j);
                Piece piece = getModel().getPieceAt(c);
                view.setIconAt(c, piece == null ? null : piece.toString());
            }
    }

    private static Game getModel() {
        return model;
    }

    private static class SettingsHandler implements VetoableChangeListener, PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null)
                return;
            if (evt.getPropertyName().equals("gameTime")) {
                String[] numbers = ((String) evt.getNewValue()).split(":");
                model.setGameTime(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]));
            } else if (evt.getPropertyName().equals("timeAdded")) {
                model.setTimeAddedPerMove(Integer.parseInt((String) evt.getNewValue()));
            } else
                assert false;
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            String newVal = ((String) evt.getNewValue()).trim();
            if (evt.getPropertyName().equals("gameTime")) {
                if (!newVal.matches("\\d+:[0-5]\\d"))
                    throw new PropertyVetoException("Invalid format, use mm:ss", evt);
            } else if (evt.getPropertyName().equals("timeAdded")) {
                if (!((String) evt.getNewValue()).trim().matches("\\d+"))
                    throw new PropertyVetoException("invalid format", evt);
            } else
                assert false;
        }

    }

}
