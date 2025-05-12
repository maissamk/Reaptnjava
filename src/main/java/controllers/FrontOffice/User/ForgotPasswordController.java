package controllers.FrontOffice.User;

import Models.user;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import services.UserServices;
import utils.EmailSenderUser;
import utils.NavigationUtil;

import java.time.LocalDateTime;

public class ForgotPasswordController {
    @FXML private TextField emailField;
    @FXML private AnchorPane rootPane;
    @FXML private ImageView backgroundImage; // Add this if you have a background image in your FXML

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = stage.getScene();

            // Bind background image to window size if exists
            if (backgroundImage != null) {
                backgroundImage.fitWidthProperty().bind(scene.widthProperty());
                backgroundImage.fitHeightProperty().bind(scene.heightProperty());
            }

            // Make window full screen or maximized
            stage.setMaximized(true);

            // Focus on email field when window loads
            emailField.requestFocus();
        });
    }

    @FXML
    void handleResetRequest() {
        String email = emailField.getText().trim();

        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            NavigationUtil.showErrorAlert("Error", "Invalid Email", "Please enter a valid email address.");
            return;
        }

        UserServices userService = new UserServices();
        user user = userService.getUserByEmail(email);

        if (user != null) {
            String verificationCode = String.format("%06d", (int)(Math.random() * 1000000));
            LocalDateTime expiryTime = LocalDateTime.now().plusHours(24);

            if (userService.setPasswordResetToken(email, verificationCode, expiryTime)) {
                try {
                    EmailSenderUser.sendPasswordResetEmail(email, verificationCode);
                    NavigationUtil.navigateToWithData(
                            "/FrontOffice/user/ResetPasswordVerification.fxml",
                            rootPane,
                            email
                    );
                } catch (Exception e) {
                    NavigationUtil.showErrorAlert("Error", "Email Failed",
                            "Failed to send verification email. Please try again later.");
                    e.printStackTrace();
                }
            } else {
                NavigationUtil.showErrorAlert("Error", "Database Error",
                        "Failed to set verification code.");
            }
        } else {
            NavigationUtil.showErrorAlert("Error", "Account Not Found",
                    "No account found with this email address.");
        }
    }

    public void redirectToLogin(ActionEvent actionEvent) {
        NavigationUtil.navigateToAnchorPane("/FrontOffice/user/login.fxml", rootPane);
    }
}