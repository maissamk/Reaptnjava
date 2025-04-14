package controllers;

import Models.user;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import utils.SessionManager;
import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;

public class Home implements Initializable {

    @FXML private ImageView userAvatar;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button profileButton;

    @FXML private Button loginButton;
    @FXML private Label welcomeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateUI();
        setupEventHandlers();
    }

    private void updateUI() {
        if (SessionManager.getInstance().isLoggedIn()) {
            user currentUser = SessionManager.getInstance().getCurrentUser();

            // Update user profile section
            userNameLabel.setText(currentUser.getNom());
            userRoleLabel.setText(String.join(", ", currentUser.getRoles()));

            try {
                Image avatar = new Image(getClass().getResourceAsStream(
                        "/images" + currentUser.getAvatar()));
                userAvatar.setImage(avatar);
            } catch (Exception e) {
                userAvatar.setImage(new Image(getClass().getResourceAsStream("/images/defaultavatar.png")));
            }

            welcomeLabel.setText("Welcome back, " + currentUser.getNom());
            loginButton.setText("Logout");
            loginButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
            profileButton.setVisible(true);
        } else {
            userNameLabel.setText("Guest");
            userRoleLabel.setText("Not logged in");
            userAvatar.setImage(new Image(getClass().getResourceAsStream("/images/defaultavatar.png")));

            welcomeLabel.setText("Welcome to Agricultural Management System");
            loginButton.setText("Login");
            loginButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
            profileButton.setVisible(false);
        }
    }
    private void setupEventHandlers() {
        loginButton.setOnAction(e -> handleLogin());
        profileButton.setOnAction(e -> handleProfile());
    }

    @FXML

    private void handleProfile() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/profile.fxml"));
            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleAccueil() {
        System.out.println("Accueil clicked");
    }

    private void handleProduits() {
        System.out.println("Produits clicked");
    }

    private void handleProduitsDetail() {
        System.out.println("Produits Detail clicked");
    }

    private void handleParcelle() {
        System.out.println("Parcelle clicked");
    }

    private void handleOffers() {
        System.out.println("Offers disponibles clicked");
    }

    private void handleMasterful() {
        System.out.println("Masterful Agricole clicked");
    }

    @FXML
    private void handleLogin() {
        try {
            if (SessionManager.getInstance().isLoggedIn()) {

                SessionManager.getInstance().logout();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}