package controllers.BackOffice.category;

import Models.Categorie;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.CategorieService;

public class EditCategorieController {
    private final CategorieService categorieService = new CategorieService();
    private Categorie categorieToEdit;

    @FXML private TextField nomField;
    @FXML private TextArea descArea;

    public void setCategorieToEdit(Categorie categorie) {
        this.categorieToEdit = categorie;
        nomField.setText(categorie.getNom());
        descArea.setText(categorie.getDescription());
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            categorieToEdit.setNom(nomField.getText().trim());
            categorieToEdit.setDescription(descArea.getText().trim());

            categorieService.modifier(categorieToEdit);
            closeWindow();
        }
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler les modifications");
        alert.setContentText("Voulez-vous vraiment annuler? Toutes les modifications seront perdues.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                closeWindow();
            }
        });
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