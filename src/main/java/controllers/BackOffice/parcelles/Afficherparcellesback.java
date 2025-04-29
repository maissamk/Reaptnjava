package controllers.BackOffice.parcelles;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.ParcelleProprietes;
import services.ParcelleProprietesService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Afficherparcellesback {
    @FXML
    private ListView<ParcelleProprietes> listView;
    @FXML private Button btnActualiser;
    @FXML private Button btnRetour;

    private final ParcelleProprietesService service = new ParcelleProprietesService();

    @FXML
    public void initialize() {
        configureListView();
        loadData();
    }

    private void configureListView() {
        listView.setCellFactory(param -> new ListCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label titre = new Label();
            private final Label prix = new Label();
            private final Label statut = new Label();
            private final Label taille = new Label();
            private final HBox actions = new HBox();
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");

            private final VBox card = new VBox();

            {
                // Configuration de la mise en page
                imageView.setFitWidth(200);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);

                titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                prix.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 14px;");
                statut.setStyle("-fx-font-style: italic;");

                actions.setSpacing(10);
                actions.getChildren().addAll(btnModifier, btnSupprimer);

                card.setSpacing(10);
                card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15px; -fx-border-color: #ddd; -fx-border-radius: 5px;");
                card.setPrefWidth(300);
                card.getChildren().addAll(imageView, titre, prix, statut, taille, actions);

                // Style des boutons
                btnModifier.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                // Gestion des événements
                btnModifier.setOnAction(event -> {
                    ParcelleProprietes parcelle = getItem();
                    openModificationWindow(parcelle);
                });

                btnSupprimer.setOnAction(event -> {
                    ParcelleProprietes parcelle = getItem();
                    service.delete(parcelle);
                    loadData();
                });
            }

            @Override
            protected void updateItem(ParcelleProprietes parcelle, boolean empty) {
                super.updateItem(parcelle, empty);
                if (empty || parcelle == null) {
                    setGraphic(null);
                } else {
                    try {
                        // Chargement de l'image
                        File file = new File("src/main/resources/" + parcelle.getImage());
                        if (file.exists()) {
                            imageView.setImage(new Image(file.toURI().toString()));
                        }

                        // Mise à jour des informations
                        titre.setText(parcelle.getTitre());
                        prix.setText(parcelle.getPrix() + " DT");
                        statut.setText(parcelle.getStatus() + " : " + parcelle.getType_terrain());
                        taille.setText("Superficie: " + parcelle.getTaille() + " m²");

                        setGraphic(card);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setGraphic(new Label("Erreur de chargement"));
                    }
                }
            }
        });
    }

    private void openModificationWindow(ParcelleProprietes parcelle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Modifierparcelles.fxml"));
            Parent root = loader.load();

            Modifierparcellesback controller = loader.getController();
            controller.setParcelleToEdit(parcelle);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification");
        }
    }

    private void loadData() {
        List<ParcelleProprietes> parcelles = service.getAll();
        ObservableList<ParcelleProprietes> data = FXCollections.observableArrayList(parcelles);
        listView.setItems(data);
    }

    @FXML
    private void handleActualiser() {
        loadData();
    }

    @FXML
    private void handleRetour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Ajouterparcelles.fxml"));
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue d'ajout");
        }
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
