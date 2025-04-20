package controllers.BackOffice.GestionCommandeBack;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.stage.Stage;
import models.gestionCommande.CommandeDetails;
import services.gestionCommande.CommandeService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;

public class CommandesAvecDetailsController {

    @FXML private TableColumn<CommandeDetails, Void> colAction;

    @FXML private TableView<CommandeDetails> tableView;
    @FXML private TableColumn<CommandeDetails, Integer> colIdCommande;
    @FXML private TableColumn<CommandeDetails, Integer> colQuantite;
    @FXML private TableColumn<CommandeDetails, Float> colTotal;
    @FXML private TableColumn<CommandeDetails, java.util.Date> colDateCommande;
    @FXML private TableColumn<CommandeDetails, String> colAdresse;
    @FXML private TableColumn<CommandeDetails, String> colStatut;
    @FXML private TableColumn<CommandeDetails, java.util.Date> colDateLivraison;
    @FXML private TableColumn<CommandeDetails, String> colPaiement;
    @FXML private TableColumn<CommandeDetails, java.util.Date> colDatePaiement;

    @FXML
    public void initialize() {
        // Colonnes de base
        colIdCommande.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getCommande().getId()).asObject());
        colQuantite.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getCommande().getQuantite()).asObject());
        colTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleFloatProperty(data.getValue().getCommande().getTotale()).asObject());
        colDateCommande.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCommande().getDate_commande()));

        colAdresse.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getLivraison() != null ? data.getValue().getLivraison().getAdresse() : "N/A"));

        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "Validation en cours", "En préparation", "Expédiée", "Livrée"
        );

        colStatut.setCellValueFactory(data -> {
            if (data.getValue().getLivraison() != null) {
                return new javafx.beans.property.SimpleStringProperty(data.getValue().getLivraison().getStatus());
            } else {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        colStatut.setCellFactory(column -> {
            ComboBoxTableCell<CommandeDetails, String> cell = new ComboBoxTableCell<>(statusOptions);
            cell.setConverter(new StringConverter<>() {
                @Override public String toString(String object) { return object; }
                @Override public String fromString(String string) { return string; }
            });
            return cell;
        });

        colStatut.setOnEditCommit(event -> {
            CommandeDetails commandeDetails = event.getRowValue();
            String newStatus = event.getNewValue();
            String oldStatus = event.getOldValue();

            if (commandeDetails.getLivraison() != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation de mise à jour");
                alert.setHeaderText("Voulez-vous vraiment modifier le statut ?");
                alert.setContentText("Ancien statut : " + oldStatus + "\nNouveau statut : " + newStatus);

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        commandeDetails.getLivraison().setStatus(newStatus);
                        CommandeService service = new CommandeService();
                        service.updateStatutLivraison(commandeDetails.getLivraison());

                        if ("Livrée".equalsIgnoreCase(newStatus)) {
                            tableView.getItems().remove(commandeDetails);
                            Alert info = new Alert(Alert.AlertType.INFORMATION);
                            info.setTitle("Commande archivée");
                            info.setHeaderText(null);
                            info.setContentText("La commande a été livrée et archivée avec succès !");
                            info.showAndWait();
                        } else {
                            tableView.refresh();
                        }
                    } else {
                        tableView.refresh();
                    }
                });
            }
        });

        colDateLivraison.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(
                data.getValue().getLivraison() != null ? data.getValue().getLivraison().getDateLiv() : null));

        colPaiement.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPaiement() != null ? data.getValue().getPaiement().getMethodePaiement() : "N/A"));

        colDatePaiement.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(
                data.getValue().getPaiement() != null ? data.getValue().getPaiement().getDatePaiement() : null));

        tableView.setEditable(true);

        // Charger et filtrer les données (commandes non livrées)
        CommandeService service = new CommandeService();
        List<CommandeDetails> listeCommandes = service.getCommandesAvecDetails();
        ObservableList<CommandeDetails> commandes = FXCollections.observableArrayList();

        for (CommandeDetails cmd : listeCommandes) {
            if (cmd.getLivraison() == null || !"Livrée".equalsIgnoreCase(cmd.getLivraison().getStatus())) {
                commandes.add(cmd);
            }
        }

        commandes.sort((a, b) -> b.getCommande().getDate_commande().compareTo(a.getCommande().getDate_commande()));
        tableView.setItems(commandes);
    }

    @FXML
    private void ouvrirArchives() {
        try {
            // Charger la vue des archives
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/GestionCommandeBack/ArchivesView.fxml"));
            Parent archivesRoot = loader.load();

            // Récupérer la scène actuelle
            Scene currentScene = tableView.getScene();

            // Si vous voulez conserver la scène principale en mémoire
            Parent mainRoot = currentScene.getRoot();

            // Créer une nouvelle scène avec la vue des archives
            Scene archivesScene = new Scene(archivesRoot);

            // Récupérer la fenêtre actuelle et y attacher la nouvelle scène
            Stage currentStage = (Stage) currentScene.getWindow();
            currentStage.setScene(archivesScene);
            currentStage.setTitle("Commandes Archivées");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue des archives: " + e.getMessage());
        }
    }
}
