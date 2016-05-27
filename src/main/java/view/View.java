package view;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.util.function.BiConsumer;

import controller.Coordinates;

public interface View {

	void addSettingsListeners(ActionListener actionListener, VetoableChangeListener vcs, PropertyChangeListener pcs);

	void addNewGameStarter(ActionListener actionListener);

	void addGameLoader(ActionListener actionListener);

	void addGameSaver(ActionListener actionListener);

	void addPauseListener(ActionListener actionListener);

	void addEndGameListener(ActionListener actionListener);

	void addLegalStuffdisplayer(ActionListener actionListener);

	void setIconAt(Coordinates c, String pieceName);

	void addMoveHandler(BiConsumer<Coordinates, Coordinates> handler);

	void move(Coordinates from, Coordinates to);

	void clearGUI();

	void castle(Coordinates moveFrom, Coordinates moveTo);

	void enPassant(Coordinates moveFrom, Coordinates moveTo);

	String getPromotionChoice(String whoseMove);

	void promote(Coordinates moveFrom, Coordinates moveTo, String promotionChoice);

	void setTime(String string, String whiteTime);

	File getFileToReadFrom();

	File getFileToSaveIn();

	void displaySettingsScreen();
}
