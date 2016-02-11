import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Iterator;

/**
 * Created by Ricardo on 2016-02-10.
 */
public class test {
    public test(Mat matOriginal, MatOfPoint contour) {
        Rect rec = Imgproc.boundingRect(contour);
//				"fun" math brought to you by miss daisy (team 341)!
        double y = rec.br().y + rec.height / 2;
        y= -((2 * (y / matOriginal.height())) - 1);
        double distance = (TOP_TARGET_HEIGHT - TOP_CAMERA_HEIGHT) /
                Math.tan((y * VERTICAL_FOV / 2.0 + CAMERA_ANGLE) * Math.PI / 180);
//				angle to target...would not rely on this
        double targetX = rec.tl().x + rec.width / 2;
        targetX = (2 * (targetX / matOriginal.width())) - 1;
        double azimuth = normalize360(targetX*HORIZONTAL_FOV /2.0 + 0);
//				drawing info on target
        Point center = new Point(rec.br().x-rec.width / 2 - 15,rec.br().y - rec.height / 2);
        Point centerw = new Point(rec.br().x-rec.width / 2 - 15,rec.br().y - rec.height / 2 - 20);
        Imgproc.putText(matOriginal, "dist: "+(int)distance, center, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(0, 255, 0));
        Imgproc.putText(matOriginal, "az: "+(int)azimuth, centerw, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(0, 255, 0));
        Imgcodecs.imwrite("9F.jpg", matOriginal);
    }

    public static double normalize360(double angle){
        while(angle >= 360.0)
        {
            angle -= 360.0;
        }
        while(angle < 0.0)
        {
            angle += 360.0;
        }
        return angle;
    }


    //	Constants for known variables

//	the height to the top of the target in first stronghold is 97 inches

    public static final int TOP_TARGET_HEIGHT = 97;

//	the physical height of the camera lens

    public static final int TOP_CAMERA_HEIGHT = 9;


//	camera details, can usually be found on the datasheets of the camera

    public static final double VERTICAL_FOV = 36.2;

    public static final double HORIZONTAL_FOV = 47;

    public static final double CAMERA_ANGLE = 30;
}
