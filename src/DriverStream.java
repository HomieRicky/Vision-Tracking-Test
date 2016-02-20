import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
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
    public BufferedImage imgBuffer = null;

    public static void main(String args[]) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat baseFrame = Imgcodecs.imread("logo.jpg");
        DriverStream driverStream = new DriverStream();
        CameraInputStream cameraStream = new CameraInputStream(driverStream);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Runnable camThread = cameraStream;
        executor.execute(camThread);
        errText = "Started";

        while(true) {
            if(driverStream.imgBuffer == null) driverStream.frame = baseFrame;
            else driverStream.frame = bufferedImageToMat(driverStream.imgBuffer);
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

            if(trajectoryVals.length == 0) errText = " No trajectory.";
            else {
                for (MatOfPoint point : targets) {
                    Imgproc.circle(overlay, t.getTargetPoint(point), 3, new Scalar(0, 0, 255, 200), 2);
                }
            }
            Mat displayFrame = new Mat(driverStream.frame.rows(), driverStream.frame.cols(), driverStream.frame.type());
            byte[] data = new byte[displayFrame.rows()*displayFrame.cols()*displayFrame.channels()];
            driverStream.frame.get(0, 0, data);
            displayFrame.put(0, 0, data);
            overtrayImage(displayFrame, overlay);
            driverStream.console.setText(errText + "\r\n" + latency + "\r\n FPS: " + String.valueOf(fps));
            BufferedImage buf = matToBufferedImage(displayFrame);
            ImageIcon icon = new ImageIcon(buf);
            driverStream.image.setIcon(icon);
            //driverStream.repaint();
        }

    }

    //@Override
    //public void paint(Graphics g) {
    //    g.drawImage(matToBufferedImage(frame), 0, FRAME_OFFSET, this);
    //}

    public DriverStream() {
        frame = new Mat(480, 640, CvType.CV_8UC3, Scalar.all(128));

        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        console = new JTextArea(10, 50);
        console.setRows(5);
        console.setLineWrap(true);
        console.setEditable(false);

        Mat tmpFrame = frame;
        BufferedImage buf = matToBufferedImage(tmpFrame);
        ImageIcon icon = new ImageIcon(buf);
        image = new JLabel(icon);

        add(panel);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(console, gbc);
        gbc.gridy = 1;
        panel.add(image, gbc);

        //console.setAlignmentY();


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //setSize(640, 480+FRAME_OFFSET);
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
