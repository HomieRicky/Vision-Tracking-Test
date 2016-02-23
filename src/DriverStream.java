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

/**
 * Created by Ricardo on 2016-02-21.
 */
public class DriverStream extends JFrame {
    JLabel image;
    JLabel processedImage;
    JPanel panel;
    JLabel fpsLabel;
    JLabel processInfo;
    JTextArea console;
    JScrollPane consoleContainer;

    static USBCameraInputStream stream;
    static FrameProcessHandler processor;
    BufferedImage frameBuffer;
    BufferedImage processedFrameBuffer;
    boolean frameUpdated = false;
    boolean processedFrameUpdated = false;
    boolean sendData = false;
    String consoleBuffer = "";
    String processInfoBuffer = "";
    double azimuth = 0;
    double distance = 0;
    long timestamp = 0;
    long lastTimestamp = System.currentTimeMillis();
    int frameCounter = 0;
    int processLatency = 0;

    Object interruptedThread = null;


    public static void main(String args[]) throws InterruptedException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        DriverStream d = new DriverStream();
        stream = new USBCameraInputStream(d, 30);
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
                //Insert networktables code here
                d.processedFrameUpdated = false;
            }
            d.pack();
            if(d.interruptedThread != null) {
                System.out.println("Restarting " + d.interruptedThread.toString());
                if(d.interruptedThread instanceof FrameProcessHandler) {
                    processor = new FrameProcessHandler(1, d);
                    processHandlerThread = new Thread(processor);
                    processHandlerThread.start();
                } else if (d.interruptedThread instanceof USBCameraInputStream) {
                    stream = new USBCameraInputStream(d, 30);
                    camThread = new Thread(stream);
                    camThread.start();
                }
                d.interruptedThread = null;
            }
        }
    }

    public DriverStream() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        console = new JTextArea(5, 60);
        console.setRows(5);
        console.setLineWrap(true);
        console.setEditable(false);
        consoleContainer = new JScrollPane(console);
        consoleContainer.setMinimumSize(new Dimension(480, 80));
        consoleContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        consoleContainer.setPreferredSize(new Dimension(600, 100));
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
        panel.add(consoleContainer, gbc);
        gbc.gridx = 1;
        panel.add(processInfo, gbc);


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

    /*public static String trimLines(String in) {
        int index = 0;
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        while((index = in.indexOf("\r\n", index+1)) != -1) {
            indexes.add(index);
        }
        if(indexes.size() >= 5) {
            System.out.println("trim " + (indexes.size()-5));
            return in.substring(indexes.get(indexes.size()-5)+2);
        }
        return in;
    }*/

    public static String trimLines(String in) {
        int max = 5*60;
        if(in.length()>max) return in.substring(in.length()-max);
        return in;
    }

}
