import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Ricardo on 2016-02-21.
 */
public class DriverStream extends JFrame implements ITableListener {
    public static final String VERSION = "Prerelease-20160225a";

    JLabel image;
    JLabel processedImage;
    JPanel panel;
    JLabel fpsLabel;
    JLabel processInfo;
    JPanel consoles;
    JTextArea console;
    JScrollPane consoleContainer;
    JTextArea roboConsole;
    JScrollPane roboConsoleContainer;

    static NetworkTable visionTbl;
    static NetworkTable modeTbl;

    static USBCameraInputStream stream;
    static FrameProcessHandler processor;
    BufferedImage frameBuffer;
    BufferedImage processedFrameBuffer;
    boolean frameUpdated = false;
    boolean processedFrameUpdated = false;
    boolean sendData = false;
    static boolean modeChanged = false;
    static int mode = 0;
    String consoleBuffer = "";
    static String roboConsoleBuffer = "";
    String processInfoBuffer = "";
    double azimuth = 0;
    double distance = 0;
    long timestamp = 0;
    long lastTimestamp = System.currentTimeMillis();
    int frameCounter = 0;
    int processLatency = 0;

    Object interruptedThread = null;
    boolean usingLocalCamera = false;

    int flash = 0;

    static InetAddress robotAddress;

    public static void main(String args[]) throws InterruptedException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        DriverStream d = new DriverStream();
        attemptConnect(d);
        //initNetworkTables(d);
        //stream = new USBCameraInputStream(d, true, 1);
        processor = new FrameProcessHandler(1, d);
        Thread processHandlerThread = new Thread(processor);
        processHandlerThread.start();
        Thread camThread = new Thread(stream);
        camThread.start();

        while(true) {
            d.consoleBuffer = trimLines(d.consoleBuffer);
            d.console.setText(d.consoleBuffer);
            d.consoleContainer.getVerticalScrollBar().setValue(d.consoleContainer.getVerticalScrollBar().getMaximum());
            if(d.frameUpdated) {
                d.image.setIcon(new ImageIcon(d.frameBuffer));
                d.processor.addFrame(d.frameBuffer);
                d.frameCounter++;
                long time = d.timestamp;
                long interval;
                if((interval = time-d.lastTimestamp) >= 1000) {
                    double fps = (d.frameCounter*1.0)/(interval/1000.0);
                    d.fpsLabel.setText("FPS: " + (float) fps);
                    d.lastTimestamp = time;
                    d.frameCounter = 0;
                }
                d.frameUpdated = false;
            }
            if(d.processedFrameUpdated) {
                d.processedImage.setIcon(new ImageIcon(d.processedFrameBuffer));
                d.processInfo.setText("Azimuth: " + d.azimuth + " | Distance: " + d.distance + " | Latency: " + d.processLatency
                        + " | Threads in use: " + d.processor.getThreadsInUse() + " | " + d.processInfoBuffer);
                d.processedFrameUpdated = false;
            }
            if(d.interruptedThread != null) {
                System.out.println("Restarting " + d.interruptedThread.toString());
                System.out.println("d = " + d.interruptedThread.getClass().getTypeName());
                if(d.interruptedThread.getClass().getName().contains("FrameProcessHandler")) {
                    System.out.println("Thread is instance of FrameProcessHandler");
                    processor = new FrameProcessHandler(1, d);
                    processHandlerThread = new Thread(processor);
                    processHandlerThread.start();
                } else if (d.interruptedThread.getClass().getName().contains("USBCameraInputStream")) {
                    System.out.println("Thread is instance of USBCameraInputStream");
                    if(d.usingLocalCamera) stream = new USBCameraInputStream(d, true, USBCameraInputStream.testLocalCameraPorts(1));
                    else stream = new USBCameraInputStream(d, 30);
                    camThread = new Thread(stream);
                    camThread.start();
                }
                d.interruptedThread = null;
            }
            d.roboConsole.setText(roboConsoleBuffer);
            if(modeChanged) {
                switch(d.mode) {
                    case 0: d.panel.setBackground(Color.LIGHT_GRAY); break;
                    case 1: d.panel.setBackground(Color.GREEN); break;
                    case 2: d.panel.setBackground(Color.YELLOW); break;
                    case 3: d.panel.setBackground(Color.RED); break;
                    case 4: d.flash++; break;
                }
                if(d.mode != 4) d.flash = 0;
                modeChanged = false;
            }
            if(d.flash > 0) {
                if(d.flash == 1) { d.panel.setBackground(Color.RED); d.flash++; }
                else if(d.flash == 2) { d.panel.setBackground(Color.GREEN); d.flash--; }
            }
        }
    }

    public DriverStream() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        consoles = new JPanel(new GridLayout(2, 1));
        console = new JTextArea(5, 60);
        console.setRows(5);
        console.setLineWrap(true);
        console.setEditable(false);
        consoleContainer = new JScrollPane(console);
        consoleContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        consoleContainer.setPreferredSize(new Dimension(600, 100));
        roboConsole = new JTextArea(5, 60);
        roboConsole.setRows(5);
        roboConsole.setLineWrap(true);
        roboConsole.setEditable(false);
        roboConsole.setBackground(Color.LIGHT_GRAY);
        roboConsole.setDisabledTextColor(Color.WHITE);
        roboConsoleContainer = new JScrollPane(roboConsole);
        roboConsoleContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        roboConsoleContainer.setPreferredSize(new Dimension(600, 100));
        consoles.add(consoleContainer);
        consoles.add(roboConsoleContainer);
        processInfo = new JLabel();
        fpsLabel = new JLabel("FPS: 0    Bitrate: 0 Mbps");
        image = new JLabel();
        processedImage = new JLabel();
        try {
            image.setIcon(new ImageIcon(ImageIO.read(new File("logo.jpg"))));
            processedImage.setIcon(new ImageIcon(ImageIO.read(new File("logoBlack.jpg"))));
        } catch (IOException e) {
            consoleBuffer += "Failed to load logo files.";
        }
        processInfo.setText("Azimuth:   Distance:   Threads in use: ");

        add(panel);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(image, gbc);
        gbc.gridx = 1;
        panel.add(processedImage, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(fpsLabel, gbc);
        gbc.gridy = 2;
        panel.add(consoles, gbc);
        gbc.gridx = 1;
        panel.add(processInfo, gbc);

        setTitle("WarpGUI ver. " + VERSION);
        panel.setBackground(Color.LIGHT_GRAY);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

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

    public static String trimLines(String in) {
        int max = 5*60;
        if(in.length()>max) return in.substring(in.length()-max);
        return in;
    }

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
}
