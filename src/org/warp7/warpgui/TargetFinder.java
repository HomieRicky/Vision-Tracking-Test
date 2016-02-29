package org.warp7.warpgui;

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
        TargetFinder tf = new TargetFinder();
    }

    public TargetFinder() throws ExecutionException, InterruptedException {
        int i = 9;
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<List<MatOfPoint>> retrReciever;
        File fpath = new File("targ\\" + i + ".jpg");
        String path = fpath.getAbsolutePath();
        Mat frame = Imgcodecs.imread(path);
        FrameProcessor fd = new FrameProcessor(frame);
        retrReciever = es.submit(fd);
        while(!retrReciever.isDone()) {}
        List<MatOfPoint> points = retrReciever.get();
        if(!points.isEmpty()) {
            Mat overlay = new Mat(frame.rows(), frame.cols(), CvType.CV_8UC4, new Scalar(0, 0, 0, 0));
            //Imgproc.fillPoly(overlay, points, new Scalar(0, 0, 255, 100));
            Imgproc.polylines(overlay, points, true, new Scalar(255, 0, 0, 255), 1);
            Imgproc.line(overlay, new Point(overlay.cols() / 2, 0), new Point(overlay.cols() / 2, overlay.rows()), new Scalar(0, 0, 255, 255), 1);
            Imgproc.line(overlay, new Point(0, overlay.rows() / 2), new Point(overlay.cols(), overlay.rows() / 2), new Scalar(0, 0, 255, 255), 1);

            Trajectory t = new Trajectory(points);
            for (MatOfPoint point : points) {
                Imgproc.circle(overlay, t.getTargetPoint(point), 3, new Scalar(0, 0, 255, 200), 2);
            }
            frame = FrameProcessor.overtrayImage(frame, overlay);
        }
        Imgcodecs.imwrite(path.substring(0, path.lastIndexOf(".")) + "E.png", frame);
        System.out.println("Done!");
    }


}




