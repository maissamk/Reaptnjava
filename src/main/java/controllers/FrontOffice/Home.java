package controllers.FrontOffice;

import Models.user;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Home implements Initializable {

    @FXML public Button material;
    @FXML private HBox topNavigationBar;
    @FXML private ImageView logoImageView;
    @FXML private Button accueilButton;
    @FXML private Button produitsButton;
    @FXML private Button produitsDetailButton;
    @FXML private Button parcelleButton;
    @FXML private Button offersButton;
    @FXML private Button masterfulButton;
    @FXML private Button loginButton;
    @FXML private Button profileButton;
    @FXML private ImageView userAvatar;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private StackPane mainContentPane; // Make sure this matches your FXML
    @FXML private Label welcomeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkPersistentSession();
        updateUI();
        setupEventHandlers();
    }

    private void checkPersistentSession() {
        if (SessionManager.getInstance().isLoggedIn() &&
                SessionManager.getInstance().getCurrentUser() == null) {
            SessionManager.getInstance().loadSession();
        }
    }

    private void updateUI() {
        if (SessionManager.getInstance().isLoggedIn()) {
            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
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
                return;
            }
        }

        // Default state when not logged in
        userNameLabel.setText("Guest");
        userRoleLabel.setText("Not logged in");
        userAvatar.setImage(new Image(getClass().getResourceAsStream("/images/defaultavatar.png")));
        welcomeLabel.setText("Welcome to Agricultural Management System");
        loginButton.setText("Login");
        loginButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        profileButton.setVisible(false);
    }

    private void setupEventHandlers() {
        accueilButton.setOnAction(e -> handleAccueil());
        material.setOnAction(this::handleMaterial);
        produitsButton.setOnAction(e -> handleProduitsDetail());
        produitsDetailButton.setOnAction(e -> handleProduitsDetail());
        parcelleButton.setOnAction(e -> handleParcelle());
        offersButton.setOnAction(e -> handleOffers());
        masterfulButton.setOnAction(e -> handleMasterful());
        loginButton.setOnAction(e -> handleLogin());
        profileButton.setOnAction(e -> handleProfile());
    }

    @FXML
    private void handleProfile() {
        try {
            // Load the profile FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/user/profile.fxml"));
            Parent profileContent = loader.load();

            // Clear existing content and add the profile content
            mainContentPane.getChildren().clear();
            mainContentPane.getChildren().add(profileContent);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load profile page", Alert.AlertType.ERROR);
        }
    }

    // Other handler methods remain the same...
    private void handleAccueil() {
        System.out.println("Accueil clicked");
    }

    private void handleMaterial(ActionEvent event) {
        navigateTo("/FrontOffice/materials/client/IndexMateriel.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load page: " + fxmlPath, Alert.AlertType.ERROR);
        }
    }

    private void handleProduitsDetail() {
        // Implementation
    }

    private void handleParcelle() {
        System.out.println("Parcelle clicked");
    }

    @FXML
    private void handleOffers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/indexOffre.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load offers page", Alert.AlertType.ERROR);
        }
    }

    private void handleMasterful() {
        System.out.println("Masterful Agricole clicked");
    }

    @FXML
    private void handleLogin() {
        try {
            if (SessionManager.getInstance().isLoggedIn()) {
                SessionManager.getInstance().logout();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.show();
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/user/Login.fxml"));
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.show();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load login page", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void HomeBack(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/HomeBack.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}