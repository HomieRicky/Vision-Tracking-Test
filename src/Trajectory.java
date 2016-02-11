import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.io.*;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Ricardo on 2016-02-08.
 */
public class Trajectory {
    public static enum Constants {
        CAMERA_HEIGHT (0), //Height of camera above ground
        SHOOTER_DISTANCE (1), //Distance the shooter is in front of the camera
        SHOOTER_HEIGHT (2), //Height of the shooter exit point above ground
        TARGET_HEIGHT (3), //Height of the bottom of the target above ground
        TARGET_STRIP_Y (4), //Height of the reflective strip
        TARGET_STRIP_X (5), //Width of the reflective strip
        BALL_DIAMETER (6), //Diameter of the ball
        CAMERA_VERTICAL_FOV (7), //Field of view of the camera
        CAMERA_HORIZONTAL_FOV(8),
        CAMERA_ANGLE (9), //Angle of the centre of the camera from the ground
        CAMERA_PIXEL_WIDTH (10);

        public final int code;

        Constants(int i) {
            this.code = i;
        }
    }

    //All constant units are in inches and degrees
    public static double constants[] = new double[11];
    public static File constantsFile = new File("values.txt");
    public static boolean importedAleady = false;

    public List<MatOfPoint> points;

    public Trajectory(List<MatOfPoint> points) {
        this.points = points;
        if(!importedAleady) importValues();
    }

    public static void importValues() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(constantsFile));
            String line = "";
            int set = 0;
            while(br.ready()) {
                line = br.readLine();
                if(!line.startsWith("#") && !line.equals("")) {
                    String field = line.substring(0, line.indexOf("=")).trim();
                    try {
                        Field f = Constants.class.getDeclaredField(field);
                        constants[Constants.valueOf(field).code] = Double.parseDouble(line.substring(line.indexOf("=") + 1).trim());
                        set++;
                    } catch (NoSuchFieldException e) {
                        //Ignore
                    }
                }
            }
            System.out.println("Imported " + set + " values.");
            if(set < constants.length) System.out.println("Warning: not all values are set!");
            for(int i = 0; i < constants.length; i++) System.out.print(constants[i] + ", ");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void importValues(String path) {
        constantsFile = new File(path);
        importValues();
    }

    public double[] getTrajectory() {   //Returns 3 vars: Angle to turn (+Right), shooting angle, shooter PWM value
        Point[] targetPoints = new Point[points.size()];
        for(int i = 0; i < targetPoints.length; i++) targetPoints[i] = getTargetPoint(points.get(i));
        Point targetPoint = pickIdealTarget(targetPoints);
        double azimuth = (targetPoint.x-(constants[Constants.CAMERA_PIXEL_WIDTH.code]/2)/);
    }

    public boolean isVerticalLine(Point a, Point b) { //Args: 2 Points to form a line
        if(Math.abs(b.x-a.x) < Math.abs(b.y-a.y)) return true;
        return false;
    }

    public Point getTargetPoint(MatOfPoint m) {
        Point retrPoint = null;
        Point[] pointArray = m.toArray();
        for(int i = 0; i < pointArray.length; i++) {
            if(!isVerticalLine(pointArray[i], FrameDecoder.getNext(pointArray, i))) {
                Point p = new Point(((pointArray[i].x+FrameDecoder.getNext(pointArray, i).x))/2,
                        ((pointArray[i].y+FrameDecoder.getNext(pointArray, i).y))/2);
                if(retrPoint == null) retrPoint = p;
                else if(retrPoint.y > p.y) retrPoint = p;
            }

        }
        return retrPoint;
    }

    public Point pickIdealTarget(Point[] candidates) {
        int retrIndex = 0;
        for(int i = 0; i < candidates.length; i++) {
            if(Math.abs(candidates[i].x-constants[Constants.CAMERA_PIXEL_WIDTH.code]/2) < Math.abs(candidates[retrIndex].x-constants[Constants.CAMERA_PIXEL_WIDTH.code]/2)) {
                retrIndex = i;
            }
        }
        return candidates[retrIndex];
    }



}
