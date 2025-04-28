package controllers.FrontOffice.User;

import Models.user;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import services.UserServices;
import utils.EmailSender;
import utils.NavigationUtil;

import java.time.LocalDateTime;

public class ForgotPasswordController {
    @FXML private TextField emailField;
    @FXML private AnchorPane rootPane; // This will now be properly injected

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
            // Generate 6-digit verification code
            String verificationCode = String.format("%06d", (int)(Math.random() * 1000000));
            LocalDateTime expiryTime = LocalDateTime.now().plusHours(24);

            // Update user with verification code
            if (userService.setPasswordResetToken(email, verificationCode, expiryTime)) {
                try {
                    // Send email with verification code
                    EmailSender.sendPasswordResetEmail(email, verificationCode);

                    // Navigate to verification screen with email context
                    NavigationUtil.navigateToWithData(
                            "/FrontOffice/user/ResetPasswordVerification.fxml",
                            rootPane, // Now using the properly injected rootPane
                            email
                    );
                } catch (Exception e) {
                    NavigationUtil.showErrorAlert("Error", "Email Failed",
                            "Failed to send verification email. Please try again later.");
                    e.printStackTrace(); // Log the error for debugging
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
        NavigationUtil.navigateTo("/FrontOffice/user/login.fxml", rootPane);
    }
}