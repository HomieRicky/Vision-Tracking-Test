package org.warp7.warpgui.tabs;

import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

/**
 * Created by Ricardo on 2016-02-29.
 */
public class TestPanel extends JPanel implements ITableListener {

    JScrollPane jsp;
    JTable table;
    Hashtable<String, Object> data = new Hashtable<>();
    Object[][] organizedInfo = new Object[128][3];
    static Object[] headers = new Object[]{"Group", "Item", "Value"};


    public TestPanel() {
        super(new GridLayout(1, 1));
        table = new JTable(organizedInfo, headers);
        jsp = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setPreferredSize(new Dimension(300, 700));
        add(jsp);
    }

    @Override
    public void valueChanged(ITable iTable, String s, Object o, boolean b) {

    }

    public void updateTable(String table, String key, Object item) {
        String addr = table+"/"+key;
        if(data.containsKey(addr)) {
            data.replace(addr, item);
        } else {
            data.put(addr, item);
        }
        Object[] keys = data.keySet().toArray();
        Object[] vals = data.values().toArray();
        System.out.println(keys.length + " | " + vals.length);
        for(int i = 0; i < keys.length; i++) {
            organizedInfo[i][0] = String.valueOf(keys[i]).substring(String.valueOf(keys[i]).indexOf("/")+1, String.valueOf(keys[i]).lastIndexOf("/"));
            organizedInfo[i][1] = String.valueOf(keys[i]).substring(String.valueOf(keys[i]).lastIndexOf("/")+1);
            organizedInfo[i][2] = vals[i];
        }
        this.table = new JTable(organizedInfo, headers);
        for(int i = 0; i < organizedInfo.length; i++) {
            for(int j = 0; j < organizedInfo[i].length; j++) {
                this.table.setValueAt(organizedInfo[i][j], i, j);
            }
        }
    }


}
