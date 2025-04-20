package controllers.FrontOffice;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;


import java.net.URL;
import java.util.ResourceBundle;

public class Home implements Initializable {

    // Navigation elements
    @FXML private HBox topNavigationBar;
    @FXML private ImageView logoImageView;
    @FXML private Button accueilButton;
    @FXML private Button produitsButton;
    @FXML private Button produitsDetailButton;
    @FXML private Button parcelleButton;
    @FXML private Button offersButton;
    @FXML private Button masterfulButton;
    @FXML private Button loginButton;


    // Main content
    @FXML private StackPane mainContentPane;
    @FXML private Label welcomeLabel;

    // Footer elements
    @FXML private HBox footerBar;
    @FXML private Label privacyPolicyLabel;
    @FXML private Label termsOfUseLabel;
    @FXML private Label salesRefundsLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Verify all FXML injections
        if (welcomeLabel == null) {
            throw new IllegalStateException("welcomeLabel was not injected properly from FXML");
        }

        updateUI();
        setupEventHandlers();
    }

    private void updateUI() {
        if (true) {
            welcomeLabel.setText("Welcome back, ");
            loginButton.setText("Logout");
        } else {
            welcomeLabel.setText("Welcome to Agricultural Management System");
            loginButton.setText("Login");
        }
    }

    private void setupEventHandlers() {
        // Navigation buttons
        accueilButton.setOnAction(e -> handleAccueil());
        produitsButton.setOnAction(e -> handleProduits());
        produitsDetailButton.setOnAction(e -> handleProduitsDetail());
        parcelleButton.setOnAction(e -> handleParcelle());
        offersButton.setOnAction(e -> handleOffers());
        masterfulButton.setOnAction(e -> handleMasterful());

    }

    // Navigation handlers
    private void handleAccueil() {
        System.out.println("Accueil clicked");
    }

    private void handleProduits() {
        System.out.println("Produits clicked");
    }

    private void handleProduitsDetail() {
        System.out.println("Produits Detail clicked");
    }

    private void handleParcelle() {
        System.out.println("Parcelle clicked");
    }

    @FXML
    private void handleOffers() {
        System.out.println("Offers disponibles clicked");
        try {
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/Frontoffice/BaseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/Frontoffice/BaseFront.fxml"));
            Parent content = contentLoader.load(); // content with its own controller & methods

            // Inject the page content into base layout
            baseController.getContentPane().getChildren().setAll(content);

            // Now show the complete scene
            Scene scene = new Scene(baseRoot);
            Stage stage = (Stage) offersButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMasterful() {
        System.out.println("Masterful Agricole clicked");
    }



}
