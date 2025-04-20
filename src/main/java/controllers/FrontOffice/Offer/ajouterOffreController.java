package controllers.FrontOffice.Offer;

import controllers.FrontOffice.BaseFrontController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.OffreService;
import models.Offre;

import java.io.IOException;
import java.sql.SQLException;

public class ajouterOffreController {


    @FXML private TextField titreField;
    @FXML private TextArea descField;
    @FXML private TextField competenceField;
    @FXML private CheckBox statutCheckBox;
    @FXML private Button RetourButton;


    private OffreService offreService = new OffreService();

    // Handle the Add Offer action
    @FXML
    private void handleAddOffre() {

        String titre = titreField.getText().trim();
        String description = descField.getText().trim();
        String competence = competenceField.getText().trim();
        boolean statut = statutCheckBox.isSelected();

        // 1. Vérifier les champs vides
        if (titre.isEmpty() || description.isEmpty() || competence.isEmpty()) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }




        // 3. Créer et sauvegarder l'offre
        try {
            Offre newOffre = new Offre();

            newOffre.setTitre(titre);
            newOffre.setDescr(description);
            newOffre.setComp(competence);
            newOffre.setStatut(statut);

            offreService.add(newOffre);

            showAlert("Succès", "Offre ajoutée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de l'ajout de l'offre.");
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

    // Helper method to show alert messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
