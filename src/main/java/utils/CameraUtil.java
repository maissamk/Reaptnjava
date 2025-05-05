package utils;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import javafx.scene.image.Image;

public class CameraUtil {
    private OpenCVFrameGrabber grabber;
    private JavaFXFrameConverter converter;
    private boolean isCameraActive;

    public CameraUtil() {
        this.converter = new JavaFXFrameConverter();
    }

    public void startCamera() throws Exception {
        if (grabber == null) {
            grabber = new OpenCVFrameGrabber(0); // 0 for default camera
            grabber.start();
            isCameraActive = true;
        }
    }

    public Frame grabFrame() throws Exception {
        if (isCameraActive && grabber != null) {
            return grabber.grab();
        }
        return null;
    }

    public Image convertFrameToImage(Frame frame) {
        return converter.convert(frame);
    }

    public void stopCamera() {
        try {
            if (grabber != null) {
                isCameraActive = false;
                grabber.stop();
                grabber.release();
                grabber = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean saveImageToFile(Image image, String filePath) {
        try {
            java.awt.image.BufferedImage bImage = javafx.embed.swing.SwingFXUtils.fromFXImage(image, null);
            java.io.File outputFile = new java.io.File(filePath);
            return javax.imageio.ImageIO.write(bImage, "png", outputFile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}