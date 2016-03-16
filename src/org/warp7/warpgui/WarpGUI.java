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
    private static final String VERSION = "prerelease-20160316a";
    public static NetworkTable robotTables, robot, processingOutputs;
    public static NetworkTableListener robotStandardListener;
    public static JTabbedPane tabs;
    public static JPanel autoPanel, optionsPanel;
    public static MainPanel mainPanel;
    public static VisionTestPanel visionTestPanel;
    public static TestPanel testPanel;

    //DEFAULTS
    public static final File CONFIG_DEFAULT_PATH = new File("values.txt");
    public static final File SETTINGS_DEFAUlT_PATH = new File("settings.txt");
    public static String LOG_DEFAULT_PATH;
    public static String DEFAULT_IP = "roborio-865-frc.local";
    public static int DEFAULT_PORT = 1180;
    public static int LOCAL_CAM_PORT = 0;

    //STORAGE
    public static volatile BufferedImage frameBuffer = null;
    public static volatile boolean frameUpdated = false;
    public static volatile long timestamp = System.currentTimeMillis();
    public static long lastTimestamp = 0;
    public static int frameCounter = 0;
    public static float fps = 0;
    public static long processStartTime = 0;
    public static long processLatency = 0;
    static USBCameraInputStream stream;
    //SENDING TO ROBOT
    static double azimuth = 0;
    static double dist = 0;
    static boolean dataSendable = false;
    static String info = "";
    static boolean thruNetwork = false;
    static boolean ntEnabled = false;



    //NON-STATICS

    public static void main(String args[]) throws URISyntaxException {
        final File ldp = new File(WarpGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        LOG_DEFAULT_PATH = ldp.getAbsolutePath().substring(0, ldp.getAbsolutePath().lastIndexOf("\\"));

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                //e.printStackTrace();
                WarpGUI.mainPanel.GUIconsole.addText(e.getLocalizedMessage());
                /*try {
                    restartApplication();
                } catch (URISyntaxException e1) {
                    System.exit(1);
                } catch (IOException e1) {
                    System.exit(1);
                }*/
            }
        });

        main = new WarpGUI();
        /*
        INSTRUCTIONS:
        TRY USING attemptConnect()
        IF IT DOESNT WORK, USE THE SECOND STREAM INITIALIZER BELOW AND USE initNetworkTables()
        THE FIRST STREAM INITIALIZER IS FOR A LOCAL CAMERA ON THE LAPTOP
        DO NOT INITIALIZE NETWORKTABLES IF YOU'RE NOT CONNECTED TO THE ROBOT
        -Ricardo
         */
        attemptConnect();
        //stream = new USBCameraInputStream(true, 0);
        //stream = new USBCameraInputStream(30);
        //initNetworkTables();
        testPanel.updateTable("data/intake", "intake", 1.0);
        testPanel.updateTable("data/drive", "leftMotor", 0.5);
        testPanel.updateTable("data/drive", "rightMotor", 0.7);
        testPanel.updateTable("data/intake", "intake", 0.9);
        Thread streamThread = new Thread(stream);
        streamThread.start();
        mainPanel.GUIconsole.addText("Connected to camera");
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
                        frameCounter++;
                        if(frameCounter >= 5) {
                            fps = (float) ((timestamp-lastTimestamp)/15);
                            lastTimestamp = timestamp;
                            frameCounter = 0;
                        }
                        if(!doingTask) {
                            processor = new FrameProcessor(bufferedImageToMat(frameBuffer));
                            es = Executors.newSingleThreadExecutor();
                            futureTask = es.submit(processor);
                            doingTask = true;
                            processStartTime = System.currentTimeMillis();
                        } else {
                            try {
                                if (futureTask.isDone()) {
                                    List<MatOfPoint> retr = new ArrayList<>();
                                    try {
                                        retr = futureTask.get();
                                    } catch(ArrayIndexOutOfBoundsException e) {
                                        //System.out.println("no targets");
                                    }
                                    System.out.println(retr.size());
                                    Mat m = processor.m;
                                    Mat overlay = new Mat(m.rows(), m.cols(), CvType.CV_8UC4, Scalar.all(0));
                                    if(retr.size() != 0) {
                                        Imgproc.polylines(overlay, retr, true, new Scalar(255, 0, 0, 255), 4);
                                        Imgproc.fillPoly(overlay, retr, new Scalar(255, 255, 255, 64));
                                        Trajectory t = new Trajectory(retr);
                                        double[] trajectoryVals = t.getTrajectory();
                                        if (trajectoryVals.length == 2) {
                                            for (MatOfPoint target : retr)
                                                Imgproc.circle(overlay, t.getTargetPoint(target), 5, new Scalar(0, 255, 0, 255), 2);
                                            Imgproc.circle(overlay, t.idealPoint, 5, new Scalar(0, 0, 255, 255), 2);
                                            azimuth = trajectoryVals[0];
                                            dist = trajectoryVals[1];
                                            dataSendable = true;
                                            info = "Target found!";
                                        } else if (trajectoryVals == new double[]{-2}) {
                                            info = "Targets spotted. None shootable.";
                                        }
                                    } else {
                                        info = "No targets.";
                                    }
                                    m = FrameProcessor.overtrayImage(m, overlay);
                                    mainPanel.processWindow.setIcon(new ImageIcon(matToBufferedImage(m)));
                                }
                            } catch(NullPointerException e) {
                                mainPanel.GUIconsole.addText(e.getLocalizedMessage() + " in main loop.");
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                mainPanel.GUIconsole.addText(e.getLocalizedMessage() + " in main loop.");
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                mainPanel.GUIconsole.addText(e.getLocalizedMessage() + " in main loop.");
                                e.printStackTrace();
                            }
                            doingTask = false;
                            processLatency = System.currentTimeMillis()-processStartTime;
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
            //Any generic loop stuff here
            mainPanel.streamStatus.setText("Status: " + info + " | FPS: " + fps + " | Dist: " + dist + " | Azimuth: " + azimuth + " | Latency: " + processLatency);
            if(ntEnabled && dataSendable) {
                processingOutputs.putNumber("distance", dist);
                processingOutputs.putNumber("azimuth", azimuth);
                dataSendable = false;
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
        setPreferredSize(new Dimension(1920, 840));
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


    private static void initNetworkTables() {
        NetworkTable.setClientMode();
        NetworkTable.setIPAddress(DEFAULT_IP);
        processingOutputs = NetworkTable.getTable("vision");
        robotTables = NetworkTable.getTable("status");
        robotStandardListener = new NetworkTableListener();
        robotTables.addTableListener(robotStandardListener);
        processingOutputs.addTableListener(robotStandardListener);
        ntEnabled = true;
    }

    public static void attemptConnect() {
        InetAddress robotAddress;
        try {
            robotAddress = InetAddress.getByName(DEFAULT_IP);
            if(robotAddress.isReachable(5000)) {
                stream = new USBCameraInputStream(30);
                thruNetwork = true;
                initNetworkTables();
            } else {
                thruNetwork = false;
                stream = new USBCameraInputStream(true, LOCAL_CAM_PORT);
            }
        } catch (UnknownHostException e) {
            stream = new USBCameraInputStream(true, LOCAL_CAM_PORT);
            WarpGUI.mainPanel.GUIconsole.addText("Error looking for " + DEFAULT_IP + ". Using local camera.");
            thruNetwork = false;
        } catch (IOException e) {
            stream = new USBCameraInputStream(true, LOCAL_CAM_PORT);
            WarpGUI.mainPanel.GUIconsole.addText("Error looking for " + DEFAULT_IP + ". Using local camera.");
            thruNetwork = false;
        }
    }
}
