package controllers.FrontOffice.User;

import Models.user;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import services.FacePlusPlusService;
import services.GoogleAuthGIS;
import services.GoogleAuthService;
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
    @FXML
    private ProgressIndicator googleLoadingIndicator;
    @FXML
    private Label statusLabel;

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
        googleLoadingIndicator.setVisible(true);
        statusLabel.setText("Connexion à Google en cours...");
        Button googleBtn = (Button) event.getSource();
        googleBtn.setDisable(true);
        statusLabel.setText("Connecting to Google...");

        GoogleAuthService googleAuth = new GoogleAuthService();
        Task<user> authTask = googleAuth.authenticate();

        authTask.setOnSucceeded(e -> {
            try {
                user googleUser = authTask.getValue();
                SessionManager.getInstance().startSession(googleUser);
                googleLoadingIndicator.setVisible(false);
                statusLabel.setText("Login successful!");
                redirectToHome();
            } catch (Exception ex) {
                statusLabel.setText("Login failed");
                NavigationUtil.showErrorAlert("Session Error", "Failed to start session",
                        "Please try again or contact support.");
            } finally {
                googleBtn.setDisable(false);
            }
        });

        authTask.setOnFailed(e -> {
            Throwable ex = authTask.getException();
            String errorMsg = "Authentication failed. Please try again.";
            String detailedMsg = ex.getMessage();

            // Handle specific error cases
            if (ex.getCause() instanceof java.net.BindException) {
                errorMsg = "Port 8888 is already in use. Please close other applications using this port.";
            }
            else if (detailedMsg.contains("redirect_uri_mismatch")) {
                errorMsg = "Configuration error. Please contact support.";
            }
            else if (detailedMsg.contains("timed out")) {
                errorMsg = "Connection timed out. Please check your internet and try again.";
            }

            statusLabel.setText("Google login failed");
            NavigationUtil.showErrorAlert("Google Login Failed", errorMsg,
                    detailedMsg.contains("timed out") ? "" : detailedMsg);
            googleLoadingIndicator.setVisible(false);
            googleBtn.setDisable(false);
        });

        new Thread(authTask).start();
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

// ✨ If no user matches the face:
                    javafx.application.Platform.runLater(() ->
                            NavigationUtil.showErrorAlert("Login Failed", "Face Not Registered", "Your face was not recognized. Please register first.")
                    );


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
        user currentUser = SessionManager.getInstance().getCurrentUser();
        String homePagePath;

        // Check if user has ROLE_ADMIN
        if (currentUser.getRoles() != null && currentUser.getRoles().contains("ROLE_ADMIN")) {
            homePagePath = "/BackOffice/HomeBack.fxml";
        } else {
            homePagePath = "/FrontOffice/Home.fxml";
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(homePagePath));
        Parent root = loader.load();

        Stage stage = (Stage) loginButton.getScene().getWindow();

        double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();

        Scene scene = new Scene(root, screenWidth * 0.9, screenHeight * 0.9);
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.centerOnScreen();
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

    @FXML
    void handleForgotPassword(ActionEvent event) {
        try {
            // Load the forgot password screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/user/forgot-password.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene with proper dimensions
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);

            // Maintain window position and state
            stage.centerOnScreen();
            if (stage.isMaximized()) {
                stage.setMaximized(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            NavigationUtil.showErrorAlert("Error", "Navigation Failed",
                    "Could not load forgot password screen: " + e.getMessage());
        }
    }
}