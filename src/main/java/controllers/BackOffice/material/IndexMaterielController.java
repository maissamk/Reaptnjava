package controllers.BackOffice.material;

import Models.MaterielLocation;
import Models.MaterielVente;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import services.MaterielService;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    @FXML private Button addVenteBtn;
    @FXML private BarChart<String, Number> availabilityChart;
    @FXML private BarChart<String, Number> priceChart;
    @FXML private Label totalMaterialsLabel;
    @FXML private Label availableMaterialsLabel;
    @FXML private Label averagePriceLabel;
    // Composants FXML pour la location
    @FXML private GridPane locationGridContainer;
    @FXML private Pagination locationPagination;
    @FXML private TextField searchLocationField;
    @FXML private Button addLocationBtn;

    @FXML
    public void initialize() {
        loadVenteData();
        loadLocationData();
        addVenteBtn.setOnAction(this::goToAddVente);
        addLocationBtn.setOnAction(this::goToAddLocation);

        // Setup dynamic search listeners
        searchVenteField.textProperty().addListener((obs, oldVal, newVal) -> handleSearchVente());
        searchLocationField.textProperty().addListener((obs, oldVal, newVal) -> handleSearchLocation());

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
    @FXML
    public void loadStatistics() {
        if (availabilityChart == null) return;

        availabilityChart.getData().clear();
        priceChart.getData().clear();

        List<MaterielVente> venteItems = materielService.findAllVente();
        List<MaterielLocation> locationItems = materielService.findAllLocation();

        int totalVente = venteItems.size();
        int totalLocation = locationItems.size();
        int availableVente = (int) venteItems.stream().filter(MaterielVente::isDisponibilite).count();
        int availableLocation = (int) locationItems.stream().filter(MaterielLocation::isDisponibilite).count();

        // Update summary cards
        totalMaterialsLabel.setText(String.valueOf(totalVente + totalLocation));
        availableMaterialsLabel.setText(String.valueOf(availableVente + availableLocation));

        double avgPriceVente = venteItems.stream().mapToDouble(MaterielVente::getPrix).average().orElse(0);
        double avgPriceLocation = locationItems.stream().mapToDouble(MaterielLocation::getPrix).average().orElse(0);
        averagePriceLabel.setText(String.format("%.2f TND", (avgPriceVente + avgPriceLocation)/2));

        // Availability Chart Data
        XYChart.Series<String, Number> availabilitySeries = new XYChart.Series<>();
        availabilitySeries.getData().add(new XYChart.Data<>("Vente - Disponible", availableVente));
        availabilitySeries.getData().add(new XYChart.Data<>("Vente - Indisponible", totalVente - availableVente));
        availabilitySeries.getData().add(new XYChart.Data<>("Location - Disponible", availableLocation));
        availabilitySeries.getData().add(new XYChart.Data<>("Location - Indisponible", totalLocation - availableLocation));
        availabilityChart.getData().add(availabilitySeries);

        // Price Distribution Chart Data
        XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
        priceSeries.setName("Répartition des Prix");

        priceSeries.getData().add(new XYChart.Data<>("0-100 TND",
                venteItems.stream().filter(m -> m.getPrix() <= 100).count() +
                        locationItems.stream().filter(m -> m.getPrix() <= 100).count()
        ));
        priceSeries.getData().add(new XYChart.Data<>("100-500 TND",
                venteItems.stream().filter(m -> m.getPrix() > 100 && m.getPrix() <= 500).count() +
                        locationItems.stream().filter(m -> m.getPrix() > 100 && m.getPrix() <= 500).count()
        ));
        priceSeries.getData().add(new XYChart.Data<>("500+ TND",
                venteItems.stream().filter(m -> m.getPrix() > 500).count() +
                        locationItems.stream().filter(m -> m.getPrix() > 500).count()
        ));

        priceChart.getData().add(priceSeries);

        styleCharts();
    }

    private void styleCharts() {
        // Style availability chart
        availabilityChart.setStyle("-fx-font-family: 'Segoe UI';");
        availabilityChart.getXAxis().setStyle("-fx-font-size: 12;");
        availabilityChart.getYAxis().setStyle("-fx-font-size: 12;");

        // Style price chart
        priceChart.setStyle("-fx-font-family: 'Segoe UI';");
        priceChart.getXAxis().setStyle("-fx-font-size: 12;");
        priceChart.getYAxis().setStyle("-fx-font-size: 12;");



        int colorIndex = 0;
        for (XYChart.Series<String, Number> series : availabilityChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                 }
                colorIndex++;
            }
        }



        colorIndex = 0;
        for (XYChart.Series<String, Number> series : priceChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                 }
                colorIndex++;
            }
        }
    }
    @FXML
    private void showStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/materials/StatisticsView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Statistiques des Matériels");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les statistiques", Alert.AlertType.ERROR);
        }
    }
    private void handleEditVente(MaterielVente materiel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/materials/EditMaterielVente.fxml"));
            Parent root = loader.load();

            EditMaterielVenteController controller = loader.getController();
            controller.setMaterielToEdit(materiel);

            Stage stage = new Stage();
            stage.setTitle("Modifier Matériel à Vendre");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadVenteData();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", Alert.AlertType.ERROR);
        }
    }

    private void handleEditLocation(MaterielLocation materiel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/materials/EditMaterielLocation.fxml"));
            Parent root = loader.load();

            EditMaterielLocationController controller = loader.getController();
            controller.setMaterielToEdit(materiel);

            Stage stage = new Stage();
            stage.setTitle("Modifier Matériel à Louer");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadLocationData();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", Alert.AlertType.ERROR);
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

        // QR Code
        ImageView qrCodeView = generateQRCodeForMaterial(materiel);
        qrCodeView.setFitWidth(80);
        qrCodeView.setFitHeight(80);
        qrCodeView.setStyle("-fx-border-color: #ddd; -fx-border-width: 1px;");

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

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");
        editBtn.setOnAction(e -> handleEditVente(materiel));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> handleDeleteVente(materiel));

        buttonBox.getChildren().addAll(showBtn, editBtn, deleteBtn);

        // Add QR code to the card
        HBox infoBox = new HBox(10);
        infoBox.getChildren().addAll(qrCodeView, new VBox(5, nameLabel, priceLabel, statusLabel));

        card.getChildren().addAll(imageView, infoBox, buttonBox);

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

        // QR Code
        ImageView qrCodeView = generateQRCodeForMaterial(materiel);
        qrCodeView.setFitWidth(80);
        qrCodeView.setFitHeight(80);
        qrCodeView.setStyle("-fx-border-color: #ddd; -fx-border-width: 1px;");

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

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");
        editBtn.setOnAction(e -> handleEditLocation(materiel));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> handleDeleteLocation(materiel));

        buttonBox.getChildren().addAll(showBtn, editBtn, deleteBtn);

        // Add QR code to the card
        HBox infoBox = new HBox(10);
        infoBox.getChildren().addAll(qrCodeView, new VBox(5, nameLabel, priceLabel, statusLabel));

        card.getChildren().addAll(imageView, infoBox, buttonBox);

        return card;
    }

    private ImageView generateQRCodeForMaterial(Object materiel) {
        try {
            String qrContent;
            if (materiel instanceof MaterielVente) {
                MaterielVente mv = (MaterielVente) materiel;
                qrContent = String.format("Matériel Vente\nNom: %s\nPrix: %.2f TND\nDisponible: %s\nDescription: %s",
                        mv.getNom(), mv.getPrix(), mv.isDisponibilite() ? "Oui" : "Non", mv.getDescription());
            } else if (materiel instanceof MaterielLocation) {
                MaterielLocation ml = (MaterielLocation) materiel;
                qrContent = String.format("Matériel Location\nNom: %s\nPrix: %.2f TND/jour\nDisponible: %s\nDescription: %s",
                        ml.getNom(), ml.getPrix(), ml.isDisponibilite() ? "Oui" : "Non", ml.getDescription());
            } else {
                return new ImageView();
            }

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            return new ImageView(new Image(new ByteArrayInputStream(pngData)));
        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            return new ImageView();
        }
    }

    @FXML
    private void handleSearchVente() {
        String query = searchVenteField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            loadVenteData();
            return;
        }

        List<MaterielVente> filtered = materielService.findAllVente().stream()
                .filter(m -> m.getNom().toLowerCase().contains(query) ||
                        m.getDescription().toLowerCase().contains(query) ||
                        String.valueOf(m.getPrix()).contains(query))
                .toList();

        venteList.clear();
        venteList.addAll(filtered);
        currentVentePage = 0;
        refreshVenteList();
    }

    @FXML
    private void handleSearchLocation() {
        String query = searchLocationField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            loadLocationData();
            return;
        }

        List<MaterielLocation> filtered = materielService.findAllLocation().stream()
                .filter(m -> m.getNom().toLowerCase().contains(query) ||
                        m.getDescription().toLowerCase().contains(query) ||
                        String.valueOf(m.getPrix()).contains(query))
                .toList();

        locationList.clear();
        locationList.addAll(filtered);
        currentLocationPage = 0;
        refreshLocationList();
    }

    private void handleShowVente(MaterielVente materiel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/materials/ShowMaterielVente.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/materials/ShowMaterielLocation.fxml"));
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

    private void handleDeleteVente(MaterielVente materiel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce matériel?");
        alert.setContentText("Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                materielService.supprimer(materiel.getId(), "vente");
                loadVenteData();
            }
        });
    }

    private void handleDeleteLocation(MaterielLocation materiel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce matériel?");
        alert.setContentText("Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                materielService.supprimer(materiel.getId(), "location");
                loadLocationData();
            }
        });
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

    @FXML
    private void goToAddVente(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/materials/AddMaterielVente.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter Matériel à Vendre");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadVenteData();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToAddLocation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/materials/AddMaterielLocation.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter Matériel à Louer");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadLocationData();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout", Alert.AlertType.ERROR);
        }
    }
    @FXML
    void gotohisotrique(ActionEvent event) {
        try {
            // Get the current stage

            // Load new window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/materials/HistoriqueController.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Gestion des Catégories");
            newStage.setScene(new Scene(root));

            // Close current window
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();

            // Set stage width and height to screen dimensions
            newStage.setWidth(screenBounds.getWidth());
            newStage.setHeight(screenBounds.getHeight());


            // Show new window
            newStage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la gestion des catégories", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    void gotocategorie(ActionEvent event) {
        try {
            // Get the current stage
            Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();

            // Load new window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/category/IndexCategorie.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Gestion des Catégories");
            newStage.setScene(new Scene(root));

            // Close current window
            currentStage.close();
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();

            // Set stage width and height to screen dimensions
            newStage.setWidth(screenBounds.getWidth());
            newStage.setHeight(screenBounds.getHeight());


            // Show new window
            newStage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la gestion des catégories", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void goToEditVente(MaterielVente materiel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/materials/EditMaterielVente.fxml"));
            Parent root = loader.load();

          EditMaterielVenteController controller = loader.getController();
            controller.setMaterielToEdit(materiel);

            Stage stage = new Stage();
            stage.setTitle("Modifier Matériel à Vendre");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadVenteData();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification", Alert.AlertType.ERROR);
        }
    }

    private void goToEditLocation(MaterielLocation materiel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/materials/EditMaterielLocation.fxml"));
            Parent root = loader.load();

            EditMaterielLocationController controller = loader.getController();
            controller.setMaterielToEdit(materiel);

            Stage stage = new Stage();
            stage.setTitle("Modifier Matériel à Louer");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadLocationData();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification", Alert.AlertType.ERROR);
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