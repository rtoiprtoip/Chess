package view.swing;

import javax.swing.*;
import java.awt.*;

class LegalStuffDisplayer extends JPanel {
    
    final JButton okButton;
    
    private final String newLine = System.getProperty("line.separator");
    
    @SuppressWarnings("FieldCanBeLocal")
    private final String legalStuff =
            newLine + newLine + newLine + newLine + "Created by Piotr Urbanczyk." + newLine
            + "This work is licensed under a Creative Commons Attribution-ShareAlike 4.0 International License,"
            + newLine + "available at http://creativecommons.org/licenses/by-sa/4.0/." + newLine + newLine
            + "Icons of pieces are property of Wikipedia user Cburnett." + newLine
            + "They are licensed under Creative Commons Attribution-Share Alike 3.0 Unported license," + newLine
            + "available at http://creativecommons.org/licenses/by-sa/3.0/." + newLine + newLine
            +
            "In short, both licenses allow you to use, modify and redistribute the work, as long as you give " +
            "appropriate credit"
            + newLine + " to original creators and distribute your contributions under the same license.";
    
    public LegalStuffDisplayer() {
        super();
        this.setLayout(new BorderLayout());
        this.add(new JTextArea(legalStuff), BorderLayout.CENTER);
        okButton = new JButton("OK");
        this.add(okButton, BorderLayout.PAGE_END);
    }
}
