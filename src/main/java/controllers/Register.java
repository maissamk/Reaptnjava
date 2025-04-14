package controllers;

import Models.user;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.UserServices;
import utils.PasswordUtils;

import java.io.IOException;

public class Register {

    @FXML private RadioButton agriculteurRadio;
    @FXML private RadioButton clientRadio;
    @FXML private TextField emailField;
    @FXML private TextField nomField;
    @FXML private PasswordField passwordField;
    @FXML private TextField prenomField;
    @FXML private TextField telephoneField;

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


            String hashedPassword = PasswordUtils.hashForSymfony(newUser.getPassword());
            newUser.setPassword(hashedPassword);

            us.add(newUser);
            showSuccessAndRedirect();

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
        newUser.setRoles(agriculteurRadio.isSelected() ? "Agriculteur" : "Client");
        newUser.setStatus("active");
        return newUser;
    }

    private void showSuccessAndRedirect() {
        showAlert("Inscription réussie", "Succès",
                "Utilisateur enregistré avec succès!");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            emailField.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erreur", "Navigation impossible",
                    "Impossible de charger la page de login");
        }
    }
}