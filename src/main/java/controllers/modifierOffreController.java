package controllers;

import javafx.fxml.FXML;
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
    @FXML private Button cancelButton;

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

            // Save the updated Offre using the service
            OffreService service = new OffreService();
            service.update(currentOffre); // Update the Offer in the database

            // Optionally, show a success message to the user
            showAlert("Success", "Offer updated successfully!");

            // Return to the previous screen (e.g., indexOffre.fxml) after saving
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/indexOffre.fxml"));
            Scene scene = new Scene(loader.load());
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

    @FXML
    private void handleCancel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/indexOffre.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) titreField.getScene().getWindow();
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
