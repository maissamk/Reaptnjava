package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import models.Offre;
import services.OffreService;
import java.sql.SQLException;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class indexOffreController {

    @FXML private ListView<Offre> offersListView;
    @FXML private Button loadMoreButton;
    @FXML private Button addOffreButton;

    private ObservableList<Offre> offersList = FXCollections.observableArrayList();
    private OffreService offreService = new OffreService(); // Instance of OffreService

    // Initialize method to populate the ListView
    public void initialize() {
        try {
            loadOffers(); // Load the offers from the database
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the cell factory to display offer titles
        offersListView.setCellFactory(param -> new ListCell<Offre>() {
            @Override
            protected void updateItem(Offre offre, boolean empty) {
                super.updateItem(offre, empty);
                if (empty || offre == null) {
                    setText(null);
                } else {
                    setText(offre.getTitre()); // Display the title of each offer
                }
            }
        });

        // Add a listener to handle selection of an offer from the ListView
        offersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Open the details page for the selected offer
                openDetailOffrePage(newValue);
            }
        });
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
        Stage stage = (Stage) addOffreButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterOffre.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }

    // Method to open the details page for the selected offer
    private void openDetailOffrePage(Offre selectedOffre) {
        try {
            // Load the detailOffre.fxml page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detailOffre.fxml"));
            Parent root = loader.load();

            // Get the controller for the detailOffre page
            detailOffreController controller = loader.getController();
            controller.setOffre(selectedOffre);  // Pass the selected offer details

            // Open the details window
            Stage detailStage = new Stage();
            Scene detailScene = new Scene(root);
            detailStage.setScene(detailScene);
            detailStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
