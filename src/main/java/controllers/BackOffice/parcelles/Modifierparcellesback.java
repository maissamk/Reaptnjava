package controllers.BackOffice.parcelles;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import models.ParcelleProprietes;
import services.ParcelleProprietesService;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Modifierparcellesback {

    @FXML
    private TextField titreField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> statutCombo;
    @FXML private ComboBox<String> typeTerrain;
    @FXML private TextField tailleField;
    @FXML private TextField emplacementField;
    @FXML private TextField nomProprio;
    @FXML private TextField contactProprio;
    @FXML private TextField emailField;
    @FXML private CheckBox disponibleCheckBox;
    @FXML private Label fileNameLabel;

    private File selectedFile;
    private ParcelleProprietes parcelleToEdit;

    ParcelleProprietesService service = new ParcelleProprietesService();

    public void setParcelleToEdit(ParcelleProprietes parcelle) {
        this.parcelleToEdit = parcelle;
        populateFields();
    }

    private void populateFields() {
        if (parcelleToEdit != null) {
            titreField.setText(parcelleToEdit.getTitre());
            prixField.setText(String.valueOf(parcelleToEdit.getPrix()));
            statutCombo.setValue(parcelleToEdit.getStatus());
            typeTerrain.setValue(parcelleToEdit.getType_terrain());
            tailleField.setText(String.valueOf(parcelleToEdit.getTaille()));
            emplacementField.setText(parcelleToEdit.getEmplacement());
            nomProprio.setText(parcelleToEdit.getNom_proprietaire());
            contactProprio.setText(parcelleToEdit.getContact_proprietaire());
            emailField.setText(parcelleToEdit.getEmail());
            disponibleCheckBox.setSelected(parcelleToEdit.isEst_disponible());
            fileNameLabel.setText(parcelleToEdit.getImage() != null ? parcelleToEdit.getImage() : "Aucun fichier");
        }
    }

    @FXML
    private void initialize() {
        statutCombo.getItems().addAll("Louer", "Vendre", "En location");
        typeTerrain.getItems().addAll("Agricole", "Résidentiel", "Commercial", "Mixte");

        tailleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                tailleField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        prixField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                prixField.setText(oldVal);
            }
        });

        contactProprio.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*") || newVal.length() > 8) {
                contactProprio.setText(oldVal);
            }
        });
    }

    @FXML
    private void handleFileSelect() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedFile = file;
            fileNameLabel.setText(file.getName());
        } else {
            fileNameLabel.setText("Aucun fichier choisi");
        }
    }

    @FXML
    private void handleModifier() {
        if (parcelleToEdit == null) return;

        // 1. Vérification des champs obligatoires
        if (titreField.getText().isEmpty() || prixField.getText().isEmpty() ||
                statutCombo.getValue() == null || typeTerrain.getValue() == null ||
                tailleField.getText().isEmpty() || emplacementField.getText().isEmpty() ||
                nomProprio.getText().isEmpty() || contactProprio.getText().isEmpty() ||
                emailField.getText().isEmpty()) {

            showAlert("Erreur", "Tous les champs sont obligatoires.");
            return;
        }

        // 2. Vérification du contact (8 chiffres)
        String contact = contactProprio.getText().trim();
        if (!contact.matches("\\d{8}")) {
            showAlert("Erreur", "Le contact doit contenir exactement 8 chiffres.");
            return;
        }

        // 3. Vérification de l'email
        if (!emailField.getText().matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            showAlert("Erreur", "Email invalide.");
            return;
        }

        // 4. Vérification de l’image
        if (selectedFile == null && (parcelleToEdit.getImage() == null || parcelleToEdit.getImage().isEmpty())) {
            showAlert("Erreur", "Veuillez sélectionner une image.");
            return;
        }

        // 5. Mise à jour de l'objet
        parcelleToEdit.setTitre(titreField.getText());
        parcelleToEdit.setPrix(Double.parseDouble(prixField.getText()));
        parcelleToEdit.setStatus(statutCombo.getValue());
        parcelleToEdit.setType_terrain(typeTerrain.getValue());
        parcelleToEdit.setTaille(Double.parseDouble(tailleField.getText()));
        parcelleToEdit.setEmplacement(emplacementField.getText());
        parcelleToEdit.setNom_proprietaire(nomProprio.getText());
        parcelleToEdit.setContact_proprietaire(contact);
        parcelleToEdit.setEmail(emailField.getText());
        parcelleToEdit.setEst_disponible(disponibleCheckBox.isSelected());
        parcelleToEdit.setDate_misajour_annonce(Timestamp.valueOf(LocalDateTime.now()));

        if (selectedFile != null) {
            parcelleToEdit.setImage(selectedFile.getAbsolutePath());
        }

        service.update(parcelleToEdit);
        showSuccess("Parcelle modifiée avec succès !");
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleAnnuler(ActionEvent actionEvent) {
        Button sourceButton = (Button) actionEvent.getSource();
        sourceButton.getScene().getWindow().hide();
    }
}
