package controller;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.*;
import java.util.function.BiConsumer;

import view.View;
import model.Game;
import model.PromotionException;
import model.SpecialMoveException;
import model.pieces.Piece;
import model.CastlingException;
import model.EnPassantException;

public class Main {

	static final View view = new view.swing.SwingView();
	static Game model;
	static MoveHistory moveHistory;

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

		view.addSettingsListeners(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getModel().setPaused(!getModel().isPaused());
			}
		}, settingsHandler, settingsHandler);

		view.addNewGameStarter(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getModel().newGame();
				updateChessboard();
				moveHistory = new MoveHistory(model);
				getModel().startOrResume();
			}
		});

		view.addGameLoader(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
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
				} catch (IOException i) {
					i.printStackTrace();
				} catch (ClassNotFoundException c) {
					c.printStackTrace();
				}
			}
		});

		view.addGameSaver(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
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

			}
		});

		view.addLogSaver(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File outputFile = view.getFileToSaveIn();
				if (outputFile == null) {
					System.err.println("Save failed");
					return;
				}

				System.err.println("Saving");
				try (PrintWriter writer = new PrintWriter(outputFile)) {
					moveHistory.getMoveLog().stream().forEach((x) -> writer.println(x));
				} catch (IOException i) {
					i.printStackTrace();
				}
			}
		});

		view.addPauseListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getModel().setPaused(!getModel().isPaused());
			}
		});

		view.addEndGameListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getModel().endGame();
				view.clearGUI();
			}
		});

		view.addLegalStuffDisplayer(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getModel().setPaused(!getModel().isPaused());
			}
		});

		view.addRevertMoveListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model = moveHistory.pop();
				updateChessboard();
				model.startOrResume();
			}
		});

		view.addMoveHandler(new BiConsumer<Coordinates, Coordinates>() {

			@Override
			public void accept(final Coordinates moveFrom, final Coordinates moveTo) {
				new Thread(new Runnable() {

					@Override
					public void run() {
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
					}
				}).start();
			}

		});
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
				if (!newVal.matches("[0-9]+:[0-9][0-9]"))
					throw new PropertyVetoException("Invalid format, use mm:ss", evt);
				String[] numbers = newVal.split(":");
				if (Integer.parseInt(numbers[1]) > 59)
					throw new PropertyVetoException("Too many seconds!", evt);
			} else if (evt.getPropertyName().equals("timeAdded")) {
				if (!((String) evt.getNewValue()).matches("[0-9]+"))
					throw new PropertyVetoException("invalid format", evt);
			} else
				assert false;
		}

	}

}
