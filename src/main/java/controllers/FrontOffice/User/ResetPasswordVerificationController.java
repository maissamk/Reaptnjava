package controllers.FrontOffice.User;

import Models.user;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import services.UserServices;
import utils.NavigationUtil;
import utils.PasswordUtils;

import java.time.LocalDateTime;

public class ResetPasswordVerificationController {
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private AnchorPane rootPane;
    @FXML private ImageView backgroundImage;

    private String email;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = stage.getScene();

            // Bind background image to window size
            if (backgroundImage != null) {
                backgroundImage.fitWidthProperty().bind(scene.widthProperty());
                backgroundImage.fitHeightProperty().bind(scene.heightProperty());
            }

            // Make window full screen or maximized
            stage.setMaximized(true);

            // Focus on code field when window loads
            codeField.requestFocus();
        });
    }

    public void initData(String email) {
        this.email = email;
    }

    @FXML
    void handleResetPassword() {
        String code = codeField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (code.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            NavigationUtil.showErrorAlert("Error", "Missing Fields",
                    "Please fill in all fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            NavigationUtil.showErrorAlert("Error", "Password Mismatch",
                    "Passwords do not match.");
            return;
        }

        if (newPassword.length() < 8) {
            NavigationUtil.showErrorAlert("Error", "Weak Password",
                    "Password must be at least 8 characters.");
            return;
        }

        // Verify code and reset password
        UserServices userService = new UserServices();
        user user = userService.getUserByEmail(email);

        if (user != null &&
                user.getResetToken() != null &&
                user.getResetToken().equals(code) &&
                user.getTokenExpiry() != null &&
                user.getTokenExpiry().isAfter(LocalDateTime.now())) {

            // Update password
            String hashedPassword = PasswordUtils.hashForSymfony(newPassword);
            user.setPassword(hashedPassword);
            user.setResetToken(null);
            user.setTokenExpiry(null);

            try {
                userService.update(user);  // still returns void
                NavigationUtil.showAlert("Success", "Password Reset",
                        "Your password has been updated successfully.");
                NavigationUtil.navigateToAnchorPane("/FrontOffice/user/Login.fxml", rootPane);
            } catch (Exception e) {
                e.printStackTrace(); // optional: log error
                NavigationUtil.showErrorAlert("Error", "Update Failed",
                        "Failed to update password. Please try again.");
            }
        } else {
            NavigationUtil.showErrorAlert("Error", "Invalid Code",
                    "The verification code is invalid or expired.");
        }
    }

    public void redirectToLogin(ActionEvent actionEvent) {
        NavigationUtil.navigateToAnchorPane("/FrontOffice/user/login.fxml", rootPane);
    }
}