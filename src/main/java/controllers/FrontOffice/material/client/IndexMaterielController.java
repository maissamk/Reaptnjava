package controllers.FrontOffice.material.client;

import Models.MaterielLocation;
import Models.MaterielVente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import services.MaterielService;
import utils.SessionManager;


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
        // Base style
        String cardBaseStyle = "-fx-background-color: #f8f8f8; " +
                "-fx-padding: 15; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1); " +
                "-fx-cursor: hand;";
        card.setStyle(cardBaseStyle);
        card.setPrefWidth(300);

        // Hover styles
        String cardHoverStyle = "-fx-background-color: #f0f7f0; " +
                "-fx-effect: dropshadow(gaussian, rgba(56,142,60,0.2), 10, 0, 0, 2);";

        card.setOnMouseEntered(e -> card.setStyle(cardBaseStyle + cardHoverStyle));
        card.setOnMouseExited(e -> card.setStyle(cardBaseStyle));

        // Image with natural frame
        ImageView imageView = new ImageView();
        try {
            String imagePath = "file:src/main/resources/images_materiels/" + materiel.getImage();
            imageView.setImage(new Image(imagePath, 280, 150, false, true));
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.png"), 280, 150, false, true));
        }
        imageView.setFitWidth(280);
        imageView.setFitHeight(150);
        imageView.setStyle("-fx-border-radius: 4; " +
                "-fx-border-color: #c8e6c9; " +
                "-fx-border-width: 2;");

        // Details with nature-themed colors
        Label nameLabel = new Label(materiel.getNom());
        nameLabel.setStyle("-fx-font-size: 16; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #2e7d32;");

        Label priceLabel = new Label(String.format("%.2f TND", materiel.getPrix()));
        priceLabel.setStyle("-fx-text-fill: #1b5e20; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14;");

        Label statusLabel = new Label(materiel.isDisponibilite() ? "Disponible" : "Non disponible");
        statusLabel.setStyle("-fx-text-fill: " + (materiel.isDisponibilite() ? "#2e7d32" : "#c62828") + "; " +
                "-fx-font-weight: bold;");

        // Buttons with natural colors
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");

        Button showBtn = new Button("Détails");
        String buttonBaseStyle = "-fx-background-color: #388e3c; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;";
        String buttonHoverStyle = "-fx-background-color: #2e7d32;";

        showBtn.setStyle(buttonBaseStyle);
        showBtn.setOnMouseEntered(e -> showBtn.setStyle(buttonBaseStyle + buttonHoverStyle));
        showBtn.setOnMouseExited(e -> showBtn.setStyle(buttonBaseStyle));
        showBtn.setOnAction(e -> handleShowVente(materiel));

        buttonBox.getChildren().addAll(showBtn);
        card.getChildren().addAll(imageView, nameLabel, priceLabel, statusLabel, buttonBox);

        return card;
    }

    private VBox createLocationCard(MaterielLocation materiel) {
        VBox card = new VBox(10);
        // Base style
        String cardBaseStyle = "-fx-background-color: #f8f8f8; " +
                "-fx-padding: 15; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1); " +
                "-fx-cursor: hand;";
        card.setStyle(cardBaseStyle);
        card.setPrefWidth(300);

        // Hover styles
        String cardHoverStyle = "-fx-background-color: #f0f7f0; " +
                "-fx-effect: dropshadow(gaussian, rgba(56,142,60,0.2), 10, 0, 0, 2);";

        card.setOnMouseEntered(e -> card.setStyle(cardBaseStyle + cardHoverStyle));
        card.setOnMouseExited(e -> card.setStyle(cardBaseStyle));

        // Image with natural frame
        ImageView imageView = new ImageView();
        try {
            String imagePath = "file:src/main/resources/images_materiels/" + materiel.getImage();
            imageView.setImage(new Image(imagePath, 280, 150, false, true));
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.png"), 280, 150, false, true));
        }
        imageView.setFitWidth(280);
        imageView.setFitHeight(150);
        imageView.setStyle("-fx-border-radius: 4; " +
                "-fx-border-color: #c8e6c9; " +
                "-fx-border-width: 2;");

        // Details with nature-themed colors
        Label nameLabel = new Label(materiel.getNom());
        nameLabel.setStyle("-fx-font-size: 16; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #2e7d32;");

        Label priceLabel = new Label(String.format("%.2f TND/jour", materiel.getPrix()));
        priceLabel.setStyle("-fx-text-fill: #1b5e20; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14;");

        Label statusLabel = new Label(materiel.isDisponibilite() ? "Disponible" : "Non disponible");
        statusLabel.setStyle("-fx-text-fill: " + (materiel.isDisponibilite() ? "#2e7d32" : "#c62828") + "; " +
                "-fx-font-weight: bold;");

        // Buttons with natural colors
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");

        Button showBtn = new Button("Détails");
        String buttonBaseStyle = "-fx-background-color: #388e3c; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;";
        String buttonHoverStyle = "-fx-background-color: #2e7d32;";

        showBtn.setStyle(buttonBaseStyle);
        showBtn.setOnMouseEntered(e -> showBtn.setStyle(buttonBaseStyle + buttonHoverStyle));
        showBtn.setOnMouseExited(e -> showBtn.setStyle(buttonBaseStyle));
        showBtn.setOnAction(e -> handleShowLocation(materiel));

        buttonBox.getChildren().addAll(showBtn);
        card.getChildren().addAll(imageView, nameLabel, priceLabel, statusLabel, buttonBox);

        return card;
    }

    private void handleShowVente(MaterielVente materiel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/materials/client/ShowMaterielVente.fxml"));
            Parent root = loader.load();

            ShowMaterielVenteController controller = loader.getController();
            controller.setMateriel(materiel);

            Stage stage = new Stage();
            stage.setTitle("Détails du Matériel à Vendre");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {

            e.printStackTrace();        }
    }

    private void handleShowLocation(MaterielLocation materiel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/materials/client/ShowMaterielLocation.fxml"));
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
    } @FXML
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