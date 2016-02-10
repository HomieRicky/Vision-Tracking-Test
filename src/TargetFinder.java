import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Ricardo on 2016-01-26.
 */
public class TargetFinder {

    public static void main(String args[]) throws ExecutionException, InterruptedException {
        int i = 9;
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<List<MatOfPoint>> retrReciever;
        File fpath = new File("targ\\" + i + ".jpg");
        String path = fpath.getAbsolutePath();
        Mat frame = Imgcodecs.imread(path);
        FrameDecoder fd = new FrameDecoder(frame);
        retrReciever = es.submit(fd);
        while(!retrReciever.isDone()) {}
        List<MatOfPoint> points = retrReciever.get();
        Mat overlay = new Mat(frame.rows(), frame.cols(), CvType.CV_8UC4, new Scalar(0, 0, 0, 0));
        //Imgproc.fillPoly(overlay, points, new Scalar(0, 0, 255, 100));
        Imgproc.polylines(overlay, points, true, new Scalar(255, 0, 0, 255), 20);
        frame = overtrayImage(frame, overlay);
        Imgcodecs.imwrite(path.substring(0, path.lastIndexOf(".")) + "E.png", frame);
        System.out.println("Done!");
        Trajectory t = new Trajectory(points);
    }

    // http://stackoverflow.com/questions/21080722/merge-a-png-with-transparency-onto-another-image
    // Written by stackoverflow user "stack-o-frankie"
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




