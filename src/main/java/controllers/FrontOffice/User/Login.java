package controllers.FrontOffice.User;

import Models.user;
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
import javafx.scene.image.Image;
import javafx.stage.Stage;
import services.FacePlusPlusService;
import services.GoogleAuthGIS;
import services.UserServices;
import utils.CameraUtil;
import utils.NavigationUtil;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Login {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button CreateAcoount;
    @FXML private Button loginButton;
    @FXML private Button faceLoginButton;

    private final UserServices userService = new UserServices();
    private final FacePlusPlusService faceService = new FacePlusPlusService();

    @FXML
    void userLogin(ActionEvent event) {
        try {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                NavigationUtil.showErrorAlert("Error", "Missing Fields", "Please fill all fields");
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
                    NavigationUtil.showErrorAlert("Account Blocked", "Access Denied", e.getMessage());
                } else {
                    NavigationUtil.showErrorAlert("Error", "Login Failed", e.getMessage());
                }
            }
        } catch (Exception e) {
            NavigationUtil.showErrorAlert("Error", "System Error", e.getMessage());
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
                    redirectToHome();
                } catch (Exception ex) {
                    NavigationUtil.showErrorAlert("Session Error", "Failed to start session", ex.getMessage());
                } finally {
                    googleBtn.setDisable(false);
                }
            });

            authTask.setOnFailed(e -> {
                Throwable ex = authTask.getException();
                String errorMsg = "Authentication failed. Please try again.";

                if (ex instanceof java.net.BindException) {
                    errorMsg = "Could not start local server. Please close other applications using ports 8888-8890.";
                } else if (ex.getCause() != null) {
                    errorMsg = ex.getCause().getMessage();
                }

                if (errorMsg.contains("redirect_uri_mismatch")) {
                    errorMsg = "Google login configuration error. Please contact support.";
                }

                NavigationUtil.showErrorAlert("Google Login Failed", errorMsg, "");
                googleBtn.setDisable(false);
            });

            new Thread(authTask).start();
        } catch (Exception e) {
            NavigationUtil.showErrorAlert("Error", "System Error", e.getMessage());
            googleBtn.setDisable(false);
        }
    }

    @FXML
    private void handleFaceLogin(ActionEvent event) {
        faceLoginButton.setDisable(true);

        Task<Void> faceLoginTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Image capturedImage = CameraUtil.captureImage();
                    File tempFile = File.createTempFile("face-login", ".png");
                    CameraUtil.saveImageToFile(capturedImage, tempFile.getAbsolutePath());

                    String capturedToken = faceService.detectFace(tempFile);

                    if (capturedToken == null) {
                        javafx.application.Platform.runLater(() ->
                                NavigationUtil.showErrorAlert("Error", "Face Not Detected", "Please try again with clear face visibility."));
                        return null;
                    }

                    List<user> usersWithFaces = userService.getAllUsersWithFaceTokens();
                    for (user u : usersWithFaces) {
                        if (faceService.compareFaces(u.getFace_token(), capturedToken)) {
                            SessionManager.getInstance().startSession(u);
                            javafx.application.Platform.runLater(() -> {
                                try {
                                    redirectToHome();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            return null;
                        }
                    }

                    javafx.application.Platform.runLater(() ->
                            NavigationUtil.showErrorAlert("Error", "No Match Found", "No registered user matches your face."));
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() ->
                            NavigationUtil.showErrorAlert("Error", "Login Failed", e.getMessage()));
                    e.printStackTrace();
                } finally {
                    javafx.application.Platform.runLater(() -> faceLoginButton.setDisable(false));
                }
                return null;
            }
        };

        new Thread(faceLoginTask).start();
    }

    private void redirectToHome() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) loginButton.getScene().getWindow();

        double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();

        Scene scene = new Scene(root, screenWidth * 0.9, screenHeight * 0.9); // Use 90% of screen size
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.centerOnScreen();

        // Optional: If you want it full screen, you can enable this
        // stage.setFullScreen(true);

        stage.show();
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
            double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();

            Scene scene = new Scene(root, screenWidth * 0.9, screenHeight * 0.9); // 90% of screen
            stage.setScene(scene);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.centerOnScreen();

            // Optional fullscreen:
            // stage.setFullScreen(true);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            NavigationUtil.showErrorAlert("Error", "Logout Failed", "Unable to redirect after logout");
        }
    }

}