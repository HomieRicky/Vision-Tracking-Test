package org.warp7.warpgui.tabs;

import org.warp7.warpgui.FrameProcessor;
import org.warp7.warpgui.WarpGUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Created by Ricardo on 2016-02-29.
 */
public class VisionTestPanel extends JPanel implements ChangeListener {

    public JScrollPane scrollable;
    public JLabel original, a, b, c, d;
    public JSlider channelSlider;
    public static int sliderVal = 0;


    public VisionTestPanel() {
        super(new GridLayout(2, 1));
        JPanel jp = new JPanel(new GridLayout(1, 5));
        scrollable = new JScrollPane();
        original = new JLabel(new ImageIcon("logo.jpg"));
        a = new JLabel(new ImageIcon("logoBlack.jpg"));
        b = new JLabel(new ImageIcon("logoBlack.jpg"));
        c = new JLabel(new ImageIcon("logoBlack.jpg"));
        d = new JLabel(new ImageIcon("logoBlack.jpg"));
        channelSlider = new JSlider(JSlider.CENTER, -255, 255, 0);
        channelSlider.addChangeListener(this);
        channelSlider.setMajorTickSpacing(10);
        channelSlider.setMinorTickSpacing(2);
        jp.add(original);
        jp.add(a);
        jp.add(b);
        jp.add(c);
        jp.add(d);
        scrollable = new JScrollPane(jp);
        scrollable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scrollable);
        add(channelSlider);
        scrollable.setPreferredSize(new Dimension(1600, 500));
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(e.getSource() instanceof JSlider) {
            JSlider s = (JSlider) e.getSource();
            sliderVal = s.getValue();
            System.out.println("val: " + sliderVal);
        }
    }
}
