package controllers.FrontOffice.parcelles;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Models.ParcelleProprietes;
import services.ParcelleProprietesService;

import java.io.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Modifierparcelles {

    @FXML private TextField titreField;
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
    private Runnable refreshCallback;
    private final ParcelleProprietesService service = new ParcelleProprietesService();

    public void setParcelleToEdit(ParcelleProprietes parcelle) {
        this.parcelleToEdit = parcelle;
        populateFields();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
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
            fileNameLabel.setText(parcelleToEdit.getImage());
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
        }
    }

    @FXML
    private void handleModifier() {
        if (!validateInputs()) return;

        updateParcelleFromFields();

        try {
            service.update(parcelleToEdit);
            showSuccess("Parcelle modifiée avec succès !");
            closeWindowAndRefresh();
        } catch (Exception e) {
            showAlert("Erreur", "Échec de la mise à jour: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (titreField.getText().isEmpty() || prixField.getText().isEmpty() ||
                statutCombo.getValue() == null || typeTerrain.getValue() == null ||
                tailleField.getText().isEmpty() || emplacementField.getText().isEmpty() ||
                nomProprio.getText().isEmpty() || contactProprio.getText().isEmpty() ||
                emailField.getText().isEmpty()) {

            showAlert("Erreur", "Tous les champs sont obligatoires.");
            return false;
        }

        if (!contactProprio.getText().matches("\\d{8}")) {
            showAlert("Erreur", "Le contact doit contenir exactement 8 chiffres.");
            return false;
        }

        if (!emailField.getText().matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            showAlert("Erreur", "Email invalide.");
            return false;
        }

        return true;
    }

    private void updateParcelleFromFields() {
        parcelleToEdit.setTitre(titreField.getText());
        parcelleToEdit.setPrix(Double.parseDouble(prixField.getText()));
        parcelleToEdit.setStatus(statutCombo.getValue());
        parcelleToEdit.setType_terrain(typeTerrain.getValue());
        parcelleToEdit.setTaille(Double.parseDouble(tailleField.getText()));
        parcelleToEdit.setEmplacement(emplacementField.getText());
        parcelleToEdit.setNom_proprietaire(nomProprio.getText());
        parcelleToEdit.setContact_proprietaire(contactProprio.getText());
        parcelleToEdit.setEmail(emailField.getText());
        parcelleToEdit.setEst_disponible(disponibleCheckBox.isSelected());
        parcelleToEdit.setDate_misajour_annonce(Timestamp.valueOf(LocalDateTime.now()));

        // IMAGE HANDLING WITH ABSOLUTE PATH
        if (selectedFile != null) {
            try {
                // 1. Generate unique filename
                String originalName = selectedFile.getName();
                String fileExtension = originalName.substring(originalName.lastIndexOf("."));
                String fileName = "parcel_" + System.currentTimeMillis() + fileExtension;

                // 2. Define your ABSOLUTE base path
                String basePath = "C:/Users/romdh/Downloads/pi2025/pi2025/public/uploads/images/";

                // 3. Verify and create directory structure
                File uploadDir = new File(basePath);
                if (!uploadDir.exists()) {
                    if (!uploadDir.mkdirs()) {
                        showAlert("Error", "Failed to create directory: " + basePath);
                        return;
                    }
                    System.out.println("Created directory: " + uploadDir.getAbsolutePath());
                }

                // 4. Check write permissions
                if (!uploadDir.canWrite()) {
                    showAlert("Error", "No write permissions for: " + basePath);
                    return;
                }

                // 5. Create destination file
                File destFile = new File(uploadDir, fileName);

                // 6. Copy the file
                try (InputStream in = new FileInputStream(selectedFile);
                     OutputStream out = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) > 0) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                // 7. Store the ABSOLUTE path in database
                parcelleToEdit.setImage(destFile.getAbsolutePath());

                // Debug output
                System.out.println("Successfully saved to: " + destFile.getAbsolutePath());
                System.out.println("Stored in DB: " + parcelleToEdit.getImage());

            } catch (Exception e) {
                showAlert("Error", "Failed to save image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private void closeWindowAndRefresh() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();

        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
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
}