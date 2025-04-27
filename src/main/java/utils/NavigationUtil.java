package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.io.IOException;

public class NavigationUtil {
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 800;
    private static final double MIN_WIDTH = 800;
    private static final double MIN_HEIGHT = 600;

    public static void navigateTo(String fxmlPath, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) sourceNode.getScene().getWindow();

            double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

            double targetWidth = Math.max(stage.getWidth(), DEFAULT_WIDTH);
            double targetHeight = Math.max(stage.getHeight(), DEFAULT_HEIGHT);

            // If the current size is too small, use default
            if (targetWidth < MIN_WIDTH) targetWidth = DEFAULT_WIDTH;
            if (targetHeight < MIN_HEIGHT) targetHeight = DEFAULT_HEIGHT;

            // But don't go beyond screen size
            if (targetWidth > screenWidth) targetWidth = screenWidth;
            if (targetHeight > screenHeight) targetHeight = screenHeight;

            Scene newScene = new Scene(root, targetWidth, targetHeight);

            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);

            stage.setScene(newScene);
            stage.centerOnScreen();

            // Optional: Maximize if needed (if stage was already maximized)
            if (stage.isFullScreen()) {
                stage.setFullScreen(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Cannot load page", e.getMessage());
        }
    }

    public static void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
