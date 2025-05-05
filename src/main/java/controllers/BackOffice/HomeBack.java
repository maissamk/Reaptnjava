package controllers.BackOffice;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.NavigationUtil;
import utils.SessionManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HomeBack implements Initializable {

    // Header Elements
    @FXML private ImageView adminAvatar;
    @FXML private Label adminNameLabel;
    @FXML private Label adminRoleLabel;

    // Navigation Buttons
    @FXML private Button dashboardBtn;
    @FXML private Button usersBtn;
    @FXML private Button productsBtn;
    @FXML private Button ordersBtn;
    @FXML private Button reportsBtn;
    @FXML private Button logoutBtn;
    @FXML private Button categoriebtn;
    @FXML private Button location;
    @FXML private Button returnToFrontBtn;

    // Sidebar Buttons
    @FXML private Button statisticsBtn;
    @FXML private Button statisticsBtnUser;
    @FXML private Button farmersBtn;
    @FXML private Button parcelsBtn;
    @FXML private Button harvestBtn;
    @FXML private Button settingsBtn;
    @FXML private Button logsBtn;

    // Status Bar
    @FXML private Label systemStatusLabel;
    @FXML private Label lastUpdateLabel;

    // Content Area
    @FXML private StackPane contentPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserData();
        setupEventHandlers();
        updateSystemStatus();
        startClock();
        addButtonHoverEffects();
    }

    private void loadUserData() {
        if (SessionManager.getInstance().isLoggedIn()) {
            Models.user currentUser = SessionManager.getInstance().getCurrentUser();
            adminNameLabel.setText(currentUser.getNom());
            adminRoleLabel.setText(String.join(", ", currentUser.getRoles()));

            // Load avatar image safely
            String avatarPath = "/images/" + (currentUser.getAvatar() != null ?
                    currentUser.getAvatar() : "default-avatar.png");
            loadImage(adminAvatar, avatarPath);
        }
    }

    private void loadImage(ImageView imageView, String path) {
        try {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream != null) {
                imageView.setImage(new Image(stream));
            } else {
                // Fallback to default image
                InputStream defaultStream = getClass().getResourceAsStream("/images/default-avatar.png");
                if (defaultStream != null) {
                    imageView.setImage(new Image(defaultStream));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        // Main Navigation
        dashboardBtn.setOnAction(e -> loadDashboardContent());
        usersBtn.setOnAction(e -> loadContent("/BackOffice/user/UserList.fxml"));
        
        // Créer un menu déroulant pour le bouton Products
        javafx.scene.control.ContextMenu productsMenu = new javafx.scene.control.ContextMenu();
        
        // Créer les 5 options du menu
        javafx.scene.control.MenuItem dashboardItem = new javafx.scene.control.MenuItem("Dashboard");
        dashboardItem.setOnAction(e -> loadContent("/Produits/Dashboard.fxml"));
        
        javafx.scene.control.MenuItem productsItem = new javafx.scene.control.MenuItem("Products");
        productsItem.setOnAction(e -> loadContent("/Produits/ProductManagement.fxml"));
        
        javafx.scene.control.MenuItem productTypesItem = new javafx.scene.control.MenuItem("Product Types");
        productTypesItem.setOnAction(e -> loadContent("/Produits/ProductTypeManagement.fxml"));
        
        javafx.scene.control.MenuItem stockItem = new javafx.scene.control.MenuItem("Stock Management");
        stockItem.setOnAction(e -> loadContent("/Produits/StockManagement.fxml"));
        
        javafx.scene.control.MenuItem reportsItem = new javafx.scene.control.MenuItem("Reports & Analytics");
        reportsItem.setOnAction(e -> loadContent("/Produits/Reports.fxml"));
        
        // Ajouter les options au menu
        productsMenu.getItems().addAll(dashboardItem, productsItem, productTypesItem, stockItem, reportsItem);
        
        // Configurer le bouton Products pour afficher le menu au clic
        productsBtn.setOnAction(e -> productsMenu.show(productsBtn, javafx.geometry.Side.BOTTOM, 0, 0));
        
        ordersBtn.setOnAction(e -> loadContent("/views/BackOffice/Orders.fxml"));
        reportsBtn.setOnAction(e -> loadContent("/views/BackOffice/Reports.fxml"));

        // Sidebar Navigation
        statisticsBtnUser.setOnAction(this::handleStatisticsButton);
        //statisticsBtn.setOnAction(e -> loadContent("/views/BackOffice/Statistics.fxml"));
        statisticsBtn.setOnAction(e -> loadContent("/BackOffice/Offre/statistiques.fxml"));
        farmersBtn.setOnAction(e -> loadContent("/views/BackOffice/Farmers.fxml"));
        parcelsBtn.setOnAction(e -> loadContent("/views/BackOffice/Parcels.fxml"));
        harvestBtn.setOnAction(e -> loadContent("/views/BackOffice/Harvest.fxml"));
        settingsBtn.setOnAction(e -> loadContent("/views/BackOffice/Settings.fxml"));
        logsBtn.setOnAction(e -> loadContent("/views/BackOffice/Logs.fxml"));
        location.setOnAction(this::IndexMateriels);
        categoriebtn.setOnAction(this::handleCategory);

        // Logout
        logoutBtn.setOnAction(e -> handleLogout());

        // Return to Front
        returnToFrontBtn.setOnAction(e -> handleReturnToFront());
    }
    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Obtenir les dimensions de l'écran pour une interface responsive
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            
            // Définir la taille de la fenêtre à 80% de l'écran
            double width = screenBounds.getWidth() * 0.8;
            double height = screenBounds.getHeight() * 0.8;
            
            Scene scene = new Scene(root, width, height);
            
            // Créer et configurer le stage
            Stage stage = new Stage();
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void IndexMateriels(ActionEvent event) {
        navigateTo("/BackOffice/materials/IndexMateriel.fxml", event);
    }
    private void handleCategory(ActionEvent event) {
        navigateTo("/BackOffice/category/IndexCategorie.fxml", event);
    }

    private void loadDashboardContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/HomeBack.fxml"));
            Parent dashboard = loader.load();
            contentPane.getChildren().setAll(dashboard);
        } catch (IOException e) {
            // Fallback to default content
            e.printStackTrace();
        }
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentPane.getChildren().setAll(content);
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void updateSystemStatus() {
        systemStatusLabel.setText("Operational");
        systemStatusLabel.getStyleClass().setAll("status-active");
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            lastUpdateLabel.setText("Last updated: " + time);
        }));  // Added missing parenthesis
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
    private void addButtonHoverEffects() {
        Button[] buttons = {
                dashboardBtn, usersBtn, productsBtn, ordersBtn, reportsBtn,
                statisticsBtn, farmersBtn, parcelsBtn, harvestBtn, settingsBtn, logsBtn
        };

        for (Button btn : buttons) {
            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: rgba(255,255,255,0.2);");
                btn.setEffect(new DropShadow(10, Color.GOLD));
            });

            btn.setOnMouseExited(e -> {
                btn.setStyle("");
                btn.setEffect(null);
            });
        }
    }
    @FXML
    private void handleStatisticsButton(ActionEvent event) {
        try {
            // Load the UserStats content into the main content area
            loadContent("/BackOffice/user/UserStats.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            NavigationUtil.showErrorAlert("Navigation Error", "Failed to open statistics", e.getMessage());
        }
    }

    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/user/Login.fxml"));
            Parent root = loader.load();
            
            // Obtenir les dimensions de l'écran
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            
            // Définir une taille initiale relative à l'écran
            double width = screenBounds.getWidth() * 0.8;
            double height = screenBounds.getHeight() * 0.8;
            
            Scene scene = new Scene(root, width, height);
            
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleBack(ActionEvent event) {
        // Just load the dashboard content
        try {
            loadContent("/BackOffice/HomeBack.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleReturnToFront() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent root = loader.load();
            
            // Obtenir les dimensions de l'écran
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            
            // Définir une taille initiale relative à l'écran
            double width = screenBounds.getWidth() * 0.8;
            double height = screenBounds.getHeight() * 0.8;
            
            Scene scene = new Scene(root, width, height);
            
            Stage stage = (Stage) returnToFrontBtn.getScene().getWindow();
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}