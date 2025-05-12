package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.AccountStatusChecker;
import utils.SessionManager;
import Models.user;

public class MainFX extends Application {

    private static final double DEFAULT_WIDTH = 1250.0;
    private static final double DEFAULT_HEIGHT = 800.0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        AccountStatusChecker.startChecking();

        // Get the SessionManager instance
        SessionManager sessionManager = SessionManager.getInstance();

        String fxmlPath;

        if (sessionManager.isLoggedIn()) {
            // User is logged in - check their role
            user currentUser = sessionManager.getCurrentUser();
            if (currentUser.getRoles().contains("ROLE_ADMIN")) {
                fxmlPath = "/BackOffice/HomeBack.fxml";
            } else {
                fxmlPath = "/FrontOffice/Home.fxml";
            }
        } else {
            // No user logged in - go to login page
            fxmlPath = "/FrontOffice/user/login.fxml";
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        primaryStage.setTitle("Agricultural Management System");
        primaryStage.setScene(scene);

        configureStageSizeManagement(primaryStage);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private void configureStageSizeManagement(Stage stage) {
        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);
        stage.setMinWidth(1500);
        stage.setMinHeight(700);

        stage.fullScreenProperty().addListener((obs, wasFullScreen, isNowFullScreen) -> {
            if (!isNowFullScreen) {
                stage.setWidth(DEFAULT_WIDTH);
                stage.setHeight(DEFAULT_HEIGHT);
                stage.centerOnScreen();
            }
        });

        stage.setResizable(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}