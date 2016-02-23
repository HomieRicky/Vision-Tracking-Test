import org.opencv.core.MatOfPoint;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by Ricardo on 2016-02-22.
 */
public class FrameProcessHandler {
    private ArrayList<FrameProcessor> processes;
    private ArrayList<Future<List<MatOfPoint>>> matOfPointCollector;
    private ArrayList<List<MatOfPoint>> returns;
    private ArrayList<ExecutorService> services;
    private int threadsInUse = 0;
    private int threadLimit = 0;

    public FrameProcessHandler(int processLimit) {
        threadLimit = processLimit;
        processes = new ArrayList<>(processLimit);
        matOfPointCollector = new ArrayList<>(processLimit);
        services = new ArrayList<>(processLimit);
        returns = new ArrayList<>(processLimit);
    }

    public boolean addFrame(BufferedImage img) {
        if(threadsInUse == threadLimit) return false;

    }




}
