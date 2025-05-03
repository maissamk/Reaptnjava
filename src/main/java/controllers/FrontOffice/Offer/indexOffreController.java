package controllers.FrontOffice.Offer;

import controllers.FrontOffice.BaseFrontController;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import Models.Offre;
import services.OffreService;
import java.sql.SQLException;
import java.util.List;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.LanguageManager;

import java.io.IOException;

import java.util.ResourceBundle;

public class indexOffreController {

    @FXML
    private Label availableOffersLabel;
    @FXML private ListView<Offre> offersListView;
    @FXML private Button loadMoreButton;
    @FXML private Button addOffreButton;
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

        FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
        Parent baseRoot = baseLoader.load();
        BaseFrontController baseController = baseLoader.getController();


        FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/ajouterOffre.fxml"));
        Parent content = contentLoader.load(); // content with its own controller & methods

        // Inject the page content into base layout
        baseController.getContentPane().getChildren().setAll(content);

        //display
        Scene scene = new Scene(baseRoot);
        Stage stage = (Stage) addOffreButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void openDetailOffrePage(Offre selectedOffre) {
        try {
            // Load base layout
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            // Load the detail content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/detailOffre.fxml"));
            Parent content = contentLoader.load();

            // Inject detail page into the base layout
            baseController.getContentPane().getChildren().setAll(content);

            // Get the detail controller and pass the selected Offre
            detailOffreController detailController = contentLoader.getController();
            detailController.setOffre(selectedOffre);

            // Display everything
            Scene scene = new Scene(baseRoot);
            Stage stage = (Stage) offersListView.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
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

}
