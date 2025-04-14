package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Contrat;
import services.ContratService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class Affichercontrat {

    @FXML private ListView<Contrat> listView;
    @FXML private Button btnActualiser;
    @FXML private Button btnRetour;

    private final ContratService service = new ContratService();

    @FXML
    public void initialize() {
        configureListView();
        loadData();
    }

    private void configureListView() {
        listView.setCellFactory(param -> new ListCell<>() {
            private final Label acheteur = new Label();
            private final Label vendeur = new Label();
            private final Label dateDebut = new Label();
            private final Label dateFin = new Label();
            private final Label statut = new Label();
            private final HBox actions = new HBox();
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");

            private final VBox card = new VBox();

            {
                acheteur.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                vendeur.setStyle("-fx-font-size: 14px;");
                dateDebut.setStyle("-fx-font-style: italic;");
                statut.setStyle("-fx-text-fill: #2ecc71;");

                actions.setSpacing(10);
                actions.getChildren().addAll(btnModifier, btnSupprimer);

                card.setSpacing(10);
                card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15px; -fx-border-color: #ddd; -fx-border-radius: 5px;");
                card.setPrefWidth(400);
                card.getChildren().addAll(acheteur, vendeur, dateDebut, dateFin, statut, actions);

                btnModifier.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                btnModifier.setOnAction(event -> {
                    Contrat contrat = getItem();
                    openModificationWindow(contrat);
                });

                btnSupprimer.setOnAction(event -> {
                    Contrat contrat = getItem();
                    service.delete(contrat);
                    loadData();
                });
            }

            @Override
            protected void updateItem(Contrat contrat, boolean empty) {
                super.updateItem(contrat, empty);
                if (empty || contrat == null) {
                    setGraphic(null);
                } else {
                    // Formater les deux dates
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    dateDebut.setText("Début : " + sdf.format(contrat.getDate_debut_contrat()));
                    dateFin.setText("Fin : " + sdf.format(contrat.getDatefin_contrat()));
                    statut.setText("Info : " + contrat.getInformation_contrat());
                    setGraphic(card);
                }
            }
        });
    }

    private void openModificationWindow(Contrat contrat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Modifiercontrat.fxml"));
            Parent root = loader.load();

            Modifiercontrat controller = loader.getController();
            controller.initData(contrat); // Initialisation des données

            Stage stage = new Stage();
            stage.setTitle("Modifier le Contrat");
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Attend la fermeture de la fenêtre

            loadData(); // Rafraîchit la liste après modification

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur : " + e.getMessage());
        }
    }

    private void loadData() {
        List<Contrat> contrats = service.getAll();
        ObservableList<Contrat> data = FXCollections.observableArrayList(contrats);
        listView.setItems(data);
    }
//    catch (
//    SQLException e) {
//        showAlert("Erreur", "Impossible de charger les données : " + e.getMessage());
//    }

    @FXML
    private void handleActualiser() {
        loadData();
    }

    @FXML
    private void handleRetour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Ajoutercontrat.fxml"));
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
