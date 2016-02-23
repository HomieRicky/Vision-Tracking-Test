import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
        CAMERA_PIXEL_WIDTH (10),
        CAMERA_PIXEL_HEIGHT (11);

        public final int code;

        Constants(int i) {
            this.code = i;
        }
    }

    //All constant units are in inches and degrees
    public static double constants[] = new double[12];
    public static File constantsFile = new File("values.txt");
    public static boolean importedAleady = false;
    public static double minXToYTargetRatio;

    public List<MatOfPoint> points;
    public Point idealPoint;

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
            minXToYTargetRatio = (constants[Constants.BALL_DIAMETER.code]+4)/constants[Constants.TARGET_STRIP_Y.code];
            importedAleady = true;
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

    public double[] getTrajectory() {   //Returns 4 vars: Angle to turn (+Right), shooter angle (degrees from horizon), distance (in), height (in)
        if(points.isEmpty()) return new double[]{-1};

        ArrayList<Point> p = new ArrayList<>();
        for(int i = 0; i < points.size(); i++) if(!isShotTooNarrow(points.get(i))) p.add(getTargetPoint(points.get(i)));

        if(p.isEmpty()) return new double[]{-2};

        Point[] targetPoints = new Point[p.size()];
        for(int i = 0; i < p.size(); i++) targetPoints[i] = p.get(i);
        Point targetPoint = pickIdealTarget(targetPoints);

        double midX = constants[Constants.CAMERA_PIXEL_WIDTH.code]/2;
        double azimuth = ((targetPoint.x-midX)/constants[Constants.CAMERA_PIXEL_WIDTH.code])*constants[Constants.CAMERA_HORIZONTAL_FOV.code];
        double midY = constants[Constants.CAMERA_PIXEL_HEIGHT.code]/2;
        double angleFromCamera = (((midY-targetPoint.y)/constants[Constants.CAMERA_PIXEL_HEIGHT.code])*constants[Constants.CAMERA_VERTICAL_FOV.code])+constants[Constants.CAMERA_ANGLE.code];
        double distFromCamera = (constants[Constants.TARGET_HEIGHT.code]-constants[Constants.CAMERA_HEIGHT.code])/Math.tan(Math.toRadians(angleFromCamera));

        //double distFromShooter = distFromCamera-constants[Constants.SHOOTER_DISTANCE.code];
        //double heightFromShooter = constants[Constants.TARGET_HEIGHT.code]-constants[Constants.SHOOTER_HEIGHT.code];
        //double angleFromShooter = Math.toDegrees(Math.atan2(heightFromShooter, distFromShooter));

        return new double[]{azimuth, distFromCamera};
    }

    public boolean isVerticalLine(Point a, Point b) { //Args: 2 Points to form a line
        if(Math.abs(b.x-a.x) < Math.abs(b.y-a.y)) return true;
        return false;
    }

    public Point getTargetPoint(MatOfPoint m) {
        Point retrPoint = null;
        Point[] pointArray = m.toArray();
        for(int i = 0; i < pointArray.length; i++) {
            if(!isVerticalLine(pointArray[i], FrameProcessor.getNext(pointArray, i))) {
                Point p = new Point(((pointArray[i].x+FrameProcessor.getNext(pointArray, i).x))/2,
                        ((pointArray[i].y+FrameProcessor.getNext(pointArray, i).y))/2);
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
        idealPoint = candidates[retrIndex];
        return candidates[retrIndex];
    }

    public boolean isShotTooNarrow(MatOfPoint m) {
        Point[] mPoints = m.toArray();
        Point[] verticalPoints = new Point[2];
        Point[] horizontalPoints = new Point[2];
        if(isVerticalLine(mPoints[0], mPoints[1])) {
            verticalPoints = new Point[]{mPoints[0], mPoints[1]};
            horizontalPoints = new Point[]{mPoints[1], mPoints[2]};
        } else {
            horizontalPoints = new Point[]{mPoints[0], mPoints[1]};
            verticalPoints = new Point[]{mPoints[1], mPoints[2]};
        }
        if(Math.sqrt(Math.pow(horizontalPoints[1].y-horizontalPoints[0].y, 2) + Math.pow(horizontalPoints[1].x-horizontalPoints[0].x, 2))/
                (Math.sqrt(Math.pow(verticalPoints[1].y-verticalPoints[0].y, 2) + Math.pow(verticalPoints[1].x-verticalPoints[0].x, 2))) < minXToYTargetRatio) {
            return true;
        }
        return false;
    }


}
