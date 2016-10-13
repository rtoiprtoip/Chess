package view;

import controller.domain.Colors;
import controller.domain.Coordinates;
import controller.domain.PieceKind;
import controller.domain.Time;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.util.function.BiConsumer;

public interface View {
    
    void addSettingsListeners(ActionListener actionListener, VetoableChangeListener vcs, PropertyChangeListener pcs);
    
    void addNewGameStarter(ActionListener actionListener);
    
    void addGameLoader(ActionListener actionListener);
    
    void addGameSaver(ActionListener actionListener);
    
    void addPauseListener(ActionListener actionListener);
    
    void addEndGameListener(ActionListener actionListener);
    
    void addLegalStuffDisplayer(ActionListener actionListener);
    
    void setIconAt(Coordinates c, PieceKind pieceKind, Colors color);
    
    void addMoveHandler(BiConsumer<Coordinates, Coordinates> handler);
    
    void move(Coordinates from, Coordinates to);
    
    void clearGUI();
    
    void castle(Coordinates moveFrom, Coordinates moveTo);
    
    void enPassant(Coordinates moveFrom, Coordinates moveTo);
    
    PieceKind getPromotionChoice(Colors whoseMove);
    
    void promote(Coordinates moveFrom, Coordinates moveTo, PieceKind promotionChoice, Colors color);
    
    void setTime(Colors color, Time time);
    
    File getFileToReadFrom();
    
    File getFileToSaveIn();
    
    void addRevertMoveListener(ActionListener actionListener);
    
    void addLogSaver(ActionListener actionListener);
    
    void disableOrEnableButtonsCharacteristicForGameInProgressEqualTo(boolean gameInProgress);
}
