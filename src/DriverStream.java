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
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ricardo on 2016-02-21.
 */
public class DriverStream extends JFrame {
    JLabel image;
    JLabel processedImage;
    JPanel panel;
    JTextArea console;
    JLabel infoLabel;
    static USBCameraInputStream stream;
    BufferedImage frameBuffer;
    boolean frameUpdated = false;
    String consoleBuffer = "";
    long timestamp = 0;
    long lastTimestamp = System.currentTimeMillis();
    int frameCounter = 0;


    public static void main(String args[]) throws InterruptedException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        DriverStream d = new DriverStream();
        stream = new USBCameraInputStream(d, 30);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Runnable camThread = stream;
        executor.execute(camThread);

        while(true) {
            d.consoleBuffer = trimLines(d.consoleBuffer);
            d.console.setText(d.consoleBuffer);
            //d.console.setRows(5);
            if(d.frameUpdated) {
                d.image.setIcon(new ImageIcon(d.frameBuffer));
                d.frameCounter++;
                long time = d.timestamp;
                long interval;
                if((interval = time-d.lastTimestamp) >= 1000) {
                    double fps = (d.frameCounter*1.0)/(interval/1000.0);
                    d.infoLabel.setText("FPS: " + (float) fps);
                    d.lastTimestamp = time;
                    d.frameCounter = 0;
                }
                d.frameUpdated = false;
            }
            d.pack();
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
        infoLabel = new JLabel("FPS: 0    Bitrate: 0 Mbps");
        image = new JLabel();
        processedImage = new JLabel();
        try {
            image.setIcon(new ImageIcon(ImageIO.read(new File("logo.jpg"))));
            processedImage.setIcon(new ImageIcon(ImageIO.read(new File("logoBlack.jpg"))));
        } catch (IOException e) {
            consoleBuffer += "Failed to load logo files.";
        }

        add(panel);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(image, gbc);
        gbc.gridx = 1;
        panel.add(processedImage, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(infoLabel, gbc);
        gbc.gridy = 2;
        panel.add(console, gbc);


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
        int newlineCount = 0;
        int index = 0;
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        while((index = in.indexOf("\r\n", index+1)) != -1) {
            newlineCount++;
            indexes.add(index);
        }
        if(newlineCount > 5) {
            return in.substring(indexes.get(newlineCount-5)+2);
        }
        return in;
    }

}
