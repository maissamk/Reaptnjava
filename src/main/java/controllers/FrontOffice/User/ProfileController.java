package controllers.FrontOffice.User;

import Models.user;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import services.FacePlusPlusService;
import services.UserServices;
import utils.CameraUtil;
import utils.NavigationUtil;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProfileController {
    @FXML private Label welcomeLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private ImageView avatarImage;
    @FXML private Button registerFaceButton;
    @FXML private ImageView cameraPreview;

    private final FacePlusPlusService faceService = new FacePlusPlusService();
    private boolean isCameraActive = false;
    private CameraUtil cameraUtil;
    private ScheduledExecutorService cameraExecutor;

    @FXML
    public void initialize() {
        loadUserProfile();
        cameraUtil = new CameraUtil();
    }

    private void loadUserProfile() {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getPrenom() + "!");
            fullNameLabel.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            emailLabel.setText(currentUser.getEmail());
            phoneLabel.setText(currentUser.getTelephone());
            roleLabel.setText(currentUser.getRoles());
            statusLabel.setText(currentUser.getStatus() != null ? currentUser.getStatus() : "Active");

            try {
                String avatarPath = currentUser.getAvatar();
                if (avatarPath == null || avatarPath.isEmpty()) {
                    avatarPath = "/images/defaultavatar.png";
                } else if (!avatarPath.startsWith("/images/avatars/")) {
                    avatarPath = "/images/avatars/" + avatarPath;
                }

                Image avatar = new Image(getClass().getResourceAsStream(avatarPath));
                avatarImage.setImage(avatar);
            } catch (Exception e) {
                System.err.println("Error loading avatar: " + e.getMessage());
                avatarImage.setImage(new Image(getClass().getResourceAsStream("/images/defaultavatar.png")));
            }
        }
    }

    @FXML
    private void handleRegisterFace() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/user/camera-popup.fxml"));
            Parent root = loader.load();
            CameraPopupController controller = loader.getController();

            // Set the callback for when image is captured
            controller.setOnCaptureCallback(capturedImage -> {
                if (capturedImage != null) {
                    processFaceRegistration(capturedImage);
                }
            });

            Stage dialog = new Stage();
            dialog.setTitle("Register Your Face");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(registerFaceButton.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Camera Error", "Could not open camera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processFaceRegistration(Image capturedImage) {
        registerFaceButton.setDisable(true);

        Task<Void> registrationTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    File tempFile = File.createTempFile("face-register", ".png");
                    CameraUtil.saveImageToFile(capturedImage, tempFile.getAbsolutePath());

                    String faceToken = faceService.detectFace(tempFile);

                    if (faceToken != null) {
                        user currentUser = SessionManager.getInstance().getCurrentUser();
                        currentUser.setFace_token(faceToken);

                        UserServices userService = new UserServices();
                        if (userService.updateUserFaceToken(currentUser.getId(), faceToken)) {
                            Platform.runLater(() ->
                                    showAlert(Alert.AlertType.INFORMATION, "Success",
                                            "Face Registered", "Your face has been successfully registered."));
                        } else {
                            Platform.runLater(() ->
                                    showAlert(Alert.AlertType.ERROR, "Error",
                                            "Registration Failed", "Failed to save face token."));
                        }
                    } else {
                        Platform.runLater(() ->
                                showAlert(Alert.AlertType.WARNING, "No Face Detected",
                                        "Try Again", "Please position your face clearly in the camera."));
                    }

                    tempFile.delete();
                } catch (Exception e) {
                    Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "Registration Failed", e.getMessage()));
                    e.printStackTrace();
                } finally {
                    Platform.runLater(() -> registerFaceButton.setDisable(false));
                }
                return null;
            }
        };

        new Thread(registrationTask).start();
    }

    @FXML
    private void handleEditProfile() {
        NavigationUtil.navigateTo("/FrontOffice/user/EditProfile.fxml", welcomeLabel);
    }

    @FXML
    private void handleBack() {
        NavigationUtil.navigateTo("/FrontOffice/Home.fxml", welcomeLabel);
    }

    @FXML
    private void handleUserList(ActionEvent actionEvent) {
        NavigationUtil.navigateTo("/BackOffice/user/UserList.fxml", welcomeLabel);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}