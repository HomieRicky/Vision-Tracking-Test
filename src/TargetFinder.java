import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoWriter;

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
            File fpath = new File("targ\\" + i + ".jpg");
            String path = fpath.getAbsolutePath();
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
        List<MatOfInt> hulls = new ArrayList<MatOfInt>();
        List<MatOfInt4> convexityDefects = new ArrayList<MatOfInt4>();
        List<MatOfPoint> hullPointMats = new ArrayList<MatOfPoint>();
        List<MatOfPoint> simpleHullPointMats = new ArrayList<MatOfPoint>();
        for(int i = 0; i < points.size(); i++) {
            if(points.get(i).rows() > 750) {
                System.out.println("Point set " + i + ": " + points.get(i).toString());

                MatOfInt hull = new MatOfInt();
                Imgproc.convexHull(points.get(i), hull);
                hulls.add(hull);
                //System.out.print("Hull points: " + hull.dump());

                MatOfInt4 cd = new MatOfInt4();
                Imgproc.convexityDefects(points.get(i), hull, cd);
                convexityDefects.add(cd);
                //System.out.print(" | Defect points: " + cd.dump() + "\n");

                MatOfPoint hullPoints = generateHullPointMat(hull, points.get(i));
                hullPointMats.add(hullPoints);

                Imgproc.drawContours(contours, points, i, new Scalar(0, 0, 255), 2);
                Imgproc.polylines(contours, hullPointMats, false, new Scalar(0, 255, 0), 2);
                for(int j = 0; j < hullPoints.rows(); j++) {
                    Imgproc.circle(contours, hullPoints.toArray()[j], 5, new Scalar(255, 255, 255), 3);
                }
                MatOfPoint simpleHull = generateSimpleConvexHull(hullPoints);
                double angleSum = 0;
                Point[] simpleHullA = simpleHull.toArray();
                for(int j = 0; j < simpleHull.rows(); j++) {
                    Imgproc.circle(contours, simpleHull.toArray()[j], 12, new Scalar(255, 150, 0), 2);
                    Point a = getLast(simpleHullA, j);
                    Point b = simpleHullA[j];
                    Point c = getNext(simpleHullA, j);
                    double angleAB = Math.toDegrees(Math.atan2(b.y-a.y, b.x-a.x));
                    double angleBC = Math.toDegrees(Math.atan2(c.y-b.y, c.x-b.x));
                    double angle = Math.abs(angleBC-angleAB);
                    Imgproc.putText(contours, String.valueOf((float) angle), b, Core.FONT_ITALIC, 1, Scalar.all(255), 2);
                    if(angle > 180) angle-=180;
                    angleSum += angle;
                }
                if(simpleHull.rows() == 4 && angleSum < 360) simpleHullPointMats.add(simpleHull);
                Imgproc.putText(contours, String.valueOf((float) angleSum), new Point(simpleHullA[0].x-100, simpleHullA[0].y+100), Core.FONT_ITALIC, 1, new Scalar(255, 128, 128), 3);
                System.out.println("Shape interior angle sum: " + angleSum);
            }
        }
        Mat fourPoint = new Mat(blue.rows(), blue.cols(), blue.type());
        Imgproc.fillPoly(fourPoint, simpleHullPointMats, Scalar.all(255));
        String fourPointOutPath = path.substring(0, path.lastIndexOf("."));
        fourPointOutPath += "D.jpg";
        String cannyOutPath = path.substring(0, path.lastIndexOf("."));
        cannyOutPath += "C.jpg";
        String hsvPath = path.substring(0, path.lastIndexOf("."));
        hsvPath += "A.jpg";
        String bluePath = path.substring(0, path.lastIndexOf("."));
        bluePath += "B.jpg";
        Imgcodecs.imwrite(cannyOutPath, contours);
        Imgcodecs.imwrite(hsvPath, hsv);
        Imgcodecs.imwrite(bluePath, blue);
        Imgcodecs.imwrite(fourPointOutPath, fourPoint);
        //VIDEO OUTPUT
