package view.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.util.function.BiConsumer;

import javax.swing.*;

import controller.Coordinates;

public class SwingView implements view.View {
	private JFrame mainFrame;
	private MainPanel mainPanel;
	private SettingsPanel settingsPanel = new SettingsPanel();;
	private PausePanel pauseScreen = new PausePanel();
	private LegalStuffDisplayer legalStuffPanel = new LegalStuffDisplayer();
	private BiConsumer<Coordinates, Coordinates> moveConsumer;

	public void createAndShowGUI() {
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

		mainPanel.settingsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				displaySettingsScreen();
			}
		});
		mainPanel.pauseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				displayPauseScreen();
			}
		});
		mainPanel.legalStuffButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				displayLegalStuff();
			}
		});

		ActionListener mainViewDisplayer = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				displayMainView();
			}
		};

		settingsPanel.okButton.addActionListener(mainViewDisplayer);
		pauseScreen.resumeButton.addActionListener(mainViewDisplayer);
		legalStuffPanel.okButton.addActionListener(mainViewDisplayer);

		addClicksHandlerToFields();
	}

	public SwingView() {
		mainPanel = new MainPanel();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
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

		for (int i = 1; i <= 8; ++i)
			for (int j = 1; j <= 8; ++j)
				mainPanel.fields[i][j].addActionListener(clicksHandler);
	}

	public void displayLegalStuff() {
		mainFrame.getContentPane().remove(mainPanel);
		mainFrame.getContentPane().add(legalStuffPanel);
		mainFrame.pack();
	}

	public void displayMainView() {
		mainFrame.getContentPane().removeAll();
		mainFrame.getContentPane().add(mainPanel);
		mainFrame.repaint();
		mainFrame.pack();
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
	public void addLegalStuffdisplayer(ActionListener actionListener) {
		mainPanel.legalStuffButton.addActionListener(actionListener);
		legalStuffPanel.okButton.addActionListener(actionListener);
	}

	@Override
	public void setIconAt(Coordinates c, String string) {
		mainPanel.setIconAt(c, string);
	}

	@Override
	public void addMoveHandler(BiConsumer<Coordinates, Coordinates> biConsumer) {
		moveConsumer = biConsumer;
	}

	@Override
	public void move(Coordinates arg0, Coordinates arg1) {
		mainPanel.moveIcon(arg0, arg1);
	}

	public void displayPauseScreen() {
		mainFrame.getContentPane().remove(mainPanel);
		mainFrame.getContentPane().add(pauseScreen);
		mainFrame.repaint();
		mainFrame.pack();
	}

	@Override
	public void clearGUI() {
		mainPanel.clear();
		displayMainView();
	}

	@Override
	public void castle(Coordinates from, Coordinates to) {
		Coordinates dir = Coordinates.getDir(from, to);
		Coordinates rookPos = new Coordinates(dir.getCol() > 0 ? 8 : 1, from.getRow());
		mainPanel.moveIcon(from, to);
		mainPanel.moveIcon(rookPos, from.plus(dir));
	}

	@Override
	public void enPassant(Coordinates from, Coordinates to) {
		mainPanel.moveIcon(from, to);
		mainPanel.setIconAt(new Coordinates(to.getCol(), from.getRow()), null);
	}

	@Override
	public String getPromotionChoice(String whoseMove) {
		return mainPanel.getPromotionChoice(whoseMove);
	}

	@Override
	public void promote(Coordinates moveFrom, Coordinates moveTo, String promotionChoice) {
		mainPanel.promote(moveFrom, moveTo, promotionChoice);
	}

	@Override
	public void setTime(String color, String time) {
		if (color.equals("white"))
			mainPanel.whiteTimeDisplayer.setText(time);
		else
			mainPanel.blackTimeDisplayer.setText(time);
	}

	@Override
	public File getFileToReadFrom() {
		JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
		while (true) {
			int ok = fc.showOpenDialog(mainFrame);
			if (ok == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists() && file.isFile())
					return file;
			}
			if (ok == JFileChooser.CANCEL_OPTION)
				return null;
		}
	}

	@Override
	public File getFileToSaveIn() {
		@SuppressWarnings("serial")
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
	public void displaySettingsScreen() {
		settingsPanel.setPreferredSize(mainFrame.getContentPane().getSize());
		mainFrame.getContentPane().remove(mainPanel);
		mainFrame.getContentPane().add(settingsPanel);
		mainFrame.repaint();
		mainFrame.pack();
	}

}
