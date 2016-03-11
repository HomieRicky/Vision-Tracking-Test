package org.warp7.warpgui;

import org.opencv.core.Mat;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.warp7.warpgui.tabs.VisionTestPanel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Arrays;

/**
 * Created by Ricardo on 2016-02-22.
 */
public class USBCameraInputStream implements Runnable {


    //Uses protocols from SmartDashboard and GRIP
    private final static int PORT = WarpGUI.DEFAULT_PORT;
    private final static String ADRESS = WarpGUI.DEFAULT_IP;
    private final static byte[] MAGIC_NUMBERS = {0x01, 0x00, 0x00, 0x00};
    private final static int HW_COMPRESSION = -1;
    private final static int SIZE_640x480 = 0;

    private byte[] dataBuffer = new byte[64 * 1024];
    private byte[] magicNumbersBuffer = new byte[4];
    private int fps;

    private boolean useLocal = false;
    private VideoCapture localCapturer;

    private boolean shutdown = false;

    public USBCameraInputStream(int fps) {
        this.fps = fps;
    }

    public USBCameraInputStream(boolean useLocal, int port) {
        this.useLocal = true;
        localCapturer = new VideoCapture(port);
    }



    @Override
    public void run() {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                sendMsg("Uncaught exception in USB camera stream: " + e.getMessage());
                shutdownThread();
                InterruptionHandler.add(this);
            }
        });
        while(!shutdown) {
            try {
                if(useLocal) {
                    Mat m = new Mat();
                    //sendMsg("Exp : " + localCapturer.get(Videoio.CAP_PROP_EXPOSURE));
                    //localCapturer.set(Videoio.CAP_PROP_GAMMA, VisionTestPanel.sliderVal);
                    //sendMsg("Exp : " + localCapturer.get(Videoio.CAP_PROP_GAMMA));
                    localCapturer.read(m);
                    sendFrame(WarpGUI.matToBufferedImage(m), m.rows() * m.cols() * m.channels());
                } else {
                    try (
                            Socket socket = new Socket(ADRESS, PORT);
                            DataInputStream in = new DataInputStream(socket.getInputStream());
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                        sendMsg("Connected to " + ADRESS + ":" + PORT);
                        out.writeInt(fps);
                        out.writeInt(HW_COMPRESSION);
                        out.writeInt(SIZE_640x480);

                        while (!Thread.currentThread().isInterrupted()) {
                            in.readFully(magicNumbersBuffer);
                            if (!Arrays.equals(magicNumbersBuffer, MAGIC_NUMBERS)) {
                                throw new IOException("Wrong magic numbers! Bad input.");
                            }
                            int imageSize = in.readInt();
                            dataBuffer = growIfNecessary(dataBuffer, imageSize);
                            in.readFully(dataBuffer, 0, imageSize);
                            //sendMsg("Got frame with " + imageSize + " bytes.");
                            sendFrame(ImageIO.read(new ByteArrayInputStream(dataBuffer)), imageSize);
                        }
                    } catch (IOException e) {
                        sendMsg(e.getMessage());
                        //e.printStackTrace();
                    } finally {
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException e) {
                sendMsg("Data thread interrupted!!");
            }
        }
    }

    public void shutdownThread() {
        shutdown = true;
    }

    public void sendFrame(BufferedImage img, int byteAmt) {
        try {
            WarpGUI.frameBuffer = img;
            WarpGUI.frameUpdated = true;
            WarpGUI.timestamp = System.currentTimeMillis();
            } catch(NullPointerException e) { sendMsg("Null BufferedImage!"); }
    }

    public void sendMsg(String msg) {
        WarpGUI.mainPanel.GUIconsole.addText(msg);
    }

    /**
     * Return an array big enough to hold least at least "capacity" elements.  If the supplied buffer is big enough,
     * it will be reused to avoid unnecessary allocations.
     */
    private byte[] growIfNecessary(byte[] buffer, int capacity) {
        if (capacity > buffer.length) {
            int newCapacity = buffer.length;
            while (newCapacity < capacity) {
                newCapacity *= 1.5;
            }
            sendMsg("Growing to " + newCapacity);
            return new byte[newCapacity];
        }

        return buffer;
    }

    /*
    public static int testLocalCameraPorts(int limit) {
        for(int i = limit; i >= 0; i--) {
            boolean b = false;
            try {
                VideoCapture tester = new VideoCapture(i);
                Mat m = new Mat();
                tester.read(m);
                BufferedImage testImg = WarpGUI.matToBufferedImage(m);
                b = true;
            } catch (Exception e) {
                b = false;
            }
            if(b) {
                System.out.println("Local cam" + i + " works.");
                return i;
            }
        }
        return -1;
    }
    */

}
