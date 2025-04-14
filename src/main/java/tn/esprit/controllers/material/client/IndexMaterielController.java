package tn.esprit.controllers.material.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.controllers.material.EditMaterielLocationController;
import tn.esprit.controllers.material.EditMaterielVenteController;
import tn.esprit.controllers.material.ShowMaterielLocationController;
import tn.esprit.controllers.material.ShowMaterielVenteController;
import tn.esprit.entities.MaterielLocation;
import tn.esprit.entities.MaterielVente;
import tn.esprit.services.MaterielService;

import java.io.IOException;
import java.util.List;

public class IndexMaterielController {

    private final MaterielService materielService = new MaterielService();

    // Pour les matériels de vente
    private final ObservableList<MaterielVente> venteList = FXCollections.observableArrayList();
    private int currentVentePage = 0;

    // Pour les matériels de location
    private final ObservableList<MaterielLocation> locationList = FXCollections.observableArrayList();
    private int currentLocationPage = 0;

    private static final int ITEMS_PER_PAGE = 6;

    // Composants FXML pour la vente
    @FXML private GridPane venteGridContainer;
    @FXML private Pagination ventePagination;
    @FXML private TextField searchVenteField;

    // Composants FXML pour la location
    @FXML private GridPane locationGridContainer;
    @FXML private Pagination locationPagination;
    @FXML private TextField searchLocationField;

    @FXML
    public void initialize() {
        loadVenteData();
        loadLocationData();

        setupPaginationListeners();
    }

    private void loadVenteData() {
        List<MaterielVente> materiels = materielService.findAllVente();
        venteList.clear();
        venteList.addAll(materiels);
        refreshVenteList();
    }

    private void loadLocationData() {
        List<MaterielLocation> materiels = materielService.findAllLocation();
        locationList.clear();
        locationList.addAll(materiels);
        refreshLocationList();
    }

    private void refreshVenteList() {
        int totalPages = (int) Math.ceil((double) venteList.size() / ITEMS_PER_PAGE);
        ventePagination.setPageCount(totalPages == 0 ? 1 : totalPages);
        updateVenteList(currentVentePage);
    }

    private void refreshLocationList() {
        int totalPages = (int) Math.ceil((double) locationList.size() / ITEMS_PER_PAGE);
        locationPagination.setPageCount(totalPages == 0 ? 1 : totalPages);
        updateLocationList(currentLocationPage);
    }

    private void updateVenteList(int page) {
        venteGridContainer.getChildren().clear();

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, venteList.size());
        List<MaterielVente> pageMateriels = venteList.subList(start, end);

        int row = 0;
        int col = 0;

        for (MaterielVente materiel : pageMateriels) {
            VBox card = createVenteCard(materiel);
            venteGridContainer.add(card, col, row);

            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }

    private void updateLocationList(int page) {
        locationGridContainer.getChildren().clear();

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, locationList.size());
        List<MaterielLocation> pageMateriels = locationList.subList(start, end);

        int row = 0;
        int col = 0;

        for (MaterielLocation materiel : pageMateriels) {
            VBox card = createLocationCard(materiel);
            locationGridContainer.add(card, col, row);

            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }


    private VBox createVenteCard(MaterielVente materiel) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 5; -fx-border-color: #ddd;");
        card.setPrefWidth(300);

        // Image
        ImageView imageView = new ImageView();
        try {
            String imagePath = "file:src/main/resources/images_materiels/" + materiel.getImage();
            imageView.setImage(new Image(imagePath, 280, 150, false, true));
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.png"), 280, 150, false, true));
        }
        imageView.setFitWidth(280);
        imageView.setFitHeight(150);

        // Details
        Label nameLabel = new Label(materiel.getNom());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label priceLabel = new Label(String.format("%.2f TND", materiel.getPrix()));
        priceLabel.setStyle("-fx-text-fill: #4a6baf; -fx-font-weight: bold;");

        Label statusLabel = new Label(materiel.isDisponibilite() ? "Disponible" : "Non disponible");
        statusLabel.setStyle("-fx-text-fill: " + (materiel.isDisponibilite() ? "#4CAF50" : "#F44336") + ";");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");

        Button showBtn = new Button("Détails");
        showBtn.setStyle("-fx-background-color: #4a6baf; -fx-text-fill: white;");
        showBtn.setOnAction(e -> handleShowVente(materiel));



        buttonBox.getChildren().addAll(showBtn);
        card.getChildren().addAll(imageView, nameLabel, priceLabel, statusLabel, buttonBox);

        return card;
    }

    private VBox createLocationCard(MaterielLocation materiel) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 5; -fx-border-color: #ddd;");
        card.setPrefWidth(300);

        // Image
        ImageView imageView = new ImageView();
        try {
            String imagePath = "file:src/main/resources/images_materiels/" + materiel.getImage();
            imageView.setImage(new Image(imagePath, 280, 150, false, true));
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.png"), 280, 150, false, true));
        }
        imageView.setFitWidth(280);
        imageView.setFitHeight(150);

        // Details
        Label nameLabel = new Label(materiel.getNom());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label priceLabel = new Label(String.format("%.2f TND/jour", materiel.getPrix()));
        priceLabel.setStyle("-fx-text-fill: #4a6baf; -fx-font-weight: bold;");

        Label statusLabel = new Label(materiel.isDisponibilite() ? "Disponible" : "Non disponible");
        statusLabel.setStyle("-fx-text-fill: " + (materiel.isDisponibilite() ? "#4CAF50" : "#F44336") + ";");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");

        Button showBtn = new Button("Détails");
        showBtn.setStyle("-fx-background-color: #4a6baf; -fx-text-fill: white;");
        showBtn.setOnAction(e -> handleShowLocation(materiel));



        buttonBox.getChildren().addAll(showBtn);
        card.getChildren().addAll(imageView, nameLabel, priceLabel, statusLabel, buttonBox);

        return card;
    }

    private void handleShowVente(MaterielVente materiel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/materials/ShowMaterielVente.fxml"));
            Parent root = loader.load();

            ShowMaterielVenteController controller = loader.getController();
            controller.setMateriel(materiel);

            Stage stage = new Stage();
            stage.setTitle("Détails du Matériel à Vendre");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'afficher les détails", Alert.AlertType.ERROR);
        }
    }

    private void handleShowLocation(MaterielLocation materiel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/materials/ShowMaterielLocation.fxml"));
            Parent root = loader.load();

            ShowMaterielLocationController controller = loader.getController();
            controller.setMateriel(materiel);

            Stage stage = new Stage();
            stage.setTitle("Détails du Matériel à Louer");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'afficher les détails", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSearchVente() {
        String query = searchVenteField.getText().trim();
        List<MaterielVente> filtered = materielService.searchVente(query);
        venteList.clear();
        venteList.addAll(filtered);
        currentVentePage = 0;
        refreshVenteList();
    }

    @FXML
    private void handleSearchLocation() {
        String query = searchLocationField.getText().trim();
        List<MaterielLocation> filtered = materielService.searchLocation(query);
        locationList.clear();
        locationList.addAll(filtered);
        currentLocationPage = 0;
        refreshLocationList();
    }

    private void setupPaginationListeners() {
        ventePagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            currentVentePage = newValue.intValue();
            updateVenteList(currentVentePage);
        });

        locationPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            currentLocationPage = newValue.intValue();
            updateLocationList(currentLocationPage);
        });
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}