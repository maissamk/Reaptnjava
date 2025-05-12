package controllers.FrontOffice;

import Models.user;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Home implements Initializable {

    @FXML public Button material;
    // Navigation elements
    @FXML private HBox topNavigationBar;
    @FXML private ImageView logoImageView;
    @FXML private Button accueilButton;
    @FXML private Button produitsButton;
    @FXML private Button parcelleButton;
    @FXML private Button offersButton;
    @FXML private Button plantButton;
    @FXML private Button loginButton;
    @FXML private Button profileButton;
    @FXML private Button commandeButton;

    // User info elements
    @FXML private ImageView userAvatar;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private StackPane mainContentPane; // Make sure this matches your FXML
    @FXML private Label welcomeLabel;


    private double normalWidth = 1250.0; // Default width matching your FXML
    private double normalHeight = 800.0; // Default height (adjust as needed)

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check for existing session when initializing
        checkPersistentSession();
        updateUI();
        setupEventHandlers();
    }

    public StackPane getMainContentPane() {
        return mainContentPane;
    }

    private void checkPersistentSession() {
        // This will automatically load any existing session from file
        if (SessionManager.getInstance().isLoggedIn() &&
                SessionManager.getInstance().getCurrentUser() == null) {
            // If session exists but user object isn't loaded, refresh from database
            SessionManager.getInstance().loadSession();
        }
    }

    private void updateUI() {
        if (SessionManager.getInstance().isLoggedIn()) {
            user currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Update user profile section
                userNameLabel.setText(currentUser.getNom());

                // Convert role to display-friendly format
                String roleDisplay = convertRoleToDisplayFormat(currentUser.getRoles());
                userRoleLabel.setText(roleDisplay);

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

    // New helper method to convert role strings
    private String convertRoleToDisplayFormat(String roles) {
        if (roles == null || roles.isEmpty()) {
            return "User";
        }

        // Split multiple roles if needed
        String[] roleArray = roles.split(",");

        // Convert each role to display format
        StringBuilder displayRoles = new StringBuilder();
        for (String role : roleArray) {
            String trimmedRole = role.trim();
            switch (trimmedRole) {
                case "ROLE_ADMIN":
                    displayRoles.append("Admin");
                    break;
                case "ROLE_CLIENT":
                    displayRoles.append("Client");
                    break;
                case "ROLE_AGRICULTEUR":
                    displayRoles.append("Agriculteur");
                    break;
                default:
                    displayRoles.append(trimmedRole.replace("ROLE_", ""));
            }

            // Add comma if multiple roles
            if (!trimmedRole.equals(roleArray[roleArray.length-1].trim())) {
                displayRoles.append(", ");
            }
        }

        return displayRoles.toString();
    }
    private void setupEventHandlers() {
        // Navigation buttons
        accueilButton.setOnAction(e -> handleAccueil());
        commandeButton.setOnAction(this::handleCommande);
        material.setOnAction(this::handleMaterial);
        produitsButton.setOnAction(e -> handleProduitsDetail());
        parcelleButton.setOnAction(e -> handleParcelle());
        offersButton.setOnAction(e -> handleOffers());
        plantButton.setOnAction(e -> handlePlant());

        // Auth buttons
        loginButton.setOnAction(e -> handleLogin());
        profileButton.setOnAction(e -> handleProfile());
    }

    private void handlePlant() {
        try{
        // Load just the parcelle content
        FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/GestionCommande/analyze_image.fxml"));
        Parent content = contentLoader.load();

        // Clear existing content and add the parcelle content
        mainContentPane.getChildren().clear();
        mainContentPane.getChildren().add(content);

    } catch (IOException e) {
        e.printStackTrace();
        showAlert("Error", "Failed to load parcels page: " + e.getMessage(), Alert.AlertType.ERROR);
    }
    }


    private void handleParcelle() {
        try {
            // Load just the parcelle content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/Afficherparcelles.fxml"));
            Parent content = contentLoader.load();

            // Clear existing content and add the parcelle content
            mainContentPane.getChildren().clear();
            mainContentPane.getChildren().add(content);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load parcels page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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

    private void handleProduitsDetail() {
        try {
            // Charger FrontOffice.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produits/FrontOffice.fxml"));
            Parent productContent = loader.load();
            
            // Effacer le contenu existant et ajouter le nouveau contenu
            mainContentPane.getChildren().clear();
            mainContentPane.getChildren().add(productContent);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load products page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) accueilButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load home page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            
            // Obtenir les dimensions de l'écran pour une interface responsive
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            
            // Définir la taille de la fenêtre à 80% de l'écran
            double width = screenBounds.getWidth() * 0.8;
            double height = screenBounds.getHeight() * 0.8;
            
            Scene scene = new Scene(root, width, height);
            
            // Activer le redimensionnement
            stage.setResizable(true);
            
            // Définir une taille minimale
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            // Configurer la scène
            stage.setScene(scene);
            
            // Centrer sur l'écran
            stage.centerOnScreen();
            
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load page: " + fxmlPath, Alert.AlertType.ERROR);
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void handleProduits(ActionEvent event) {
        // Implementation
    }

    @FXML
    private void handleCommande(ActionEvent event) {
        try {
            // Load just the commande content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/GestionCommande/PanierView.fxml"));
            Parent content = contentLoader.load();

            // Clear existing content and add the commande content
            mainContentPane.getChildren().clear();
            mainContentPane.getChildren().add(content);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load orders page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleMaterial(ActionEvent event) {
        try {
            // Load just the material content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/materials/client/IndexMateriel.fxml"));
            Parent content = contentLoader.load();

            // Clear existing content and add the material content
            mainContentPane.getChildren().clear();
            mainContentPane.getChildren().add(content);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load materials page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    @FXML
    private void handleOffers() {
        System.out.println("Offers disponibles clicked");
        try {
            // Load just the offers content (without another navbar)
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/indexOffre.fxml"));
            Parent content = contentLoader.load();

            // Clear existing content and add the offers content
            mainContentPane.getChildren().clear();
            mainContentPane.getChildren().add(content);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load offers page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleMasterful() {
        System.out.println("Masterful Agricole clicked");
    }

    @FXML
    private void handleLogin() {
        try {
            if (SessionManager.getInstance().isLoggedIn()) {
                // Logout logic
                SessionManager.getInstance().logout();

                // Refresh the home page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.show();
            } else {
                // Login logic - redirect to login page
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


    private void initializeStageSize(Stage stage) {
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isFullScreen()) {
                normalWidth = newVal.doubleValue();
            }
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isFullScreen()) {
                normalHeight = newVal.doubleValue();
            }
        });

        stage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                // When exiting full-screen, restore the previous size
                stage.setWidth(normalWidth);
                stage.setHeight(normalHeight);
            }
        });
    }
}