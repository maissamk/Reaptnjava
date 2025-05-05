package controllers.BackOffice;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
        productsBtn.setOnAction(e -> loadContent("/views/BackOffice/Products.fxml"));
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
    }
    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Get the stage from the event source
        //    Node source = (Node) event.getSource();
      //      Stage stage = (Stage) source.getScene().getWindow();
                Stage stage = new Stage();
         //   stage.setFullScreen(true);
            // Set the new scene
            stage.setScene(new Scene(root));
            stage.sizeToScene(); // Optional: resize to fit new content
            stage.show();
        } catch (IOException e) {
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

        // Regular buttons
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

        // Special styling for logout button
        logoutBtn.setOnMouseEntered(e -> {
            logoutBtn.setStyle("-fx-background-color: rgba(211, 47, 47, 0.2); " +
                    "-fx-border-color: rgba(211, 47, 47, 0.5); " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 20;");
            logoutBtn.setEffect(new DropShadow(10, Color.rgb(211, 47, 47, 0.7)));
        });

        logoutBtn.setOnMouseExited(e -> {
            logoutBtn.setStyle("-fx-background-color: transparent; " +
                    "-fx-border-color: rgba(255, 255, 255, 0.3); " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 20;");
            logoutBtn.setEffect(null);
        });

        // Add subtle pulse animation to logout button
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(logoutBtn.opacityProperty(), 0.9)),
                new KeyFrame(Duration.seconds(1.5), new KeyValue(logoutBtn.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(3), new KeyValue(logoutBtn.opacityProperty(), 0.9))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
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
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("You're about to logout");
        alert.setContentText("Are you sure you want to logout?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Perform logout
                SessionManager.getInstance().logout();

                // Navigate to login screen
                try {
                    // Close the current stage
                    Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
                    currentStage.close();

                    // Open login stage
                    Parent root = FXMLLoader.load(getClass().getResource("/FrontOffice/user/Login.fxml"));
                    Stage loginStage = new Stage();
                    loginStage.setScene(new Scene(root));
                    loginStage.setTitle("Login");
                    loginStage.show();

                    // Optional: Set fullscreen if needed
                    // loginStage.setFullScreen(true);

                } catch (IOException e) {
                    e.printStackTrace();
                    NavigationUtil.showErrorAlert("Navigation Error",
                            "Failed to logout",
                            "An error occurred while trying to logout: " + e.getMessage());
                }
            }
        });
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
}