package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.AutoStockMonitor;

public class FruitablesApp extends Application {
    // Automatic stock monitoring service
    private static AutoStockMonitor stockMonitor;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load directly to the Dashboard FXML instead of Login
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Dashboard.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        
        primaryStage.setTitle("Fruitables Management System");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        
        // Start automatic stock monitoring
        // For testing purposes, set the check interval to 5 minutes instead of 1 hour
        stockMonitor = new AutoStockMonitor(5 * 60 * 1000, "manager@fruitables.com");
        stockMonitor.startMonitoring();
    }
    
    @Override
    public void stop() {
        // Stop the stock monitor when the application is closing
        if (stockMonitor != null) {
            stockMonitor.stopMonitoring();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 