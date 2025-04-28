package controllers.FrontOffice.User;

import models.user;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.GoogleAuthGIS;
import services.UserServices;
import utils.SessionManager;

import java.io.IOException;

import static utils.NavigationUtil.showErrorAlert;

public class Login {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button CreateAcoount;
    @FXML private Button loginButton;

    private final UserServices userService = new UserServices();

    @FXML
    void userLogin(ActionEvent event) {
        try {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showErrorAlert("Error", "Missing Fields", "Please fill all fields");
                return;
            }

            try {
                user authenticatedUser = userService.authenticateSymfonyUser(email, password);

                if (authenticatedUser != null) {
                    SessionManager.getInstance().startSession(authenticatedUser);
                    redirectToHome();
                }
            } catch (SecurityException e) {
                if (e.getMessage().contains("blocked")) {
                    showErrorAlert("Account Blocked", "Access Denied", e.getMessage());
                } else {
                    showErrorAlert("Error", "Login Failed", e.getMessage());
                }
            }
        } catch (Exception e) {
            showErrorAlert("Error", "System Error", e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        Button googleBtn = (Button) event.getSource();
        googleBtn.setDisable(true);

        try {
            GoogleAuthGIS googleAuth = new GoogleAuthGIS();
            Task<user> authTask = googleAuth.authenticate();

            authTask.setOnSucceeded(e -> {
                try {
                    user googleUser = authTask.getValue();
                    SessionManager.getInstance().startSession(googleUser);
                    navigateToHome();
                } catch (Exception ex) {
                    showErrorAlert("Session Error", "Failed to start session", ex.getMessage());
                } finally {
                    googleBtn.setDisable(false);
                }
            });

            authTask.setOnFailed(e -> {
                Throwable ex = authTask.getException();
                String errorMsg = "Authentication failed. Please try again.";

                if (ex instanceof java.net.BindException) {
                    errorMsg = "Port 8888 is in use. Please close conflicting applications and try again.";
                } else if (ex.getCause() != null) {
                    errorMsg = ex.getCause().getMessage();
                }

                showErrorAlert("Google Login Failed", errorMsg, "");
                googleBtn.setDisable(false);
            });

            new Thread(authTask).start();
        } catch (Exception e) {
            showErrorAlert("Error", "System Error", e.getMessage());
            googleBtn.setDisable(false);
        }
    }
    private void redirectToHome() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    private void navigateToHome() {
        try {
            redirectToHome(); // Reuse your existing method
        } catch (IOException e) {
            showErrorAlert("Error", "Navigation Error", "Failed to navigate to home");
            e.printStackTrace();
        }
    }
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    void CreateAccount(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/user/Register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void handleLogout(ActionEvent event) {
        try {
            SessionManager.getInstance().logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml")); // or "/Login.fxml"
            Parent root = loader.load();

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Logout Failed", "Unable to redirect after logout");
        }
    }

}