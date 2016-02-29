package org.warp7.warpgui;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.warp7.warpgui.tabs.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Ricardo on 2016-02-26.
 */
public class WarpGUI extends JFrame {
    //STATICS
    //GUI
    private static WarpGUI main;
    private static final String VERSION = "prerelease-20160229b";
    public static NetworkTable robotTables, robot, processingOutputs;
    public static JTabbedPane tabs;
    public static JPanel autoPanel, testPanel, optionsPanel;
    public static MainPanel mainPanel;
    public static VisionTestPanel visionTestPanel;

    //DEFAULTS
    public static final File CONFIG_DEFAULT_PATH = new File("values.txt");
    public static final File SETTINGS_DEFAUlT_PATH = new File("settings.txt");
    public static String LOG_DEFAULT_PATH;
    private static String DEFAULT_IP = "10.8.65.2";
    private static int DEFAULT_PORT = 1180;

    //STORAGE
    public static volatile BufferedImage frameBuffer = null;
    public static volatile boolean frameUpdated = false;
    public static volatile long timestamp = System.currentTimeMillis();
    static USBCameraInputStream stream;



    //NON-STATICS

    public static void main(String args[]) throws URISyntaxException {
        final File ldp = new File(WarpGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        LOG_DEFAULT_PATH = ldp.getAbsolutePath().substring(0, ldp.getAbsolutePath().lastIndexOf("\\"));
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
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
        mainPanel.GUIconsole.addText("Connecting to local camera");
        System.out.println("connecting");
        stream = new USBCameraInputStream(true, 1);
        Thread streamThread = new Thread(stream);
        streamThread.start();
        System.out.println("connected");
        mainPanel.GUIconsole.addText("Connected to local camera");
        FrameProcessor processor = null;
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<List<MatOfPoint>> futureTask = null;
        boolean doingTask = false;



        while(true) {

            switch(tabs.getSelectedIndex()) {
                case 0:
                    if(frameUpdated) {
                        mainPanel.streamWindow.setIcon(new ImageIcon(frameBuffer));
                        frameUpdated = false;
                        if(!doingTask) {
                            processor = new FrameProcessor(bufferedImageToMat(frameBuffer));
                            futureTask = es.submit(processor);
                            doingTask = true;
                        } else {
                            try {
                                if (futureTask.isDone()) {
                                    Mat m = processor.m;
                                    Imgproc.fillPoly(m, futureTask.get(), Scalar.all(255));
                                    mainPanel.processWindow.setIcon(new ImageIcon(matToBufferedImage(m)));
                                }
                            } catch(NullPointerException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            doingTask = false;
                        }
                    }
                    break;
                case 1:

                    break;
                case 2:

                    break;
                case 3:
                    if(frameUpdated) {
                        visionTestPanel.original.setIcon(new ImageIcon(frameBuffer));
                        frameUpdated = false;
                        if (!doingTask) {
                            processor = new FrameProcessor(bufferedImageToMat(frameBuffer), true);
                            futureTask = es.submit(processor);
                            doingTask = true;
                        } else {
                            try {
                                if (futureTask.isDone()) {
                                    visionTestPanel.a.setIcon(new ImageIcon(matToBufferedImage(FrameProcessor.A)));
                                    visionTestPanel.b.setIcon(new ImageIcon(matToBufferedImage(FrameProcessor.B)));
                                    visionTestPanel.c.setIcon(new ImageIcon(matToBufferedImage(FrameProcessor.C)));
                                    visionTestPanel.d.setIcon(new ImageIcon(matToBufferedImage(FrameProcessor.D)));
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            doingTask = false;
                        }
                    }
                    break;
                case 4:

                    break;
            }
        }

    }

    public WarpGUI() {
        tabs = new JTabbedPane();
        add(tabs);

        mainPanel = new MainPanel();
        autoPanel = new AutoPanel();
        testPanel = new TestPanel();
        visionTestPanel = new VisionTestPanel();
        optionsPanel = new OptionPanel();

        tabs.addTab("Drive", mainPanel);
        tabs.addTab("Autonomous", autoPanel);
        tabs.addTab("Test", testPanel);
        tabs.addTab("Vision", visionTestPanel);
        tabs.addTab("Options", optionsPanel);


        setTitle("WarpGUI ver. " + VERSION);
        setPreferredSize(new Dimension(1920, 790));
        pack();
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
        command.add(currentJar.getPath() + " \"lol\"");

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
    }

    //Utility methods
    public static BufferedImage matToBufferedImage(Mat frame) {
        //Mat() to BufferedImage
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);
        return image;
    }

    public static Mat bufferedImageToMat(BufferedImage img) {
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        Mat m = new Mat(480, 640, CvType.CV_8UC3);
        m.put(0, 0, pixels);
        return m;
    }

    /*
    private static void initNetworkTables(DriverStream d) {
        NetworkTable.setClientMode();
        NetworkTable.setIPAddress("10.8.65.52");
        visionTbl = NetworkTable.getTable("vision");
        modeTbl = NetworkTable.getTable("status");
        visionTbl.addTableListener(d);
        modeTbl.addTableListener(d);
    }

    public static void attemptConnect(DriverStream d) {
        try {
            robotAddress = InetAddress.getByName("10.8.65.52");
            if(robotAddress.isReachable(5000)) {
                stream = new USBCameraInputStream(d, 30);
                initNetworkTables(d);
            } else {
                d.usingLocalCamera = true;
                stream = new USBCameraInputStream(d, true, USBCameraInputStream.testLocalCameraPorts(1));
            }
        } catch (UnknownHostException e) {
            stream = new USBCameraInputStream(d, true, USBCameraInputStream.testLocalCameraPorts(1));
            d.consoleBuffer += "Error looking for " + robotAddress.toString() + ". Error type: " + e.getMessage() + ". Using local cameras.";
            d.usingLocalCamera = true;
        } catch (IOException e) {
            stream = new USBCameraInputStream(d, true, USBCameraInputStream.testLocalCameraPorts(1));
            d.consoleBuffer += "Error looking for " + robotAddress.toString() + ". Error type: " + e.getMessage() + ". Using local cameras.";
            d.usingLocalCamera = true;
        }
    }

    @Override
    public void valueChanged(ITable iTable, String s, Object o, boolean b) {
        if(s.equals("messages") && o instanceof String) roboConsoleBuffer += o + "\n";
        else if(s.equals("warnings") && o instanceof String) roboConsoleBuffer += "[WARNING]" + o + "\n";
        else if(s.equals("mode") && o instanceof Double) { modeChanged = true; mode = (int) o; }
    }
*/


}
