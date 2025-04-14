package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class NavigationUtil {
    public static void navigateTo(String fxmlPath, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) sourceNode.getScene().getWindow();

            double width = stage.getWidth();
            double height = stage.getHeight();

            if (width <= 0 || height <= 0) {
                width = 800;
                height = 600;
            }

            Scene newScene = new Scene(root, width, height);

            stage.setMinWidth(800);
            stage.setMinHeight(600);

            stage.setScene(newScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}