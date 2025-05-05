package controllers.FrontOffice.User;

import Models.user;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.FacePlusPlusService;
import services.UserServices;
import utils.CameraUtil;
import utils.NavigationUtil;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;

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

    @FXML
    public void initialize() {
        loadUserProfile();
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
        registerFaceButton.setDisable(true);

        try {
            Image capturedImage = CameraUtil.captureImage();
            cameraPreview.setImage(capturedImage);

            File tempFile = File.createTempFile("face-capture", ".png");
            CameraUtil.saveImageToFile(capturedImage, tempFile.getAbsolutePath());

            String faceToken = faceService.detectFace(tempFile);

            if (faceToken != null) {
                user currentUser = SessionManager.getInstance().getCurrentUser();
                currentUser.setFace_token(faceToken);

                UserServices userService = new UserServices();
                if (userService.updateUserFaceToken(currentUser.getId(), faceToken)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Face Registered", "Your face has been successfully registered for login.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Registration Failed", "Failed to save face token to database.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "No Face Detected",
                        "Try Again", "Please position your face clearly in the camera.");
            }

            tempFile.delete();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Face Registration Failed", e.getMessage());
            e.printStackTrace();
        } finally {
            registerFaceButton.setDisable(false);
        }
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
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}