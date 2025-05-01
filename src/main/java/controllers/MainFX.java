package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
      //    FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/HomeBack.fxml"));



        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setFullScreen(true);
        primaryStage.setTitle("Connexion");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}