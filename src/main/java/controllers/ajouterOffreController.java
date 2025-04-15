package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.OffreService;
import models.Offre;

import java.sql.SQLException;

public class ajouterOffreController {

    @FXML private TextField idaField;
    @FXML private TextField titreField;
    @FXML private TextArea descField;
    @FXML private TextField competenceField;
    @FXML private CheckBox statutCheckBox;

    private OffreService offreService = new OffreService();

    // Handle the Add Offer action
    @FXML
    private void handleAddOffre() {
        // Retrieve values from the form
        String idaText = idaField.getText();
        String titre = titreField.getText();
        String description = descField.getText();
        String competence = competenceField.getText();
        boolean statut = statutCheckBox.isSelected();

        // Validate fields
        if (idaText.isEmpty() || titre.isEmpty() || description.isEmpty() || competence.isEmpty()) {
            // Show error if fields are empty
            showAlert("Erreur", "Tous les champs doivent être remplis !");
        } else {
            try {
                // Convert ida to Integer
                Integer ida = Integer.parseInt(idaText);

                // Create new Offre object and set values
                Offre newOffre = new Offre();
                newOffre.setIda(ida);
                newOffre.setTitre(titre);
                newOffre.setDescr(description);
                newOffre.setComp(competence);
                newOffre.setStatut(statut);

                // Add the new offer to the database
                offreService.add(newOffre);

                // Show success message
                showAlert("Succès", "Offre ajoutée avec succès !");
            } catch (NumberFormatException e) {
                // Handle invalid input for ida (should be an Integer)
                showAlert("Erreur", "L'ID Offre (ida) doit être un nombre valide.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Une erreur s'est produite lors de l'ajout de l'offre.");
            }
        }
    }

    // Navigate back to the index page (indexOffre.fxml)
    @FXML
    private void handleRetour() {
        // You can implement navigation logic here (go back to the previous screen)
        System.out.println("Retour clicked!");
    }

    // Helper method to show alert messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
