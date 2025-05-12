package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
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
            if (sourceNode == null) {
                throw new IllegalArgumentException("Source node cannot be null");
            }

            // Get the root BorderPane from the current scene
            BorderPane rootPane = (BorderPane) sourceNode.getScene().getRoot();

            // Check if we're trying to load into a BorderPane structure
            if (rootPane != null && rootPane.getTop() != null) {
                // Load only the content and set it in the center
                FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
                Parent content = loader.load();
                rootPane.setCenter(content);
            } else {
                // Fallback to full scene replacement
                FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
                Parent root = loader.load();
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
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Cannot load page", e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            e.printStackTrace();
            // Fallback navigation
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
    public static void navigateToAnchorPane(String fxmlPath, AnchorPane anchorPane) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Clear existing children and add new content
            anchorPane.getChildren().setAll(root);

            // Set anchor constraints to 0 to fill the AnchorPane
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load view",
                    "Could not load the requested view: " + fxmlPath);
        }
    }

    public static void navigateToAnchorPane(String fxmlPath, AnchorPane anchorPane, Object data) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Pass data to controller if it has initData method
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    Method initData = controller.getClass().getMethod("initData", Object.class);
                    initData.invoke(controller, data);
                } catch (NoSuchMethodException e) {
                    // Controller doesn't have initData method - that's fine
                }
            }

            // Clear existing children and add new content
            anchorPane.getChildren().setAll(root);

            // Set anchor constraints to 0 to fill the AnchorPane
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load view",
                    "Could not load the requested view: " + fxmlPath);
        }
    }
}
