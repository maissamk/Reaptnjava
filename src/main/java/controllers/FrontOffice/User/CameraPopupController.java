package controllers.FrontOffice.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CameraPopupController {
    @FXML private ImageView cameraPreview;
    @FXML private Button captureButton;
    @FXML private Button cancelButton;

    private OpenCVFrameGrabber grabber;
    private JavaFXFrameConverter converter = new JavaFXFrameConverter();
    private ScheduledExecutorService cameraExecutor;
    private Consumer<Image> onCaptureCallback;


    public void setOnCaptureCallback(Consumer<Image> callback) {
        this.onCaptureCallback = callback;
    }

    @FXML
    public void initialize() {
        startCamera();

        captureButton.setOnAction(e -> {
            Image capturedImage = cameraPreview.getImage();
            stopCamera();
            if (onCaptureCallback != null) {
                onCaptureCallback.accept(capturedImage);
            }
            captureButton.getScene().getWindow().hide();
        });

        cancelButton.setOnAction(e -> {
            stopCamera();
            cancelButton.getScene().getWindow().hide();
        });
    }

    private void startCamera() {
        try {
            grabber = new OpenCVFrameGrabber(0);
            grabber.start();

            cameraExecutor = Executors.newSingleThreadScheduledExecutor();
            cameraExecutor.scheduleAtFixedRate(this::updateCameraView, 0, 33, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCameraView() {
        try {
            Frame frame = grabber.grab();
            if (frame != null) {
                Image imageToShow = converter.convert(frame);
                javafx.application.Platform.runLater(() ->
                        cameraPreview.setImage(imageToShow));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopCamera() {
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
            try {
                cameraExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}