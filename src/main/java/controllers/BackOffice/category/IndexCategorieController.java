package controllers.BackOffice.category;

import models.Categorie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import services.CategorieService;


import java.io.IOException;
import java.util.List;

public class IndexCategorieController {
    private final CategorieService categorieService = new CategorieService();
    private final ObservableList<Categorie> categorieList = FXCollections.observableArrayList();
    private static final int ITEMS_PER_PAGE = 6;

    @FXML private GridPane categoriesGrid;
    @FXML private Pagination pagination;
    @FXML private TextField searchField;
    @FXML private Button addBtn;

    @FXML
    public void initialize() {
        loadData();
        setupEventHandlers();
    }

    private void loadData() {
        List<Categorie> categories = categorieService.findAll();
        categorieList.setAll(categories);
        updatePagination();
        displayPage(0);
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) categorieList.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            displayPage(newVal.intValue());
        });
    }

    private void displayPage(int page) {
        categoriesGrid.getChildren().clear();

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, categorieList.size());

        int row = 0;
        int col = 0;

        for (int i = start; i < end; i++) {
            VBox card = createCategoryCard(categorieList.get(i));
            categoriesGrid.add(card, col, row);

            if (++col >= 3) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createCategoryCard(Categorie categorie) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 5; -fx-border-color: #ddd;");
        card.setPrefWidth(300);

        // Header with category name
        Label nameLabel = new Label(categorie.getNom());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Description (limited to 100 chars)
        String shortDesc = categorie.getDescription().length() > 100
                ? categorie.getDescription().substring(0, 100) + "..."
                : categorie.getDescription();
        Label descLabel = new Label(shortDesc);
        descLabel.setStyle("-fx-font-size: 14; -fx-wrap-text: true;");

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");

        // Show button
        Button showBtn = new Button("Voir");
        showBtn.setStyle("-fx-background-color: #4a6baf; -fx-text-fill: white;");
        showBtn.setOnAction(e -> showCategorieDetails(categorie));

        // Edit button
        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");
        editBtn.setOnAction(e -> showEditForm(categorie));

        // Delete button
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> handleDelete(categorie));

        buttonBox.getChildren().addAll(showBtn, editBtn, deleteBtn);
        card.getChildren().addAll(nameLabel, descLabel, buttonBox);

        return card;
    }

    private void setupEventHandlers() {
        addBtn.setOnAction(event -> showAddForm());
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            loadData();
        } else {
            List<Categorie> filtered = categorieService.findAll().stream()
                    .filter(c -> c.getNom().toLowerCase().contains(query) ||
                            c.getDescription().toLowerCase().contains(query))
                    .toList();
            categorieList.setAll(filtered);
            updatePagination();
            displayPage(0);
        }
    }

    private void showAddForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/category/AddCategorie.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Catégorie");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadData();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire", Alert.AlertType.ERROR);
        }
    }

    private void showCategorieDetails(Categorie categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/category/ShowCategorie.fxml"));
            Parent root = loader.load();

            ShowCategorieController controller = loader.getController();
            controller.setCategorie(categorie);

            Stage stage = new Stage();
            stage.setTitle("Détails de la Catégorie");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'afficher les détails", Alert.AlertType.ERROR);
        }
    }

    private void showEditForm(Categorie categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/category/EditCategorie.fxml"));
            Parent root = loader.load();

            EditCategorieController controller = loader.getController();
            controller.setCategorieToEdit(categorie);

            Stage stage = new Stage();
            stage.setTitle("Modifier la Catégorie");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadData();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", Alert.AlertType.ERROR);
        }
    }
    @FXML
    void gotomaterials(ActionEvent event) {
        try {
            // Get the current stage
            Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();

            // Load new window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/materials/IndexMateriel.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Gestion des Matériels");
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
            showAlert("Erreur", "Impossible d'ouvrir la gestion des matériels", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    private void handleDelete(Categorie categorie) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la catégorie");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette catégorie?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                categorieService.supprimer(categorie.getId());
                loadData();
            }
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