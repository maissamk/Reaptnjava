package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import utils.AccountStatusChecker;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AccountStatusChecker.startChecking();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));

        Parent root = loader.load();
        
        // Obtenir les dimensions de l'écran
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        // Définir une taille initiale relative à l'écran (80% de la largeur et hauteur de l'écran)
        double width = screenBounds.getWidth() * 0.8;
        double height = screenBounds.getHeight() * 0.8;
        
        Scene scene = new Scene(root, width, height);
        
        // Activer le redimensionnement de la fenêtre
        primaryStage.setResizable(true);
        
        // Définir une taille minimale pour la fenêtre
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Centrer la fenêtre sur l'écran
        primaryStage.centerOnScreen();
        
        // Configurer la scène
        primaryStage.setTitle("Reap.tn - Agricultural Management");
        primaryStage.setScene(scene);
        
        // Configurer la fenêtre pour s'adapter à son contenu
        primaryStage.sizeToScene();
        
        // Afficher la fenêtre
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
