import org.opencv.videoio.VideoWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

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
            String ip = "10.8.65.11";
            url = new URL("http://" + ip + "/axis-cgi/jpg/image.cgi?resolution=640x480");
            //http://IPADDRESS/?camid=[CHANNEL]
            //http://IPADDRESS/?action=stream
            //http://IPADDRESS/video.mjpg
            //http://IPADDRESS/videostream.asf?user=[USERNAME]&pwd=[PASSWORD]&resolution=[WIDTH]*[HEIGHT] for FFMPEG
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
                //System.out.println(e.getMessage());
            }
            if(frame != null) sendToWindow(frame);
            else sendError("no frame");
            long curTime = System.currentTimeMillis();
            sendFPS(curTime-lastUpdateTime);
            lastUpdateTime = curTime;
        }
    }


}
