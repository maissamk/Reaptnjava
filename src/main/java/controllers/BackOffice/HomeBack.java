package controllers.BackOffice;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

    // Sidebar Buttons
    @FXML private Button statisticsBtn;
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
        usersBtn.setOnAction(e -> loadContent("/views/BackOffice/Users.fxml"));
        productsBtn.setOnAction(e -> loadContent("/views/BackOffice/Products.fxml"));
        ordersBtn.setOnAction(e -> loadContent("/views/BackOffice/Orders.fxml"));
        reportsBtn.setOnAction(e -> loadContent("/views/BackOffice/Reports.fxml"));

        // Sidebar Navigation
        statisticsBtn.setOnAction(e -> loadContent("/views/BackOffice/Statistics.fxml"));
        farmersBtn.setOnAction(e -> loadContent("/views/BackOffice/Farmers.fxml"));
        parcelsBtn.setOnAction(e -> loadContent("/views/BackOffice/Parcels.fxml"));
        harvestBtn.setOnAction(e -> loadContent("/views/BackOffice/Harvest.fxml"));
        settingsBtn.setOnAction(e -> loadContent("/views/BackOffice/Settings.fxml"));
        logsBtn.setOnAction(e -> loadContent("/views/BackOffice/Logs.fxml"));

        // Logout
        logoutBtn.setOnAction(e -> handleLogout());
    }

    private void loadDashboardContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BackOffice/Dashboard.fxml"));
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

    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Auth/Login.fxml"));
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}