package org.warp7.warpgui.tabs;

import org.warp7.warpgui.Console;

import javax.swing.*;
import java.awt.*;

//main tab

public class MainPanel extends JPanel {
    public Console GUIconsole, robotMessages, robotWarnings;
    public JLabel streamWindow, processWindow, intakeIndicator, streamStatus;
    public ImageIcon noIntake = new ImageIcon("noball.png");
    public ImageIcon yesIntake = new ImageIcon("ball.png");

    public MainPanel() {
        super();
        JPanel consoles = new JPanel(new GridLayout(2, 2));
        streamWindow = new JLabel(new ImageIcon("logo.jpg"));
        processWindow = new JLabel(new ImageIcon("logoBlack.jpg"));
        intakeIndicator = new JLabel(noIntake);
        streamStatus = new JLabel("Initializing...");
        GUIconsole = new Console(640, 100);
        robotMessages = new Console(640, 100);
        robotWarnings = new Console(640, 100);
        consoles.add(robotMessages);
        consoles.add(streamStatus);
        consoles.add(robotWarnings);
        consoles.add(GUIconsole);
        GridBagConstraints mainGBC = new GridBagConstraints();
        mainGBC.gridx = 0;
        mainGBC.gridy = 0;
        add(streamWindow, mainGBC);
        mainGBC.gridx = 1;
        add(processWindow, mainGBC);
        mainGBC.gridx = 2;
        add(intakeIndicator, mainGBC);
        mainGBC.gridx = 0;
        mainGBC.gridy = 1;
        add(consoles, mainGBC);
    }
}