/*
        VideoWriter vw = new VideoWriter(path.substring(0, path.lastIndexOf(".")) + ".avi", -1, 1, new Size(blue.width(), blue.height()));
        Imgproc.putText(m, "ORIGINAL", new Point(100, 300), Core.FONT_HERSHEY_SIMPLEX, 3, Scalar.all(150), 5);
        vw.write(m);
        Imgproc.putText(hsv, "HSV MAP", new Point(100, 300), Core.FONT_HERSHEY_SIMPLEX, 3, Scalar.all(0), 5);
        vw.write(hsv);
        Imgproc.putText(blue, "FILTERED BLUE", new Point(100, 300), Core.FONT_HERSHEY_SIMPLEX, 3, Scalar.all(255), 5);
        vw.write(blue);
        Imgproc.putText(contours, "CONTOURS", new Point(100, 300), Core.FONT_HERSHEY_SIMPLEX, 3, Scalar.all(255), 5);
        vw.write(contours);
        Imgproc.putText(fourPoint, "SIMPLIFIED FOUR-EDGE SHAPES", new Point(100, 300), Core.FONT_HERSHEY_SIMPLEX, 3, Scalar.all(255), 5);
        vw.write(fourPoint);
        vw.release();
        */
        System.out.println("Processed for file: " + path);
    }

    private MatOfPoint generateHullPointMat(MatOfInt hull, MatOfPoint matOfPoint) {
        int hullArray[] = hull.toArray();
        Point pointArray[] = matOfPoint.toArray();
        List<Point> hullPoints = new ArrayList<>();
        for(int i = 0; i < hullArray.length; i++) {
            //System.out.println(hullArray[i] + "|" + pointArray[i].toString());
            hullPoints.add(pointArray[hullArray[i]]);
        }
        MatOfPoint retr = new MatOfPoint();
        retr.fromList(hullPoints);
        //System.out.println(retr.toString());
        return retr;
    }

    private MatOfPoint generateSimpleConvexHull(MatOfPoint mp) {
        double minAngle = 180;
        boolean runFirstTime = true;
        int minAngleIndex = 0;
        while(minAngle < 30 || runFirstTime) {
            minAngle = 180;
            Point[] points = mp.toArray();
            for (int i = 0; i < points.length; i++) {
                Point a = getLast(points, i);
                Point b = points[i];
                Point c = getNext(points, i);
                double angleAB = Math.toDegrees(Math.atan2(b.y-a.y, b.x-a.x));
                double angleBC = Math.toDegrees(Math.atan2(c.y-b.y, c.x-b.x));
                double angle = Math.abs(angleBC-angleAB);
                if(angle > 180) angle-=180;
                //System.out.println(angle);
                if(angle < minAngle) {
                    minAngleIndex = i;
                    minAngle = angle;
                    //System.out.println("New minAngle: " + minAngle + " at " + minAngleIndex);
                }
            }
            //System.out.println("Lowest angle at " + minAngleIndex);
            //System.out.println("Points[].length = " + points.length);
            if(minAngle < 30) {
                Point[] newPoints = new Point[points.length-1];
                int subtractor = 0;
                for(int j = 0; j < points.length; j++) {
                    if(j == minAngleIndex) subtractor++;
                    else newPoints[j-subtractor] = points[j];
                }
                mp = new MatOfPoint(newPoints);
            }
            runFirstTime = false;
        }
        boolean intersectingPoints = true;
        while(intersectingPoints) {
            intersectingPoints = false;
            Point[] points = mp.toArray();
            for (int i = 0; i < points.length; i++) {
                Point a = points[i];
                Point b = getNext(points, i);
                double distance = Math.sqrt(Math.pow(Math.abs(b.x-a.x), 2) + Math.pow(Math.abs(b.y-a.y), 2));
                if(distance < 10) {
                    intersectingPoints = true;
                    Point[] newPoints = new Point[points.length-1];
                    int subtractor = 0;
                    for(int j = 0; j < points.length; j++) {
                        if(j == i) subtractor++;
                        else newPoints[j-subtractor] = points[j];
                    }
                    mp = new MatOfPoint(newPoints);
                }
            }
            System.out.println("Points[].length = " + points.length);
        }
        return mp;
    }

    private <T> T getLast(T[] array, int curIndex) {
        if(curIndex == 0) return array[array.length-1];
        return array[curIndex-1];
    }
    private <T> T getNext(T[] array, int curIndex) {
        if(curIndex == array.length-1) return array[0];
        return array[curIndex+1];
    }


}