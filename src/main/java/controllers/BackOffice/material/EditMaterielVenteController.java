package controllers.BackOffice.material;

import Models.Categorie;
import Models.MaterielVente;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.CategorieService;
import services.MaterielService;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class EditMaterielVenteController {

    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Categorie> categorieCombo;
    @FXML private CheckBox disponibiliteCheck;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private TextField imageField;

    private final MaterielService materielService = new MaterielService();
    private final CategorieService categorieService = new CategorieService();
    private MaterielVente materielToEdit;

    public void setMaterielToEdit(MaterielVente materiel) {
        this.materielToEdit = materiel;
        populateFields();
    }

    @FXML
    public void initialize() {
        // Load categories
        categorieService.findAll().forEach(categorie ->
                categorieCombo.getItems().add(categorie));

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

            // Set selected category
            categorieService.findAll().stream()
                    .filter(c -> c.getId() == materielToEdit.getCategorieId())
                    .findFirst()
                    .ifPresent(categorieCombo.getSelectionModel()::select);
        }
    }

    private void handleSave() {
        try {
            // Trim all input fields
            String nom = nomField.getText().trim();
            String prixText = prixField.getText().trim();
            String description = descriptionField.getText().trim();
            String imagePath = imageField.getText().trim();
            Categorie selectedCategorie = categorieCombo.getSelectionModel().getSelectedItem();

            // Validate required fields
            if (nom.isEmpty() || prixText.isEmpty() || description.isEmpty() || selectedCategorie == null) {
                showAlert("Error", "Please fill all required fields", Alert.AlertType.ERROR);
                return;
            }

            // Validate price
            double prix;
            try {
                prix = Double.parseDouble(prixText);
                if (prix <= 0) {
                    showAlert("Error", "Price must be greater than 0", Alert.AlertType.ERROR);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid price", Alert.AlertType.ERROR);
                return;
            }

            // Update material
            materielToEdit.setNom(nom);
            materielToEdit.setPrix(prix);
            materielToEdit.setDescription(description);
            materielToEdit.setDisponibilite(disponibiliteCheck.isSelected());
            materielToEdit.setCategorieId(selectedCategorie.getId());

            // Handle image only if changed
            if (!imagePath.isEmpty() && !imagePath.equals(materielToEdit.getImage())) {
                String imageFileName = Paths.get(imagePath).getFileName().toString();
                Path destination = Paths.get("src/main/resources/images_materiels/", imageFileName);
                Files.copy(Paths.get(imagePath), destination, StandardCopyOption.REPLACE_EXISTING);
                materielToEdit.setImage(imageFileName);
            }

            // Save changes
            materielService.modifierVente(materielToEdit);

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