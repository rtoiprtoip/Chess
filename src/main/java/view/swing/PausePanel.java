package view.swing;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

@SuppressWarnings("serial")
class PausePanel extends JPanel {
	final JButton resumeButton;

	PausePanel() {
		super();
		this.setLayout(new BorderLayout());
		resumeButton = new JButton("Resume");
		this.add(resumeButton, BorderLayout.PAGE_END);
	}
}
