package tn.esprit.controllers.material;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.Categorie;
import tn.esprit.entities.MaterielVente;
import tn.esprit.services.CategorieService;
import tn.esprit.services.MaterielService;
import tn.esprit.utils.FileUploader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class AddMaterielVenteController {

    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Categorie> categorieCombo;
    @FXML private CheckBox disponibiliteCheck;
     @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
     @FXML private TextField imageField;

    private File selectedImageFile;
    private final MaterielService materielService = new MaterielService();
    private final CategorieService categorieService = new CategorieService();

    @FXML
    public void initialize() {
        // Charger les catégories
        categorieService.findAll().forEach(categorie ->
                categorieCombo.getItems().add(categorie));

        saveBtn.setOnAction(e -> handleSave());
        cancelBtn.setOnAction(e -> handleCancel());

    }

    private void handleSave() {
        try {
            // Validation des champs requis
            if (nomField.getText().isEmpty() || prixField.getText().isEmpty() ||
                    descriptionField.getText().isEmpty() || categorieCombo.getSelectionModel().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires", Alert.AlertType.ERROR);
                return;
            }

            double prix;
            try {
                prix = Double.parseDouble(prixField.getText());
                if (prix <= 0) {
                    showAlert("Erreur", "Le prix doit être supérieur à 0", Alert.AlertType.ERROR);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Veuillez entrer un prix valide", Alert.AlertType.ERROR);
                return;
            }

            // Création du matériel
            MaterielVente materiel = new MaterielVente();
            materiel.setNom(nomField.getText());
            materiel.setPrix(prix);
            materiel.setDescription(descriptionField.getText());
            materiel.setDisponibilite(disponibiliteCheck.isSelected());
            materiel.setCategorieId(categorieCombo.getSelectionModel().getSelectedItem().getId());

            // Gestion de l'image
            String imagePath = imageField.getText();
            if (!imagePath.isEmpty()) {
                String imageFileName = Paths.get(imagePath).getFileName().toString();
                Path destination = Paths.get("src/main/resources/images_materiels/", imageFileName);
                Files.copy(Paths.get(imagePath), destination, StandardCopyOption.REPLACE_EXISTING);
                materiel.setImage(imageFileName);
            }

            // Enregistrement
            materielService.ajouterVente(materiel);

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