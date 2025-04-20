package controllers.FrontOffice.Offer;

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
        String idaText = idaField.getText().trim();
        String titre = titreField.getText().trim();
        String description = descField.getText().trim();
        String competence = competenceField.getText().trim();
        boolean statut = statutCheckBox.isSelected();

        // 1. Vérifier les champs vides
        if (idaText.isEmpty() || titre.isEmpty() || description.isEmpty() || competence.isEmpty()) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // 2. Vérifier que l'ID admin est bien un entier
        int ida;
        try {
            ida = Integer.parseInt(idaText);
            if (ida <= 0) {
                showAlert("ID invalide", "L'ID Admin doit être un entier positif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur de saisie", "L'ID Admin doit être un nombre entier.");
            return;
        }

        // 3. Créer et sauvegarder l'offre
        try {
            Offre newOffre = new Offre();
            newOffre.setIda(ida);
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
