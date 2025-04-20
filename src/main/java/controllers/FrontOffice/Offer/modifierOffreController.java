package controllers.FrontOffice.Offer;

import controllers.FrontOffice.BaseFrontController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import models.Offre;
import services.OffreService;
import java.io.IOException;
import java.sql.SQLException;
import javafx.scene.control.Alert;



public class modifierOffreController {
    @FXML private TextField titreField;
    @FXML private TextField descrField;
    @FXML private TextField competenceField;
    @FXML private CheckBox statutCheckBox;
    @FXML private Button saveButton;
    @FXML private Button RetourButton;

    private Offre currentOffre;

    public void prefillForm(Offre offre) {
        this.currentOffre = offre;
        titreField.setText(offre.getTitre());
        descrField.setText(offre.getDescr());
        competenceField.setText(offre.getComp());
        statutCheckBox.setSelected(offre.isStatut());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Ensure that the fields are not empty
            if (titreField.getText().isEmpty() || descrField.getText().isEmpty() || competenceField.getText().isEmpty()) {
                showAlert("Error", "Please fill all fields before saving.");
                return; // Prevent further processing if fields are empty
            }

            // Get the updated values from the form fields
            currentOffre.setTitre(titreField.getText());
            currentOffre.setDescr(descrField.getText());
            currentOffre.setComp(competenceField.getText());
            currentOffre.setStatut(statutCheckBox.isSelected());

            System.out.println("Saving offer with ID: " + currentOffre.getId());

            // Save the updated Offre using the service
            OffreService service = new OffreService();
            service.update(currentOffre); // Update the Offer in the database

            // Optionally, show a success message to the user
            showAlert("Success", "Offer updated successfully!");


            // Return to the previous screen (e.g., indexOffre.fxml) after saving
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/Frontoffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/indexOffre.fxml"));
            Parent content = loader.load();

            baseController.getContentPane().getChildren().setAll(content);

            Scene scene = new Scene(baseRoot);
            Stage stage = (Stage) saveButton.getScene().getWindow(); // Ensure you're working with the correct window
            stage.setScene(scene);
            stage.show();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update the offer: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the previous screen: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }



    // Navigate back to the index page (indexOffre.fxml)
    @FXML
    private void handleRetour() {
        // You can implement navigation logic here (go back to the previous screen)
        try {
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/Frontoffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/Frontoffice/Offre/indexOffre.fxml"));
            Parent content = contentLoader.load(); // content with its own controller & methods

            // Inject the page content into base layout
            baseController.getContentPane().getChildren().setAll(content);

            // Now show the complete scene
            Scene scene = new Scene(baseRoot);
            Stage stage = (Stage) RetourButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);  // You can add a header text if necessary
        alert.setContentText(message);
        alert.showAndWait();
    }
}
