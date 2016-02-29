package org.warp7.warpgui.tabs;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Ricardo on 2016-02-29.
 */
public class VisionTestPanel extends JPanel {

    public JScrollPane scrollable;
    public JLabel original, a, b, c, d;


    public VisionTestPanel() {
        super();
        JPanel jp = new JPanel(new GridLayout(1, 5));
        scrollable = new JScrollPane();
        original = new JLabel(new ImageIcon("logo.jpg"));
        a = new JLabel(new ImageIcon("logoBlack.jpg"));
        b = new JLabel(new ImageIcon("logoBlack.jpg"));
        c = new JLabel(new ImageIcon("logoBlack.jpg"));
        d = new JLabel(new ImageIcon("logoBlack.jpg"));
        jp.add(original);
        jp.add(a);
        jp.add(b);
        jp.add(c);
        jp.add(d);
        scrollable = new JScrollPane(jp);
        scrollable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scrollable);
        scrollable.setPreferredSize(new Dimension(1600, 500));
    }
}
