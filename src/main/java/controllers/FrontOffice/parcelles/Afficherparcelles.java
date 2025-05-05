package controllers.FrontOffice.parcelles;

import controllers.FrontOffice.BaseFrontController;
import controllers.FrontOffice.Home;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Models.ParcelleProprietes;
import services.ParcelleProprietesService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Afficherparcelles {

    // Eléments UI
    @FXML private GridPane gridPane;
    @FXML private Button btnActualiser;
    @FXML private Button btnRetour;
    @FXML private Button btnEstimer;

    // Eléments pour la recherche
    @FXML private ComboBox<String> cbType;
    @FXML private TextField tfTitre;
    @FXML private TextField tfEmplacement;
    @FXML private TextField tfPrixMin;
    @FXML private TextField tfPrixMax;
    @FXML private ListView<String> suggestionsList;

    private final ParcelleProprietesService service = new ParcelleProprietesService();
    private List<ParcelleProprietes> allParcelles; // Cache des données
    private ScheduledExecutorService executorService;
    private StackPane mainContentPane;


    @FXML
    public void initialize() {
        initFiltres();
        loadData();
    }

    public void setMainContentPane(StackPane pane) {
        this.mainContentPane = pane;
    }

    /// ///new code estimer parclle
    @FXML
    private void handleEstimer() {
        try {
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent homeRoot = homeLoader.load();
            Home homeController = homeLoader.getController();

            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/EstimerParcelle.fxml"));
            Parent content = contentLoader.load();

            homeController.getMainContentPane().getChildren().setAll(content);

            Stage stage = (Stage) btnEstimer.getScene().getWindow();
            stage.setScene(new Scene(homeRoot));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'estimation : " + e.getMessage());
        }
    }
    private void initFiltres() {
        cbType.getItems().addAll("Agricole", "Résidentiel", "Commercial", "Mixte");

        tfPrixMin.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) tfPrixMin.setText(oldVal);
        });

        tfPrixMax.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) tfPrixMax.setText(oldVal);
        });

        tfEmplacement.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 2) scheduleAPICall(newVal);
            else suggestionsList.setVisible(false);
        });

        // Ajout du listener pour le champ de recherche par titre
        tfTitre.textProperty().addListener((obs, oldVal, newVal) -> {
            // Filtrage dynamique lorsque le titre change
            if (newVal.length() >= 1 && allParcelles != null) {
                handleDynamicFilter();
            }
        });

        suggestionsList.setOnMouseClicked(e -> {
            String selected = suggestionsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                tfEmplacement.setText(selected);
                suggestionsList.setVisible(false);
            }
        });
    }

    // Nouvelle méthode pour le filtrage dynamique
    private void handleDynamicFilter() {
        List<ParcelleProprietes> filtered = allParcelles.stream()
                .filter(this::filtreType)
                .filter(this::filtreTitre)
                .filter(this::filtreEmplacement)
                .filter(this::filtrePrix)
                .collect(Collectors.toList());

        updateGrid(filtered);
    }

    private void scheduleAPICall(String query) {
        if (executorService != null) executorService.shutdown();

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {
            List<String> results = OSMGeocoder.getLocationSuggestions(query);
            Platform.runLater(() -> {
                suggestionsList.setItems(FXCollections.observableArrayList(results));
                suggestionsList.setVisible(!results.isEmpty());
            });
        }, 500, TimeUnit.MILLISECONDS);
    }

    private void loadData() {
        gridPane.getChildren().clear();
        allParcelles = service.getAll(); // Important: on garde une copie pour le filtrage

        int column = 0;
        int row = 1;

        for (ParcelleProprietes parcelle : allParcelles) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/ParcelleCard.fxml"));
                VBox card = loader.load();
                ParcelleCard controller = loader.getController();
                controller.setData(parcelle);

                controller.getBtnSupprimer().setOnAction(e -> {
                    service.delete(parcelle);
                    loadData();
                });

                controller.getBtnModifier().setOnAction(e -> openModificationWindow(parcelle));

                // Ajout de l'action pour le bouton d'historique
                controller.getBtnHistorique().setOnAction(e -> openHistoriqueWindow(parcelle));

                gridPane.add(card, column, row);
                GridPane.setMargin(card, new Insets(10));

                column = (column + 1) % 3;
                if (column == 0) row++;
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors du chargement des cartes : " + e.getMessage());
            }
        }
    }

    // Nouvelle méthode pour ouvrir la fenêtre d'historique
    private void openHistoriqueWindow(ParcelleProprietes parcelle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/AfficherHistoriqueParcelle.fxml"));
            Parent root = loader.load();

            AfficherHistoriqueParcelleController controller = loader.getController();
            controller.setParcelle(parcelle);

            Stage stage = new Stage();
            stage.setTitle("Historique de location - " + parcelle.getTitre());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'historique de location : " + e.getMessage());
        }
    }

    private void updateGrid(List<ParcelleProprietes> parcelles) {
        gridPane.getChildren().clear();
        int column = 0;
        int row = 1;

        for (ParcelleProprietes parcelle : parcelles) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/ParcelleCard.fxml"));
                VBox card = loader.load();
                ParcelleCard controller = loader.getController();
                controller.setData(parcelle);

                controller.getBtnSupprimer().setOnAction(e -> {
                    service.delete(parcelle);
                    loadData();
                });

                controller.getBtnModifier().setOnAction(e -> openModificationWindow(parcelle));

                // Ajout de l'action pour le bouton d'historique dans les résultats filtrés
                controller.getBtnHistorique().setOnAction(e -> openHistoriqueWindow(parcelle));

                gridPane.add(card, column, row);
                GridPane.setMargin(card, new Insets(10));

                column = (column + 1) % 3;
                if (column == 0) row++;
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors du filtrage : " + e.getMessage());
            }
        }
    }

    private void openModificationWindow(ParcelleProprietes parcelle) {
        try {
            // Load Home.fxml which contains the navbar
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent homeRoot = homeLoader.load();
            Home homeController = homeLoader.getController();

            // Load the modification content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/Modifierparcelles.fxml"));
            Parent content = contentLoader.load();

            // Get the modification controller and set up the data
            Modifierparcelles controller = contentLoader.getController();
            controller.setParcelleToEdit(parcelle);
            controller.setRefreshCallback(this::loadData); // Refresh the data when done

            // Set the content in Home's content pane
            homeController.getMainContentPane().getChildren().setAll(content);

            // Create and show the stage
            Stage stage = new Stage();
            stage.setScene(new Scene(homeRoot));
            stage.setTitle("Modifier Parcelle");
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur : " + e.getMessage());
        }
    }

    @FXML
    private void handleFiltrer() {
        List<ParcelleProprietes> filtered = allParcelles.stream()
                .filter(this::filtreType)
                .filter(this::filtreTitre)
                .filter(this::filtreEmplacement)
                .filter(this::filtrePrix)
                .collect(Collectors.toList());

        updateGrid(filtered);
    }

    @FXML
    private void handleReinitialiser() {
        cbType.getSelectionModel().clearSelection();
        tfTitre.clear();
        tfEmplacement.clear();
        tfPrixMin.clear();
        tfPrixMax.clear();
        suggestionsList.setVisible(false);
        loadData();
    }

    private boolean filtreType(ParcelleProprietes p) {
        String type = cbType.getValue();
        return type == null || type.isEmpty() || p.getType_terrain().equalsIgnoreCase(type);
    }

    private boolean filtreTitre(ParcelleProprietes p) {
        String titre = tfTitre.getText().toLowerCase();
        return titre.isEmpty() || p.getTitre().toLowerCase().contains(titre);
    }

    private boolean filtreEmplacement(ParcelleProprietes p) {
        String emp = tfEmplacement.getText().toLowerCase();
        return emp.isEmpty() || p.getEmplacement().toLowerCase().contains(emp);
    }

    private boolean filtrePrix(ParcelleProprietes p) {
        try {
            double min = tfPrixMin.getText().isEmpty() ? 0 : Double.parseDouble(tfPrixMin.getText());
            double max = tfPrixMax.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(tfPrixMax.getText());
            return p.getPrix() >= min && p.getPrix() <= max;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    @FXML
    private void handleActualiser() {
        loadData();
    }

    @FXML
    private void handleRetour() {
        try {
            // Load Home.fxml (which contains the navbar)
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent homeRoot = homeLoader.load();
            Home homeController = homeLoader.getController();

            // Load the content you want to display
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/Ajouterparcelles.fxml"));
            Parent content = contentLoader.load();

            // Set the content in Home's content pane
            homeController.getMainContentPane().getChildren().setAll(content);

            // Update the stage
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(homeRoot));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de retourner à l'ajout : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}