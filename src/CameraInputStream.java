import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Driver;
import java.util.concurrent.Callable;

/**
 * Created by Ricardo on 2016-02-19.
 */
public class CameraInputStream implements Runnable {

    URL url;
    DriverStream stream;
    private long lastUpdateTime;
    VideoWriter vw;
    long time;
    boolean ran = false;

    public CameraInputStream(DriverStream stream) {
        try {
            url = new URL("http://10.8.65.11/axis-cgi/jpg/image.cgi?resolution=640x480");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.stream = stream;
        lastUpdateTime = System.currentTimeMillis();
        //vw = new VideoWriter("save.avi", -1, 30, new Size(480, 640));
        time = System.currentTimeMillis();
    }

    public void sendToWindow(BufferedImage image) {
        stream.imgBuffer = image;
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
            BufferedImage frame = null;
            try {
                frame = ImageIO.read(url);
                System.out.println("Sent legit frame!");
            } catch (Exception e) {
                sendError(e.getMessage());
                System.out.println(e.getMessage());
            }
            if(frame != null) sendToWindow(frame);
            else sendError("no frame");
            long curTime = System.currentTimeMillis();
            sendFPS(curTime-lastUpdateTime);
            lastUpdateTime = curTime;
        }
    }


}
