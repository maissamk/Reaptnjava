package controllers.FrontOffice.Offer;

import controllers.FrontOffice.BaseFrontController;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import Models.Offre;
import javafx.scene.layout.StackPane;
import services.OffreService;
import java.sql.SQLException;
import java.util.List;

import javafx.scene.layout.VBox;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.LanguageManager;

import java.io.IOException;

import java.util.ResourceBundle;

import static utils.NavigationUtil.showAlert;

public class indexOffreController {

    @FXML
    private Label availableOffersLabel;
    @FXML private ListView<Offre> offersListView;
    @FXML private Button loadMoreButton;
    @FXML private Button addOffreButton;
    @FXML private Button backHomeButton;
    private Offre selectedOffre;
    private ResourceBundle bundle;

    @FXML
    private Button langButton;
    @FXML
    private Button englishButton;

    private ObservableList<Offre> offersList = FXCollections.observableArrayList();
    private OffreService offreService = new OffreService(); // Instance of OffreService

    // Initialize method to populate the ListView

    public void initialize() {
        try {
            loadOffers(); // Load the offers from the database




        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the cell factory to customize how each offer is displayed
        offersListView.setCellFactory(param -> new ListCell<Offre>() {
            @Override
            protected void updateItem(Offre offre, boolean empty) {
                super.updateItem(offre, empty);
                if (empty || offre == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create VBox card
                    VBox card = new VBox(5);
                    card.setStyle("-fx-background-color: #a5d6a7; -fx-background-radius: 15; -fx-padding: 12;");

                    Label titleLabel = new Label(offre.getTitre());
                    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                    Label competenceLabel = new Label("Compétence: " + offre.getComp());
                    Label statutLabel = new Label("Statut: " + (offre.isStatut() ? "Actif" : "Inactif"));

                    card.getChildren().addAll(titleLabel, competenceLabel, statutLabel);

                    // Hover effect
                    card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #81c784; -fx-background-radius: 15; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"));
                    card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #a5d6a7; -fx-background-radius: 15; -fx-padding: 12;"));

                    setGraphic(card);
                }
            }
        });

        // Handle click on item
        offersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                openDetailOffrePage(newValue);
            }
        });

        // ⚡️ Add LanguageManager loading
        LanguageManager.selectedLanguage = "default";  // Default to English

        LanguageManager.loadLanguage();
        updateUIText(); // <- You already have this function to refresh your labels/buttons etc.



    }

    // Method to load offers into the ListView
    private void loadOffers() throws SQLException {
        List<Offre> offersFromDb = offreService.select(); // Fetch offers from the database
        offersList.setAll(offersFromDb); // Set the fetched offers to the ListView
        offersListView.setItems(offersList);
    }

    // Handle the Load More button click (optional feature for pagination)
    @FXML
    private void loadMoreOffers(ActionEvent event) {
        // For now, let's just print a message when clicking Load More
        System.out.println("Load More Offers clicked!");
        // You can implement pagination logic here if needed
    }

    // Handle the "Ajouter Offre" button click to go to the offer creation page
    @FXML
    private void handleAjouterOffre(ActionEvent event) throws IOException {
        try {
            // Load just the ajouterOffre content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/ajouterOffre.fxml"));
            Parent content = contentLoader.load();

            // Get the reference to the main content pane from the parent
            StackPane mainContentPane = (StackPane) addOffreButton.getScene().lookup("#mainContentPane");

            // Clear existing content and add the new content
            mainContentPane.getChildren().clear();
            mainContentPane.getChildren().add(content);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load add offer page", "error" );
        }
    }
    private void openDetailOffrePage(Offre selectedOffre) {
        try {
            // Get the current scene
            Scene currentScene = offersListView.getScene();
            if (currentScene == null) {
                throw new IllegalStateException("Scene is not set");
            }

            // Look for the main content pane
            StackPane mainContentPane = (StackPane) currentScene.lookup("#mainContentPane");
            if (mainContentPane == null) {
                // Try looking in the root first
                Parent root = currentScene.getRoot();
                mainContentPane = (StackPane) root.lookup("#mainContentPane");

                if (mainContentPane == null) {
                    throw new IllegalStateException("Could not find mainContentPane in scene");
                }
            }

            // Load and display content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/detailOffre.fxml"));
            Parent content = contentLoader.load();

            mainContentPane.getChildren().clear();
            mainContentPane.getChildren().add(content);

            detailOffreController detailController = contentLoader.getController();
            detailController.setOffre(selectedOffre);

        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load offer details: " + e.getMessage(), "error");
        }
    }

    // Method to switch to Deutsch
    @FXML
    private void switchLang() {
        LanguageManager.selectedLanguage = "de";  // Set the language to Deutsch
        LanguageManager.loadLanguage();
        updateUIText();  // Update UI after changing language
    }

    @FXML
    private void switchToEnglish() {
        LanguageManager.selectedLanguage = "default";  // Set the language to English
        LanguageManager.loadLanguage();
        updateUIText();  // Update UI after changing language
    }

    private void updateUIText() {
        availableOffersLabel.setText(LanguageManager.getString("availableOffers"));
        loadMoreButton.setText(LanguageManager.getString("loadMore"));
        addOffreButton.setText(LanguageManager.getString("addOffre"));
    }

    public void handleReturnHome(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backHomeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
