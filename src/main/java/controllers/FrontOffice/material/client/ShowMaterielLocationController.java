package controllers.FrontOffice.material.client;

import Models.MaterielLocation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.MaterielService;
import utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ShowMaterielLocationController implements Initializable {

    @FXML
    private ImageView imageView;
    @FXML
    private Label nomLabel;
    @FXML
    private Label prixLabel;
    @FXML
    private TextArea descriptionLabel;
    @FXML
    private Label disponibiliteLabel;
    @FXML
    private Label createdAtLabel;
    @FXML
    private Button louer;

    private final MaterielService service = new MaterielService();
    private MaterielLocation currentMateriel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SessionManager.getInstance().getCurrentUser() == null) {
            louer.setDisable(true);
            louer.setText("Vous devez vous connecter pour louer");
        }
    }

    public void setMateriel(MaterielLocation materiel) {
        this.currentMateriel = materiel;
        updateUI();
    }

    private void updateUI() {
        if (currentMateriel == null) return;

        // Load image with fallback
        try {
            String imagePath = "file:src/main/resources/images_materiels/" + currentMateriel.getImage();
            imageView.setImage(new Image(imagePath));
        } catch (Exception e) {
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.png")));
            } catch (Exception ex) {
                System.err.println("Could not load default image: " + ex.getMessage());
            }
        }

        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        nomLabel.setText(currentMateriel.getNom());
        prixLabel.setText(String.format("%.2f TND/jour", currentMateriel.getPrix()));
        descriptionLabel.setText(currentMateriel.getDescription());

        boolean isAvailable = currentMateriel.isDisponibilite();
        disponibiliteLabel.setText(isAvailable ? "Disponible" : "Non disponible");
        disponibiliteLabel.setStyle("-fx-text-fill: " + (isAvailable ? "#4CAF50" : "#F44336"));

        louer.setDisable(!isAvailable);
    }

    @FXML
    public void louer(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUser() != null) {
            if (currentMateriel == null) {
                showAlert("Erreur", "Aucun matériel sélectionné", Alert.AlertType.ERROR);
                return;
            }

            if (!currentMateriel.isDisponibilite()) {
                showAlert("Erreur", "Ce matériel n'est plus disponible", Alert.AlertType.ERROR);
                return;
            }

            try {
                service.louerMateriel(currentMateriel.getId(), SessionManager.getInstance().getCurrentUser().getId());

                // Update model
                currentMateriel.setDisponibilite(false);
                currentMateriel.setUserIdMaterielLocationId(SessionManager.getInstance().getCurrentUser().getId());

                // Update UI
                updateUI();
                showAlert("Succès", "Matériel loué avec succès!", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la location: " + e.getMessage(), Alert.AlertType.ERROR);
            }
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