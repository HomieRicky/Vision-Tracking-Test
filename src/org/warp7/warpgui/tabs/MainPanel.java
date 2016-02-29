package org.warp7.warpgui.tabs;

import org.warp7.warpgui.Console;

import javax.swing.*;
import java.awt.*;

//main tab

public class MainPanel extends JPanel {
    public Console GUIconsole, robotMessages, robotWarnings;
    public JLabel streamWindow, processWindow, intakeIndicator, streamStatus;

    public MainPanel() {
        super();
        JPanel robotConsoles = new JPanel(new GridLayout(3, 1));
        JPanel GUIinfo = new JPanel(new GridLayout(2, 1));
        streamWindow = new JLabel(new ImageIcon("logo.jpg"));
        processWindow = new JLabel(new ImageIcon("logoBlack.jpg"));
        intakeIndicator = new JLabel(new ImageIcon("noball.png"));
        streamStatus = new JLabel("dfjnvndfjvndfjkvnjdkvnjdnvkjdfnvjkndfjvndjkv");
        GUIconsole = new Console(640, 100);
        robotMessages = new Console(640, 100);
        robotWarnings = new Console(640, 100);
        robotConsoles.add(robotMessages);
        robotConsoles.add(robotWarnings);
        GUIinfo.add(streamStatus);
        GUIinfo.add(GUIconsole);
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
        add(robotConsoles, mainGBC);
        mainGBC.gridx = 1;
        add(GUIinfo, mainGBC);
    }
}
