package controllers.FrontOffice.parcelles;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Models.Contrat;
import Models.HistoriqueLocation;
import Models.ParcelleProprietes;
import services.HistoriqueLocationService;
import services.ParcelleProprietesService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AfficherHistoriqueParcelleController {

    @FXML
    private Label labelTitreParcelle;

    @FXML
    private Label labelEmplacement;

    @FXML
    private Label labelStatistiques;

    @FXML
    private TableView<HistoriqueLocation> tableHistorique;

    @FXML
    private TableColumn<HistoriqueLocation, String> colLocataire;

    @FXML
    private TableColumn<HistoriqueLocation, Date> colDateDebut;

    @FXML
    private TableColumn<HistoriqueLocation, Date> colDateFin;

    @FXML
    private TableColumn<HistoriqueLocation, Double> colPrix;

    @FXML
    private TableColumn<HistoriqueLocation, String> colStatut;

    @FXML
    private Button btnFermer;

    @FXML
    private VBox chartContainer;

    private ParcelleProprietes parcelle;
    private final HistoriqueLocationService historiqueService = new HistoriqueLocationService();
    private final ParcelleProprietesService parcelleService = new ParcelleProprietesService();

    public void setParcelle(ParcelleProprietes parcelle) {
        this.parcelle = parcelle;
        chargerDonnees();
    }

    public void setParcelle(int parcelleId) {
        // Récupérer la parcelle par son ID à partir du service
        List<ParcelleProprietes> parcelles = parcelleService.getAll();
        for (ParcelleProprietes p : parcelles) {
            if (p.getId() == parcelleId) {
                this.parcelle = p;
                break;
            }
        }

        if (this.parcelle != null) {
            chargerDonnees();
        } else {
            labelTitreParcelle.setText("Parcelle non trouvée (ID: " + parcelleId + ")");
        }
    }

    @FXML
    private void initialize() {
        // Configurer la table
        colLocataire.setCellValueFactory(data -> {
            HistoriqueLocation hl = data.getValue();
            String locataire = hl.getNom_locataire();

            // Si la location est liée à un contrat, afficher des informations supplémentaires
            if (hl.getContrat() != null) {
                Contrat contrat = hl.getContrat();
                return new SimpleStringProperty(locataire + " (Contrat #" + contrat.getId() + ")");
            }
            return new SimpleStringProperty(locataire);
        });

        colDateDebut.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDate_debut()));
        colDateDebut.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });

        colDateFin.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDate_fin()));
        colDateFin.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });

        colPrix.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrix_location()).asObject());
        colPrix.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f TND", item));
                }
            }
        });

        colStatut.setCellValueFactory(data -> {
            HistoriqueLocation hl = data.getValue();
            String statut = hl.getStatut();

            // Si la location est liée à un contrat, ajouter une indication
            if (hl.getContrat() != null) {
                Contrat contrat = hl.getContrat();
                // Utiliser un statut par défaut "louer" si le status du contrat n'est pas disponible
                String contratStatus = (contrat.getStatus() != null && !contrat.getStatus().isEmpty()) ?
                        contrat.getStatus() : "louer";
                return new SimpleStringProperty(statut + " (Contrat: " + contratStatus + ")");
            }
            return new SimpleStringProperty(statut);
        });

        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    // Appliquer le style en fonction du statut (basé sur le début de la chaîne)
                    if (item.startsWith("En cours")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (item.startsWith("Terminé")) {
                        setStyle("-fx-text-fill: gray;");
                    } else if (item.startsWith("Planifié")) {
                        setStyle("-fx-text-fill: blue;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Configurer le bouton Fermer
        btnFermer.setOnAction(e -> ((Stage) btnFermer.getScene().getWindow()).close());
    }

    private void chargerDonnees() {
        if (parcelle == null) return;

        // Mettre à jour les labels avec les informations de la parcelle
        labelTitreParcelle.setText(parcelle.getTitre());
        labelEmplacement.setText("Emplacement: " + parcelle.getEmplacement());

        // Synchroniser les contrats avec l'historique de location
        historiqueService.syncContratToHistorique(parcelle.getId());

        // Récupérer l'historique des locations
        List<HistoriqueLocation> historiques = historiqueService.getHistoriqueByParcelleId(parcelle.getId());

        // Mettre à jour la table
        ObservableList<HistoriqueLocation> data = FXCollections.observableArrayList(historiques);
        tableHistorique.setItems(data);

        // Mettre à jour les statistiques
        int nombreLocations = historiques.size();
        double totalRevenus = historiques.stream().mapToDouble(HistoriqueLocation::getPrix_location).sum();
        int locationEnCours = (int) historiques.stream().filter(h -> h.getStatut().startsWith("En cours")).count();
        int locationContrat = (int) historiques.stream().filter(h -> h.getContrat() != null).count();

        labelStatistiques.setText(String.format(
                "Nombre total de locations: %d | Revenus totaux: %.2f TND | Locations en cours: %d | Locations sous contrat: %d",
                nombreLocations, totalRevenus, locationEnCours, locationContrat
        ));

        // Créer le graphique
        creerGraphiqueLocations(historiques);
    }

    private void creerGraphiqueLocations(List<HistoriqueLocation> historiques) {
        // Vider le conteneur avant d'ajouter un nouveau graphique
        chartContainer.getChildren().clear();

        // Créer les axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Année");
        yAxis.setLabel("Nombre de locations");

        // Créer le graphique
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Historique des locations par année");
        barChart.setAnimated(false);

        // Préparer les séries pour le graphique
        XYChart.Series<String, Number> seriesTotal = new XYChart.Series<>();
        seriesTotal.setName("Nombre total de locations");

        XYChart.Series<String, Number> seriesContrat = new XYChart.Series<>();
        seriesContrat.setName("Locations sous contrat");

        // Groupe les locations par année
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

        // Compter toutes les locations par année
        historiques.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        h -> yearFormat.format(h.getDate_debut()),
                        java.util.stream.Collectors.counting()))
                .forEach((year, count) -> seriesTotal.getData().add(new XYChart.Data<>(year, count)));

        // Compter les locations sous contrat par année
        historiques.stream()
                .filter(h -> h.getContrat() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        h -> yearFormat.format(h.getDate_debut()),
                        java.util.stream.Collectors.counting()))
                .forEach((year, count) -> seriesContrat.getData().add(new XYChart.Data<>(year, count)));

        barChart.getData().add(seriesTotal);
        barChart.getData().add(seriesContrat);

        // Ajouter le graphique au conteneur
        chartContainer.getChildren().add(barChart);
    }
}