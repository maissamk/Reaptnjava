package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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

            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            if (currentWidth <= MIN_WIDTH || currentHeight <= MIN_HEIGHT) {
                currentWidth = DEFAULT_WIDTH;
                currentHeight = DEFAULT_HEIGHT;
            }

            Scene newScene = new Scene(root, currentWidth, currentHeight);
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);
            stage.setScene(newScene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}