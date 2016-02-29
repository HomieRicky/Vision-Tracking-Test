package org.warp7.warpgui;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Ricardo on 2016-02-22.
 */
public class FrameProcessHandler implements Runnable {
    @Override
    public void run() {

    }
    /*
    private ArrayList<FrameProcessor> processes;
    private ArrayList<Future<List<MatOfPoint>>> matOfPointCollector;
    private ArrayList<Mat> matStorage;
    private ArrayList<ExecutorService> services;
    private ArrayList<Boolean> free;
    private ArrayList<Integer> collectOrder;
    private ArrayList<Long> timer;

    private int threadsInUse = 0;
    private int threadLimit = 0;
    private boolean shutdown = false;
    private WarpGUI driverStream;

    public FrameProcessHandler(int processLimit, WarpGUI d) {
        driverStream = d;
        threadLimit = processLimit;
        processes = new ArrayList<>(threadLimit);
        matOfPointCollector = new ArrayList<>(threadLimit);
        services = new ArrayList<>(threadLimit);
        free = new ArrayList<>(threadLimit);
        collectOrder = new ArrayList<>(threadLimit);
        matStorage = new ArrayList<>(threadLimit);
        timer = new ArrayList<>(threadLimit);
        for(int i = 0; i < threadLimit; i++) {
            services.add(i, Executors.newSingleThreadExecutor());
            free.add(i, true);
            collectOrder.add(i, -1);
            matStorage.add(i, new Mat());
            processes.add(i, null);
            matOfPointCollector.add(i, null);
            timer.add(i, null);
        }
        Trajectory.importValues();
    }

    public boolean addFrame(BufferedImage img) {
        if(threadsInUse == threadLimit) return false;
        //System.out.println("added frame");
        Mat m = WarpGUI.bufferedImageToMat(img);
        int id = assignProcess(m);
        matStorage.set(id, m);
        processes.set(id, new FrameProcessor(m));
        matOfPointCollector.set(id, services.get(id).submit(processes.get(id)));
        int order = 0;
        for(int i = 0; i < threadLimit; i++) {
            if(collectOrder.get(i) >= order) order = collectOrder.get(i) + 1;
        }
        collectOrder.set(id, order);
        free.set(id, false);
        timer.set(id, System.currentTimeMillis());
        threadsInUse++;
        return true;
    }

    public void run() {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                sendMsg("Uncaught exception in process handler: " + e.getMessage());
                shutdownThread();
                driverStream.interruptedThread = this;
            }
        });
        while(!shutdown) {
            for(int i = 0; i < threadLimit; i++) {
                try {
                    if (collectOrder.get(i) == 0 && matOfPointCollector.get(i).isDone()) {
                        Mat m = matStorage.get(i);
                        double a = 0;
                        double d = 0;
                        boolean h = false;
                        String infoText = "";

                        List<MatOfPoint> targets = matOfPointCollector.get(i).get();
                        if (!targets.isEmpty()) {
                            Trajectory t = new Trajectory(targets);
                            Mat overlay = new Mat(m.rows(), m.cols(), CvType.CV_8UC4, Scalar.all(0));
                            Imgproc.fillPoly(overlay, targets, new Scalar(255, 255, 255, 64));
                            Imgproc.polylines(overlay, targets, true, new Scalar(255, 0, 0, 255), 2);
                            double[] trajectoryVals = t.getTrajectory();
                            if (trajectoryVals.length == 2) {
                                for (MatOfPoint target : targets)
                                    Imgproc.circle(overlay, t.getTargetPoint(target), 5, new Scalar(0, 255, 0, 255), 2);
                                Imgproc.circle(overlay, t.idealPoint, 5, new Scalar(0, 0, 255, 255), 2);
                                a = trajectoryVals[0];
                                d = trajectoryVals[1];
                                h = true;
                            } else if (trajectoryVals == new double[]{-2}) {
                                infoText = "Targets spotted. None shootable.";
                            }

                            m = FrameProcessor.overtrayImage(m, overlay);
                            //m = overlay;
                        } else {
                            infoText = "No targets spotted.";
                        }
                        BufferedImage img = DriverStream.matToBufferedImage(m);
                        collectOrder.set(i, -1);
                        shiftOrder();
                        free.set(i, true);
                        threadsInUse--;
                        sendData(img, a, d, timer.get(i), h, infoText);
                    }
                } catch (InterruptedException e) {
                    sendMsg(e.getMessage());
                } catch (ExecutionException e) {
                    sendMsg(e.getMessage());
                } catch (IllegalArgumentException e) {
                    sendMsg(e.getMessage());
                }
            }
        }

    }

    private void shiftOrder() {
        for(int i = 0; i < threadLimit; i++) {
            int j;
            if((j = collectOrder.get(i)) > 0) collectOrder.set(i ,j-1);
        }
    }

    public void sendMsg(String msg) {
        driverStream.consoleBuffer += (msg + "\n");
        //System.out.println(msg);
    }

    public int getThreadsInUse() { return threadsInUse; }

    public void sendData(BufferedImage img, double azimuth, double dist, long startTime, boolean hasTargets, String info) {
        //System.out.println("Sent processed frame.");
        driverStream.processedFrameBuffer = img;
        driverStream.processInfoBuffer = info;
        driverStream.azimuth = azimuth;
        driverStream.distance = dist;
        driverStream.processLatency = (int) (System.currentTimeMillis()-startTime);
        driverStream.sendData = hasTargets;
        driverStream.processedFrameUpdated = true;
    }

    public void shutdownThread() {
        shutdown = true;
    }

    private int assignProcess(Mat m) {
        for(int i = 0; i < threadLimit; i++) {
            if(free.get(i)) return i;
        }
        return -1;
    }
*/
}
