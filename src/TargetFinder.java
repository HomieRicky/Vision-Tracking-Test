import com.sun.media.jfxmedia.events.VideoTrackSizeListener;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricardo on 2016-01-26.
 */
public class TargetFinder {
    static final File IN_FILE = new File("image.jpg");
    static final File CONTOUR_FILE = new File("contour.jpg");
    static final File HSV_FILE = new File("hsv.jpg");

    public static void main(String args[]) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        for(int i = 9; i <= 9; i++) {
            String path = "C:\\Users\\Ricardo\\Desktop\\targ\\" + i + ".jpg";
            Process p = new Process(path);
            Thread t = new Thread(p);
            t.start();
        }
    }
}


class Process implements Runnable {
    String path;

    public Process(String filepath) {
        path = filepath;
    }

    @Override
    public void run() {
        Mat m = Imgcodecs.imread(path);
        Mat contours = new Mat(m.rows(), m.cols(), m.type());
        Mat hsv = new Mat(m.rows(), m.cols(), m.type());
        Mat blue = new Mat(m.rows(), m.cols(), CvType.CV_8UC3);
        Imgproc.applyColorMap(m, hsv, Imgproc.COLORMAP_HSV);
        byte array[] = new byte[m.rows() * m.cols() * m.channels()];
        byte blueA[] = new byte[m.rows() * m.cols() * blue.channels()];
        hsv.get(0, 0, array);
        for(int i = 0; i < array.length/3; i++) {
            blueA[i*3] = array[i*3];
            blueA[(i*3)+1] = 0;
            blueA[(i*3)+2] = 0;
        }
        blue.put(0, 0, blueA);
        List<MatOfPoint> points = new ArrayList<MatOfPoint>();
        Imgproc.threshold(blue, contours, 250, 1000, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        contours.get(0, 0, blueA);
        for(int i = 0; i < blueA.length; i++) {
            blueA[i] = (byte) (255-blueA[i]);
        }

        for(int i = 0; i < blueA.length/3; i++) {
            if(blueA[i*3] > 240) blueA[i*3] = 0;
        }
        contours.put(0, 0, blueA);
        Imgproc.cvtColor(contours, contours, Imgproc.COLOR_RGB2GRAY);
        Imgproc.findContours(contours, points, contours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        contours = new Mat(blue.rows(), blue.cols(), blue.type());
        System.out.println(points.size());
        for(int i = 0; i < points.size(); i++) {
            if(points.get(i).rows() > 750) {
                System.out.println("Point set " + i + ": " + points.get(i).toString());
                Imgproc.convex
                Imgproc.drawContours(contours, points, i, new Scalar(0, 0, 255), 2);
            }
        }
        //points.get(1).

        String cannyOutPath = path.substring(0, path.lastIndexOf("."));
        cannyOutPath += "A.jpg";
        String hsvPath = path.substring(0, path.lastIndexOf("."));
        hsvPath += "B.jpg";
        String bluePath = path.substring(0, path.lastIndexOf("."));
        bluePath += "C.jpg";
        Imgcodecs.imwrite(cannyOutPath, contours);
        Imgcodecs.imwrite(hsvPath, hsv);
        Imgcodecs.imwrite(bluePath, blue);
        System.out.println("Processed for file: " + path);
    }
}