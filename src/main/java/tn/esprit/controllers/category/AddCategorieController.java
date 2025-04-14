package tn.esprit.controllers.category;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.Categorie;
import tn.esprit.services.CategorieService;

public class AddCategorieController {
    private final CategorieService categorieService = new CategorieService();

    @FXML private TextField nomField;
    @FXML private TextArea descArea;

    @FXML
    private void handleSave() {
        if (validateInput()) {
            Categorie categorie = new Categorie();
            categorie.setNom(nomField.getText().trim());
            categorie.setDescription(descArea.getText().trim());

            categorieService.ajouter(categorie);
            closeWindow();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInput() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le nom est obligatoire", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void closeWindow() {
        ((Stage) nomField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}