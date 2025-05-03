package controllers.FrontOffice.parcelles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Models.ParcelleProprietes;

import java.io.IOException;

public class ParcelleCard {

    @FXML public VBox card;
    @FXML public Label titre;
    @FXML public Label prix;
    @FXML public Label statut;
    @FXML public Label taille;
    @FXML public ImageView imageView;
    @FXML public Button btnModifier;
    @FXML public Button btnSupprimer;
    @FXML public Button btnHistorique; // Nouveau bouton pour l'historique

    private ParcelleProprietes currentParcelle;

    public void setData(ParcelleProprietes parcelle) {
        this.currentParcelle = parcelle;

        // Set basic data
        titre.setText(parcelle.getTitre());
        prix.setText(String.format("%.2f DT", parcelle.getPrix()));
        statut.setText(parcelle.getStatus().toUpperCase());
        taille.setText("Superficie: " + parcelle.getTaille() + " m²");

        // Load image
        loadImage(parcelle.getImage());

        // Setup interactions
        setupCardHover();
        setupTitleClick();
        setupStatusStyle();
    }

    private void loadImage(String imagePath) {
        try {
            Image image = new Image(getClass().getResourceAsStream("/" + imagePath));
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(280);
            imageView.setFitHeight(150);
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.png")));
        }
    }

    private void setupCardHover() {
        card.setOnMouseEntered(e -> {
            card.getStyleClass().add("card-hover");
            card.setStyle("-fx-border-color: #3498db;");
        });

        card.setOnMouseExited(e -> {
            card.getStyleClass().remove("card-hover");
            card.setStyle("-fx-border-color: #bdc3c7;");
        });
    }

    private void setupTitleClick() {
        titre.setOnMouseClicked(this::handleTitleClick);
        titre.setStyle("-fx-cursor: hand;");
    }

    private void setupStatusStyle() {
        String status = currentParcelle.getStatus().toLowerCase();
        String styleClass = "status-default";

        if (status.contains("louer")) {
            styleClass = "status-available";
        } else if (status.contains("vendre")) {
            styleClass = "status-sale";
        }

        statut.getStyleClass().add(styleClass);
    }

    @FXML
    private void handleTitleClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/DetailParcelle.fxml"));
            Parent root = loader.load();

            DetailParcelle controller = loader.getController();
            controller.initData(currentParcelle);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails de la parcelle");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les détails");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Button getBtnModifier() {
        return btnModifier;
    }

    public Button getBtnSupprimer() {
        return btnSupprimer;
    }

    public Button getBtnHistorique() {
        return btnHistorique;
    }
}