package utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CameraUtil {
    private static FrameGrabber grabber;

    public static Image captureImage() throws FrameGrabber.Exception {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        try {
            grabber.start();
            Frame frame = grabber.grab();
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            Java2DFrameConverter java2DConverter = new Java2DFrameConverter();
            return SwingFXUtils.toFXImage(java2DConverter.convert(frame), null);
        } finally {
            try {
                grabber.stop();
            } catch (Exception e) {
                System.err.println("Error stopping grabber: " + e.getMessage());
            }
        }
    }

    public static void saveImageToFile(Image image, String filePath) throws IOException {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        if (bImage != null) {
            File outputFile = new File(filePath);
            ImageIO.write(bImage, "png", outputFile);
        } else {
            throw new IOException("Failed to convert JavaFX Image to BufferedImage");
        }
    }
}