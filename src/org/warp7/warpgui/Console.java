package org.warp7.warpgui;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * This class creates a scrollable text area meant for console output.
 * Built for FRC Team 865 Warp 7 2016 Driver Station GUI
 * Created 26 Feb, 2016
 */

public class Console extends JPanel {

    private static ArrayList<Console> allConsoles = new ArrayList<>();
    private JTextArea text;
    private JScrollPane jsp;
    private String buffer = "";
    private static Calendar cal = Calendar.getInstance();
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public Console(int width, int height) {
        super(new GridLayout(1, 1));
        text = new JTextArea();
        jsp = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        text.setLineWrap(true);
        text.setEditable(false);
        setPreferredSize(new Dimension(width, height));
        add(jsp);
        allConsoles.add(this);
    }

    public void addText(String s) {
        cal = Calendar.getInstance();
        buffer += "\n[" + sdf.format(cal.getTime()) + "] " + s;
        text.setText(buffer);
        jsp.getVerticalScrollBar().setValue(jsp.getVerticalScrollBar().getMaximum());
        //repaint();
    }

    public void clearText() {
        buffer = "";
        text.setText(buffer);
        jsp.getVerticalScrollBar().setValue(jsp.getVerticalScrollBar().getMaximum());
    }

    public static void clearAllConsoles() {
        for(Console console : allConsoles) console.clearText();
    }

    public static void writeToAllConsoles(String s) {
        for(Console console : allConsoles) console.addText(s);
    }
}
