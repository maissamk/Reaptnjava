package controllers.BackOffice.material;

import models.MaterielLocation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.MaterielService;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class EditMaterielLocationController {

    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TextArea descriptionField;
    @FXML private CheckBox disponibiliteCheck;
    @FXML private TextField imageField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private final MaterielService materielService = new MaterielService();
    private MaterielLocation materielToEdit;

    public void setMaterielToEdit(MaterielLocation materiel) {
        this.materielToEdit = materiel;
        populateFields();
    }

    @FXML
    public void initialize() {
        saveBtn.setOnAction(e -> handleSave());
        cancelBtn.setOnAction(e -> handleCancel());
    }

    private void populateFields() {
        if (materielToEdit != null) {
            nomField.setText(materielToEdit.getNom());
            prixField.setText(String.valueOf(materielToEdit.getPrix()));
            descriptionField.setText(materielToEdit.getDescription());
            disponibiliteCheck.setSelected(materielToEdit.isDisponibilite());
            imageField.setText(materielToEdit.getImage());
        }
    }

    private void handleSave() {
        try {
            // Validate fields
            if (nomField.getText().isEmpty() || prixField.getText().isEmpty() ||
                    descriptionField.getText().isEmpty()) {
                showAlert("Error", "Please fill all required fields", Alert.AlertType.ERROR);
                return;
            }

            double prix;
            try {
                prix = Double.parseDouble(prixField.getText());
                if (prix <= 0) {
                    showAlert("Error", "Price must be greater than 0", Alert.AlertType.ERROR);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid price", Alert.AlertType.ERROR);
                return;
            }

            // Update material
            materielToEdit.setNom(nomField.getText());
            materielToEdit.setPrix(prix);
            materielToEdit.setDescription(descriptionField.getText());
            materielToEdit.setDisponibilite(disponibiliteCheck.isSelected());

            // Handle image
            String imagePath = imageField.getText();
            if (!imagePath.isEmpty() && !imagePath.equals(materielToEdit.getImage())) {
                String imageFileName = Paths.get(imagePath).getFileName().toString();
                Path destination = Paths.get("src/main/resources/images_materiels/", imageFileName);
                Files.copy(Paths.get(imagePath), destination, StandardCopyOption.REPLACE_EXISTING);
                materielToEdit.setImage(imageFileName);
            }

            // Save changes
            materielService.modifierLocation(materielToEdit);

            // Close window
            ((Stage) saveBtn.getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert("Error", "Error while updating: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleCancel() {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            imageField.setText(file.getAbsolutePath());
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}