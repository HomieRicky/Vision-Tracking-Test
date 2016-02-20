import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

import java.sql.Driver;
import java.util.concurrent.Callable;

/**
 * Created by Ricardo on 2016-02-19.
 */
public class CameraInputStream implements Runnable {

    VideoCapture capturer;
    DriverStream stream;
    private long lastUpdateTime;
    VideoWriter vw;
    long time;
    boolean ran = false;

    public CameraInputStream(VideoCapture c, DriverStream stream) {
        try {
            capturer = new VideoCapture();
            capturer.open("http://10.8.65.11/mjpg/video.mjpg");
        } catch (UnsatisfiedLinkError e) {
            DriverStream.errText += "Failed to connect to camera!";
            System.out.println("Failed to connect to camera!");

        }
        this.stream = stream;
        lastUpdateTime = System.currentTimeMillis();
        vw = new VideoWriter("save.avi", -1, 30, new Size(480, 640));
        time = System.currentTimeMillis();
    }

    public void sendToWindow(Mat mat) {
        stream.frame = mat;
        DriverStream.errText = "";
    }

    public void sendError(String text) {
        DriverStream.errText = text;
    }

    public void sendFPS(long latency) {
        if(latency != 0) DriverStream.fps = 1/latency;
        else DriverStream.fps = 0;
    }

    @Override
    public void run() {
        while(true) {
            Mat frame = new Mat();
            try {
                capturer.read(frame);
                if(System.currentTimeMillis()-time < 10000) vw.write(frame);
                System.out.println(frame.get(0, 0));
                System.out.println("Sent legit frame!");
            } catch (UnsatisfiedLinkError e) {
                sendError(e.getMessage());
                System.out.println(e.getMessage());
            } catch (Exception e) {
                sendError(e.getMessage());
                System.out.println(e.getMessage());

            }
            if(System.currentTimeMillis()-time > 10000 && !ran) {
                ran = true;
                vw.release();
            }
            System.out.println(frame.size().area());
            if(frame.size().area()==0) sendToWindow(new Mat(480, 640, CvType.CV_8UC3, Scalar.all(128)));
            else sendToWindow(frame);
            long curTime = System.currentTimeMillis();
            sendFPS(curTime-lastUpdateTime);
            lastUpdateTime = curTime;
        }
    }


}
