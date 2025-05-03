package controllers.BackOffice.GestionCommandeBack;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.stage.Stage;
import Models.gestionCommande.CommandeDetails;
import services.gestionCommande.CommandeService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.StringConverter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CommandesAvecDetailsController {

    @FXML private TableColumn<CommandeDetails, Void> colAction;
    @FXML private TableView<CommandeDetails> tableView;
    @FXML private TableColumn<CommandeDetails, Integer> colQuantite;
    @FXML private TableColumn<CommandeDetails, Float> colTotal;
    @FXML private TableColumn<CommandeDetails, java.util.Date> colDateCommande;
    @FXML private TableColumn<CommandeDetails, String> colAdresse;
    @FXML private TableColumn<CommandeDetails, String> colStatut;
    @FXML private TableColumn<CommandeDetails, java.util.Date> colDateLivraison;
    @FXML private TableColumn<CommandeDetails, String> colPaiement;
    @FXML private TableColumn<CommandeDetails, java.util.Date> colDatePaiement;

    // Nouveaux éléments pour le tableau de bord
    @FXML private Label lblCommandesEnCours;
    @FXML private Label lblCommandesLivrees;
    @FXML private Label lblTotalVentes;
    @FXML private PieChart pieChartStatuts;
    @FXML private LineChart<String, Number> lineChartCommandes;

    private List<CommandeDetails> allCommandes;
    private CommandeService service;

    @FXML
    public void initialize() {
        service = new CommandeService();

        // Initialiser les colonnes du tableau
        initializeTableColumns();

        // Charger toutes les commandes
        allCommandes = service.getCommandesAvecDetails();

        // Charger et filtrer les données (commandes non livrées) pour le tableau
        loadTableData();

        // Initialiser les statistiques du tableau de bord
        initializeDashboard();
    }

    private void initializeTableColumns() {
        // Colonnes de base
        colQuantite.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getCommande().getQuantite()).asObject());
        colTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleFloatProperty(data.getValue().getCommande().getTotale()).asObject());
        colDateCommande.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCommande().getDate_commande()));

        colAdresse.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getLivraison() != null ? data.getValue().getLivraison().getAdresse() : "N/A"));

        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "Validation en cours", "En préparation", "Expédiée", "Livrée","Non Livrée"
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
                        service.updateStatutLivraison(commandeDetails.getLivraison());

                        if ("Livrée".equalsIgnoreCase(newStatus)) {
                            tableView.getItems().remove(commandeDetails);
                            Alert info = new Alert(Alert.AlertType.INFORMATION);
                            info.setTitle("Commande archivée");
                            info.setHeaderText(null);
                            info.setContentText("La commande a été livrée et archivée avec succès !");
                            info.showAndWait();

                            // Recharger les données et mettre à jour le tableau de bord
                            refreshData();
                        } else {
                            tableView.refresh();
                            // Mettre à jour le tableau de bord sans recharger toutes les données
                            updateDashboard();
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
    }

    private void loadTableData() {
        ObservableList<CommandeDetails> commandes = FXCollections.observableArrayList();

        for (CommandeDetails cmd : allCommandes) {
            if (cmd.getLivraison() == null || !"Livrée".equalsIgnoreCase(cmd.getLivraison().getStatus())) {
                commandes.add(cmd);
            }
        }

        commandes.sort((a, b) -> b.getCommande().getDate_commande().compareTo(a.getCommande().getDate_commande()));
        tableView.setItems(commandes);
    }

    private void initializeDashboard() {
        updateStatisticsLabels();
        updatePieChart();
        updateLineChart();
    }

    private void updateStatisticsLabels() {
        // Nombre de commandes en cours
        long nbCommandesEnCours = allCommandes.stream()
                .filter(cmd -> cmd.getLivraison() == null || !"Livrée".equalsIgnoreCase(cmd.getLivraison().getStatus()))
                .count();
        lblCommandesEnCours.setText(String.valueOf(nbCommandesEnCours));

        // Nombre de commandes livrées
        long nbCommandesLivrees = allCommandes.stream()
                .filter(cmd -> cmd.getLivraison() != null && "Livrée".equalsIgnoreCase(cmd.getLivraison().getStatus()))
                .count();
        lblCommandesLivrees.setText(String.valueOf(nbCommandesLivrees));

        // Calcul du chiffre d'affaires total
        float totalVentes = (float) allCommandes.stream()
                .mapToDouble(cmd -> cmd.getCommande().getTotale())
                .sum();
        lblTotalVentes.setText(String.format("%.2f DT", totalVentes));
    }

    private void updatePieChart() {
        // Regrouper les commandes par statut
        Map<String, Long> statutsCount = allCommandes.stream()
                .collect(Collectors.groupingBy(
                        cmd -> cmd.getLivraison() != null ? cmd.getLivraison().getStatus() : "Non Livrée",
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Long> entry : statutsCount.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        pieChartStatuts.setData(pieChartData);
        pieChartStatuts.setTitle("Répartition des commandes par statut");
    }

    private void updateLineChart() {
        // Préparation des données pour le graphique d'évolution
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy");
        Map<String, Integer> commandesByMonth = new TreeMap<>();

        // Regrouper les commandes par mois
        for (CommandeDetails cmd : allCommandes) {
            Date dateCommande = cmd.getCommande().getDate_commande();
            String monthYear = monthFormat.format(dateCommande);

            commandesByMonth.put(monthYear, commandesByMonth.getOrDefault(monthYear, 0) + 1);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de commandes");

        for (Map.Entry<String, Integer> entry : commandesByMonth.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        lineChartCommandes.getData().clear();
        lineChartCommandes.getData().add(series);
        lineChartCommandes.setTitle("Évolution des commandes");
    }

    @FXML
    public void refreshTable() {
        refreshData();
    }

    private void refreshData() {
        // Recharger toutes les commandes
        allCommandes = service.getCommandesAvecDetails();

        // Mettre à jour le tableau
        loadTableData();

        // Mettre à jour le tableau de bord
        updateDashboard();
    }

    private void updateDashboard() {
        updateStatisticsLabels();
        updatePieChart();
        updateLineChart();
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