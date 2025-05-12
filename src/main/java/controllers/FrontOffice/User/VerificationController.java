package controllers.FrontOffice.User;

import Models.user;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.UserServices;
import utils.EmailSenderUser;
import utils.NavigationUtil;
import utils.PasswordUtils;
import utils.SessionManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class VerificationController {
    @FXML private TextField codeField;
    @FXML private Label emailLabel;
    @FXML private Label timerLabel;
    @FXML private Node rootPane;

    private Timeline countdown;
    private user pendingUser;

    @FXML
    public void initialize() {
        pendingUser = SessionManager.getPendingUser();
        if (pendingUser == null) {
            redirectTo("/FrontOffice/user/Register.fxml");
            return;
        }

        emailLabel.setText("Code envoyé à: " + pendingUser.getEmail());
        startCountdown();
    }

    private void startCountdown() {
        countdown = new Timeline(
                new KeyFrame(javafx.util.Duration.seconds(1), e -> updateTimerDisplay())
        );
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    private void updateTimerDisplay() {
        Duration duration = Duration.between(
                LocalDateTime.now(),
                pendingUser.getVerificationSentAt().plusMinutes(15)
        );
        long secondsLeft = duration.getSeconds();

        if (secondsLeft <= 0) {
            countdown.stop();
            timerLabel.setText("Code expiré!");
            SessionManager.clearPendingUser();
            redirectTo("/FrontOffice/user/Register.fxml");
        } else {
            timerLabel.setText(String.format("Expire dans %02d:%02d",
                    secondsLeft / 60, secondsLeft % 60));
        }
    }

    @FXML
    void handleVerify() {
        try {
            String enteredCode = codeField.getText().trim();

            if (enteredCode.equalsIgnoreCase(pendingUser.getVerificationCode())) {
                // Mark as verified
                pendingUser.setVerified(true);

                // Hash password and save to DB
                UserServices userService = new UserServices();
                String hashedPassword = PasswordUtils.hashForSymfony(pendingUser.getPassword());
                pendingUser.setPassword(hashedPassword);
                userService.add(pendingUser);

                // Start session and clear pending user
                SessionManager.getInstance().startSession(pendingUser);
                SessionManager.clearPendingUser();

                // Navigate after successful verification
                redirectToHome();
            } else {
                NavigationUtil.showErrorAlert("Erreur", "Code invalide", "Le code de vérification est incorrect.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            NavigationUtil.showErrorAlert("Erreur", "Échec de vérification", "Impossible de terminer la vérification : " + e.getMessage());
        }
    }

    @FXML
    void handleResendCode() {
        // Generate new code
        String newCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        pendingUser.setVerificationCode(newCode);
        pendingUser.setVerificationSentAt(LocalDateTime.now());

        try {
            EmailSenderUser.sendVerificationEmail(pendingUser.getEmail(), newCode);
            NavigationUtil.showAlert("Succès", "Nouveau code envoyé", "Vérifiez votre email à nouveau.");
            startCountdown();
        } catch (Exception e) {
            NavigationUtil.showErrorAlert("Erreur", "Échec d'envoi", "Impossible d'envoyer le nouveau code.");
        }
    }

    public void redirectToLogin(ActionEvent actionEvent) {
        redirectTo("/FrontOffice/user/login.fxml");
    }

    private void redirectTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
            NavigationUtil.showErrorAlert("Error", "Navigation Failed", "Could not load the requested view.");
        }
    }

    private void redirectToHome() throws IOException {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        String homePagePath = currentUser.getRoles().contains("ROLE_ADMIN")
                ? "/BackOffice/HomeBack.fxml"
                : "/FrontOffice/Home.fxml";

        FXMLLoader loader = new FXMLLoader(getClass().getResource(homePagePath));
        Parent root = loader.load();

        Stage stage = (Stage) rootPane.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
    }
}