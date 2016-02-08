import org.opencv.core.MatOfPoint;

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
        TARGET_HEIGHT (3), //Height of the centre of the target above ground
        TARGET_STRIP_Y (4), //Height of the reflective strip
        TARGET_STRIP_X (5), //Width of the reflective strip
        BALL_DIAMETER (6), //Diameter of the ball
        CAMERA_FOV (7), //Field of view of the camera
        CAMERA_ANGLE (8); //Angle of the centre of the camera from the ground

        public final int code;

        Constants(int i) {
            this.code = i;
        }
    }

    //All constant units are in inches and degrees
    public static double constants[] = new double[9];
    public static File constantsFile = new File("//values.txt");
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
                if(!line.startsWith("#")) {
                    String field = line.substring(0, line.indexOf("=")).trim();
                    try {
                        Field f = Constants.class.getDeclaredField(field);
                        constants[Constants.valueOf(field).code] = Double.parseDouble(line.substring(line.indexOf("=")).trim());
                        set++;
                    } catch (NoSuchFieldException e) {
                        //Ignore
                    }
                }
            }
            System.out.println("Imported " + set + " values.");
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



}
