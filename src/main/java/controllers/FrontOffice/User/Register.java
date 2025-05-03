package controllers.FrontOffice.User;

import Models.user;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.UserServices;
import utils.EmailSenderUser;
import utils.NavigationUtil;
import utils.SessionManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class Register {

    @FXML
    private RadioButton agriculteurRadio;
    @FXML
    private RadioButton clientRadio;
    @FXML
    private TextField emailField;
    @FXML
    private TextField nomField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField telephoneField;
    @FXML
    private Node rootPane;

    private final UserServices us = new UserServices();
    private ToggleGroup roleToggleGroup;
    private static final String DEFAULT_AVATAR = "defaultavatar.png"; // Default avatar filename

    @FXML
    public void initialize() {
        roleToggleGroup = new ToggleGroup();
        agriculteurRadio.setToggleGroup(roleToggleGroup);
        clientRadio.setToggleGroup(roleToggleGroup);
        clientRadio.setSelected(true);
    }

    @FXML
    void save(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        // Validate password strength
        if (passwordField.getText().length() < 8) {
            showAlert("Erreur", "Mot de passe faible",
                    "Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        try {
            user newUser = createUserFromInputs();
            newUser.setAvatar(DEFAULT_AVATAR);

            String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            newUser.setVerificationCode(verificationCode);
            newUser.setVerificationSentAt(LocalDateTime.now());

            SessionManager.setPendingUser(newUser);

            try {
                EmailSenderUser.sendVerificationEmail(newUser.getEmail(), verificationCode);

                // Get current stage before changing scenes
                Stage currentStage = (Stage) emailField.getScene().getWindow();

                // Load verification screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/user/Verification.fxml"));
                Parent root = loader.load();

                // Create new scene with proper dimensions
                Scene newScene = new Scene(root, currentStage.getWidth(), currentStage.getHeight());
                currentStage.setScene(newScene);

                // Maintain window position and state
                currentStage.centerOnScreen();
                if (currentStage.isMaximized()) {
                    currentStage.setMaximized(true);
                }

            } catch (Exception emailException) {
                SessionManager.clearPendingUser();
                showAlert("Erreur", "Erreur d'envoi d'email", emailException.getMessage());
            }

        } catch (Exception e) {
            handleException(e);
        }
    }


    private boolean validateInputs() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String telephone = telephoneField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || telephone.isEmpty() ||
                email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Champs manquants",
                    "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        if (!nom.matches("[a-zA-Z]+")) {
            showAlert("Erreur", "Nom invalide",
                    "Le nom ne doit contenir que des lettres.");
            return false;
        }

        if (!prenom.matches("[a-zA-Z]+")) {
            showAlert("Erreur", "Prénom invalide",
                    "Le prénom ne doit contenir que des lettres.");
            return false;
        }

        if (!telephone.matches("\\d{8,15}")) {
            showAlert("Erreur", "Téléphone invalide",
                    "Le numéro de téléphone doit contenir entre 8 et 15 chiffres.");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showAlert("Erreur", "Email invalide",
                    "Veuillez entrer une adresse email valide.");
            return false;
        }

        return true;
    }

    private user createUserFromInputs() {
        user newUser = new user();
        newUser.setNom(nomField.getText());
        newUser.setPrenom(prenomField.getText());
        newUser.setTelephone(telephoneField.getText());
        newUser.setEmail(emailField.getText());
        newUser.setPassword(passwordField.getText());
        newUser.setRoles(agriculteurRadio.isSelected() ? "ROLE_AGRICULTEUR" : "ROLE_CLIENT");
        newUser.setStatus("active");
        return newUser;
    }

    private void showSuccessAndRedirect() {
        showAlert("Inscription réussie", "Succès",
                "Utilisateur enregistré avec succès!");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/user/Login.fxml"));
            Parent root = loader.load();
            nomField.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erreur", "Navigation impossible",
                    "Impossible de charger la page de login");
        }
    }

    private void handleException(Exception e) {
        System.err.println("Registration error: " + e.getMessage());
        showAlert("Erreur", "Échec de l'inscription",
                "Une erreur est survenue: " + e.getMessage());
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void redirectToLogin(ActionEvent event) {
        NavigationUtil.navigateTo("/FrontOffice/user/login.fxml", rootPane);
    }
}