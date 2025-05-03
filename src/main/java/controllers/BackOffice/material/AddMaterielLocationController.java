package controllers.BackOffice.material;

import Models.MaterielLocation;
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

public class AddMaterielLocationController {

    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TextArea descriptionField;
    @FXML private CheckBox disponibiliteCheck;
    @FXML private TextField imageField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private final MaterielService materielService = new MaterielService();

    @FXML
    public void initialize() {
        saveBtn.setOnAction(e -> handleSave());
        cancelBtn.setOnAction(e -> handleCancel());
    }

    private void handleSave() {
        try {
            // Validation des champs
            if (nomField.getText().isEmpty() || prixField.getText().isEmpty() ||
                    descriptionField.getText().isEmpty() || imageField.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.ERROR);
                return;
            }

            double prix;
            try {
                prix = Double.parseDouble(prixField.getText());
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Veuillez entrer un prix valide", Alert.AlertType.ERROR);
                return;
            }

            // Création du matériel
            MaterielLocation materiel = new MaterielLocation();
            materiel.setNom(nomField.getText());
            materiel.setPrix(prix);
            materiel.setDescription(descriptionField.getText());
            materiel.setDisponibilite(disponibiliteCheck.isSelected());

            // Gestion de l'image
            String imagePath = imageField.getText();
            if (!imagePath.isEmpty()) {
                String imageFileName = Paths.get(imagePath).getFileName().toString();
                Path destination = Paths.get("src/main/resources/images_materiels/", imageFileName);
                Files.copy(Paths.get(imagePath), destination, StandardCopyOption.REPLACE_EXISTING);
                materiel.setImage(imageFileName);
            }

            // Enregistrement
            materielService.ajouterLocation(materiel);

            // Fermeture de la fenêtre
            ((Stage) saveBtn.getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
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