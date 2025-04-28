package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.io.IOException;
import java.lang.reflect.Method;

public class NavigationUtil {
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 800;
    private static final double MIN_WIDTH = 800;
    private static final double MIN_HEIGHT = 600;

    public static void navigateTo(String fxmlPath, Node sourceNode) {
        try {
            // Debug check
            if (sourceNode == null) {
                throw new IllegalArgumentException("Source node cannot be null");
            }
            if (sourceNode.getScene() == null) {
                throw new IllegalStateException("Source node is not in a scene");
            }

            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Get the stage from the source node's scene
            Stage stage = (Stage) sourceNode.getScene().getWindow();

            // Your existing window sizing logic
            double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

            double targetWidth = Math.max(stage.getWidth(), DEFAULT_WIDTH);
            double targetHeight = Math.max(stage.getHeight(), DEFAULT_HEIGHT);

            if (targetWidth < MIN_WIDTH) targetWidth = DEFAULT_WIDTH;
            if (targetHeight < MIN_HEIGHT) targetHeight = DEFAULT_HEIGHT;
            if (targetWidth > screenWidth) targetWidth = screenWidth;
            if (targetHeight > screenHeight) targetHeight = screenHeight;

            Scene newScene = new Scene(root, targetWidth, targetHeight);
            stage.setScene(newScene);
            stage.centerOnScreen();

            if (stage.isFullScreen()) {
                stage.setFullScreen(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Cannot load page", e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            e.printStackTrace();
            // Fallback navigation if the source node isn't properly connected
            try {
                FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.show();
            } catch (IOException ex) {
                showErrorAlert("Critical Error", "Navigation Failed",
                        "Could not open new window: " + ex.getMessage());
            }
        }
    }
    public static void navigateToWithData(String fxmlPath, Node node, Object data) {
        try {
            if (node == null) {
                throw new IllegalArgumentException("Root node cannot be null for navigation");
            }

            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Pass data to controller if it has initData method
            try {
                Method initData = loader.getController().getClass().getMethod("initData", data.getClass());
                initData.invoke(loader.getController(), data);
            } catch (NoSuchMethodException e) {
                // Controller doesn't need the data
            } catch (Exception e) {
                e.printStackTrace();
            }

            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
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
