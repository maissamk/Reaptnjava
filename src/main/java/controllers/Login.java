package controllers;

import Models.user;
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
import services.UserServices;
import utils.SessionManager;

import java.io.IOException;

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
                showAlert("Error", "Missing Fields", "Please fill all fields");
                return;
            }

            user authenticatedUser = userService.authenticateSymfonyUser(email, password);

            if (authenticatedUser != null) {
                SessionManager.getInstance().startSession(authenticatedUser);
                redirectToHome();
            } else {
                showAlert("Error", "Login Failed", "Invalid credentials");
            }
        } catch (Exception e) {
            showAlert("Error", "System Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void redirectToHome() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml")); // or "/Login.fxml"
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