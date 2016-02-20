import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Ricardo on 2016-02-19.
 */
public class DriverStream extends JFrame {

    private static final int FRAME_OFFSET = 150;
    public Mat frame;
    public static String errText = "";
    public static float fps = 0;
    public JPanel panel;
    public JTextArea console;
    public JLabel image;

    VideoCapture capture;

    public static void main(String args[]) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat baseFrame = new Mat(480, 640, CvType.CV_8UC3, new Scalar(0, 255, 0));
        DriverStream driverStream = new DriverStream();
        CameraInputStream cameraStream = new CameraInputStream(driverStream.capture, driverStream);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Runnable camThread = cameraStream;
        executor.execute(camThread);
        errText = "Started";

        while(true) {
            long timer = System.currentTimeMillis();
            FrameDecoder fd;
            System.out.println(driverStream.frame.toString());
            if(!driverStream.frame.empty()) fd = new FrameDecoder(driverStream.frame);
            else {
                fd = new FrameDecoder(new Mat(480, 640, CvType.CV_8UC3, Scalar.all(128)));
                System.out.println("bad frame");
            }
            ExecutorService es = Executors.newSingleThreadExecutor();
            Future<java.util.List<MatOfPoint>> retrReciever;
            retrReciever = es.submit(fd);
            while (!retrReciever.isDone()) {
            }
            timer = System.currentTimeMillis() - timer;
            String latency = "Processing latency: " + String.valueOf(timer);
            List<MatOfPoint> targets;
            try {
                targets = retrReciever.get();
            } catch (InterruptedException e) {
                errText = e.getMessage();
                targets = new ArrayList<MatOfPoint>();
            } catch (ExecutionException e) {
                errText = e.getMessage();
                targets = new ArrayList<MatOfPoint>();
            }
            Trajectory t = new Trajectory(targets);
            double[] trajectoryVals = t.getTrajectory();

            Mat overlay = new Mat(driverStream.frame.rows(), driverStream.frame.cols(), CvType.CV_8UC4, new Scalar(0, 0, 0, 0));
            Imgproc.polylines(overlay, targets, true, new Scalar(255, 0, 0, 255), 1);
            Imgproc.line(overlay, new Point(overlay.cols() / 2, 0), new Point(overlay.cols() / 2, overlay.rows()), new Scalar(0, 0, 255, 255), 1);
            Imgproc.line(overlay, new Point(0, overlay.rows() / 2), new Point(overlay.cols(), overlay.rows() / 2), new Scalar(0, 0, 255, 255), 1);

            if(trajectoryVals.length == 0) errText += " No trajectory.";
            else {
                for (MatOfPoint point : targets) {
                    Imgproc.circle(overlay, t.getTargetPoint(point), 3, new Scalar(0, 0, 255, 200), 2);
                }
            }
            overtrayImage(driverStream.frame, overlay);
            driverStream.console.setText(errText + "\r\n" + latency + "\r\n FPS: " + String.valueOf(fps));
            if (!driverStream.frame.empty()) {
                ImageIcon iconImage = new ImageIcon(matToBufferedImage(driverStream.frame));
                driverStream.image.setIcon(iconImage);
            }
            driverStream.repaint();
        }

    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(matToBufferedImage(frame), 0, FRAME_OFFSET, this);

    }

    public DriverStream() {
        frame = new Mat(480, 640, CvType.CV_8UC3, Scalar.all(128));

        panel = new JPanel();
        console = new JTextArea("...", 10, 50);
        console.setRows(5);
        console.setLineWrap(true);;

        image = new JLabel(new ImageIcon(matToBufferedImage(frame)));

        add(image);
        add(console);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(640, 480+FRAME_OFFSET);
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

    private static Mat overtrayImage( Mat background, Mat foreground ) {
        // The background and the foreground are assumed to be of the same size.
        Mat destination = new Mat( background.size(), background.type() );

        for ( int y = 0; y < ( int )( background.rows() ); ++y ) {
            for ( int x = 0; x < ( int )( background.cols() ); ++x ) {
                double b[] = background.get( y, x );
                double f[] = foreground.get( y, x );

                double alpha = f[3] / 255.0;

                double d[] = new double[3];
                for ( int k = 0; k < 3; ++k ) {
                    d[k] = f[k] * alpha + b[k] * ( 1.0 - alpha );
                }

                destination.put( y, x, d );
            }
        }
        return destination;
    }
}
