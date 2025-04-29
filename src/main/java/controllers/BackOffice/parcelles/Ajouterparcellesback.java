package controllers.BackOffice.parcelles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.ParcelleProprietes;
import services.ParcelleProprietesService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;

public class Ajouterparcellesback {
    @FXML
    private TextField titreField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> statutCombo;
    @FXML private ComboBox<String> typeTerrain;
    @FXML private TextField tailleField;
    @FXML private DatePicker dateCreation;
    @FXML private DatePicker dateMaj;
    @FXML private TextField emplacementField;
    @FXML private TextField nomProprio;
    @FXML private TextField contactProprio;
    @FXML private TextField emailField;
    @FXML private CheckBox disponibleCheckBox;
    @FXML private Label fileNameLabel;

    private File selectedFile;

    @FXML
    private void initialize() {
        statutCombo.getItems().addAll("Louer", "Vendre", "En location");
        typeTerrain.getItems().addAll("Agricole", "Résidentiel", "Commercial", "Mixte");

        // Limiter la saisie pour la taille du terrain (uniquement des chiffres)
        tailleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                tailleField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        // Limiter la saisie pour le prix (uniquement des chiffres et une décimale)
        prixField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                prixField.setText(oldVal);
            }
        });

        // Limiter le champ "Contact du propriétaire" à des chiffres uniquement (max 8 chiffres)
        contactProprio.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                contactProprio.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (newVal.length() > 8) {
                contactProprio.setText(newVal.substring(0, 8)); // Limite à 8 chiffres
            }
        });
    }

    @FXML
    public void handleAjouterContrat() {
        try {
            // Charger le fichier FXML du contrat
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajoutercontrat.fxml"));
            Scene scene = new Scene(loader.load());

            // Créer une nouvelle fenêtre (Stage) pour afficher "ajoutercontrat.fxml"
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Ajouter Contrat");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'exception (par exemple, afficher un message d'erreur)
        }
    }

    @FXML
    private void handleFileSelect() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
        } else {
            fileNameLabel.setText("Aucun fichier choisi");
        }
    }

    @FXML
    private void handleAfficherListe() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Afficherparcelles.fxml"));
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur de navigation", "Impossible de charger la vue d'affichage : " + e.getMessage());
        }
    }

    @FXML
    private void handleEnregistrer() {
        String titre = titreField.getText();
        String prix = prixField.getText();
        String statut = statutCombo.getValue();
        String type = typeTerrain.getValue();
        String taille = tailleField.getText();
        LocalDate dateCreationVal = dateCreation.getValue();
        LocalDate dateMajVal = dateMaj.getValue();
        String emplacement = emplacementField.getText();
        String nom = nomProprio.getText();
        String contact = contactProprio.getText();
        String email = emailField.getText();
        boolean estDisponible = disponibleCheckBox.isSelected();

        // Vérifier si tous les champs obligatoires sont remplis
        if (titre.isEmpty() || prix.isEmpty() || contact.isEmpty() || email.isEmpty() || selectedFile == null) {
            showAlert("Erreur", "Tous les champs (Titre, Prix, Contact, Email et Image) sont obligatoires.");
            return;
        }

        // Vérifier si l'email est valide
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            showAlert("Email invalide", "Veuillez saisir un email valide.");
            return;
        }

        // Vérifier si les dates sont remplies
        if (dateCreationVal == null || dateMajVal == null) {
            showAlert("Erreur", "Veuillez sélectionner les dates de création et de mise à jour.");
            return;
        }

        // Vérifier si le contact du propriétaire contient au maximum 8 chiffres
        if (contact.length() > 8) {
            showAlert("Erreur", "Le contact du propriétaire ne doit pas dépasser 8 chiffres.");
            return;
        }

        String relativeImagePath = "images/default.png"; // Valeur par défaut

        // Si un fichier est sélectionné, le copier dans le répertoire images
        if (selectedFile != null) {
            try {
                String fileName = selectedFile.getName();
                File destDir = new File("src/main/resources/images");
                if (!destDir.exists()) destDir.mkdirs();

                File destFile = new File(destDir, fileName);

                try (FileInputStream fis = new FileInputStream(selectedFile);
                     FileOutputStream fos = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }

                relativeImagePath = "images/" + fileName;

            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors de la copie de l'image : " + e.getMessage());
                return;
            }
        }

        // Création de l'objet ParcelleProprietes et appel à la méthode d'ajout
        ParcelleProprietes parcelle = new ParcelleProprietes();
        parcelle.setTitre(titre);
        parcelle.setDescription("");
        parcelle.setPrix(Double.parseDouble(prix));
        parcelle.setStatus(statut);
        parcelle.setType_terrain(type);
        parcelle.setTaille(Double.parseDouble(taille));
        parcelle.setDate_creation_annonce(Timestamp.valueOf(dateCreationVal.atStartOfDay()));
        parcelle.setDate_misajour_annonce(Timestamp.valueOf(dateMajVal.atStartOfDay()));
        parcelle.setEmplacement(emplacement);
        parcelle.setNom_proprietaire(nom);
        parcelle.setContact_proprietaire(contact);
        parcelle.setEmail(email);
        parcelle.setEst_disponible(estDisponible);
        parcelle.setImage(relativeImagePath);
        parcelle.setLatitude("0.0");
        parcelle.setLongitude("0.0");

        // Enregistrement de la parcelle
        ParcelleProprietesService service = new ParcelleProprietesService();
        service.add(parcelle);

        showSuccess("Parcelle ajoutée avec succès !");
        clearFields();
    }

    private void clearFields() {
        titreField.clear();
        prixField.clear();
        statutCombo.getSelectionModel().clearSelection();
        typeTerrain.getSelectionModel().clearSelection();
        tailleField.clear();
        dateCreation.setValue(null);
        dateMaj.setValue(null);
        emplacementField.clear();
        nomProprio.clear();
        contactProprio.clear();
        emailField.clear();
        disponibleCheckBox.setSelected(false);
        fileNameLabel.setText("Aucun fichier choisi");
        selectedFile = null;
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
