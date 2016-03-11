package org.warp7.warpgui;

import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

/**
 * Created by Ricardo on 2016-02-29.
 */
public class NetworkTableListener implements ITableListener {
    public NetworkTableListener() {
        super();
    }

    @Override
    public void valueChanged(ITable source, String key, Object value, boolean isNew) {
        if(key.equals("messages")) WarpGUI.mainPanel.robotMessages.addText(String.valueOf(value));
        else if(key.equals("warnings")) WarpGUI.mainPanel.robotWarnings.addText(String.valueOf(value));
        else if(key.equals("intake")) {
            if(value == Boolean.TRUE) WarpGUI.mainPanel.intakeIndicator.setIcon(WarpGUI.mainPanel.yesIntake);
            else WarpGUI.mainPanel.intakeIndicator.setIcon(WarpGUI.mainPanel.noIntake);
        }
    }


}
