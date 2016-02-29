import edu.wpi.first.wpilibj.networktables.NetworkTable;
import org.opencv.core.Core;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by Ricardo on 2016-02-26.
 */
public class WarpGUI extends JFrame {
    //STATICS
    //GUI
    private static WarpGUI main;
    private static Console GUIconsole, processingConsole, robotMessages, robotWarnings;
    public static NetworkTable robotTables, robot, processingOutputs;
    public static JTabbedPane tabs;
    public static JPanel mainPanel, autoPanel, testPanel, visionTestPanel, optionsPanel;


    //NON-STATICS

    public static void main(String args[]) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                try {
                    restartApplication();
                } catch (URISyntaxException e1) {
                    System.exit(1);
                } catch (IOException e1) {
                    System.exit(1);
                }
            }
        });

        main = new WarpGUI();

        //Init other stuff here

        while(true) {
            //Do repeating stuff
        }

    }

    public WarpGUI() {
        tabs = new JTabbedPane();
        add(tabs);

        mainPanel = new JPanel();
        autoPanel = new JPanel();
        testPanel = new JPanel();
        visionTestPanel = new JPanel();
        optionsPanel = new JPanel();

        tabs.addTab("Drive", mainPanel);
        tabs.addTab("Autonomous", autoPanel);
        tabs.addTab("Test", testPanel);
        tabs.addTab("Vision", visionTestPanel);
        tabs.addTab("Options", optionsPanel);


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void restartApplication() throws URISyntaxException, IOException {
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File(WarpGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        /* is it a jar file? */
        if(!currentJar.getName().endsWith(".jar"))
            return;

        /* Build command: java -jar application.jar */
        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
    }


}
