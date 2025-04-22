package controllers.FrontOffice.parcelles;

import controllers.FrontOffice.BaseFrontController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.ParcelleProprietes;
import services.ParcelleProprietesService;

import java.io.IOException;
import java.util.List;

public class Afficherparcelles {

    @FXML private GridPane gridPane;
    @FXML private Button btnActualiser;
    @FXML private Button btnRetour;
    private final ParcelleProprietesService service = new ParcelleProprietesService();

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        gridPane.getChildren().clear();
        List<ParcelleProprietes> parcelles = service.getAll();

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

                gridPane.add(card, column, row);
                GridPane.setMargin(card, new Insets(10));

                column = (column + 1) % 3;
                if (column == 0) row++;
            } catch (IOException e) {
                showAlert("Error", "Failed to load parcel card: " + e.getMessage());
            }
        }
    }

    private void openModificationWindow(ParcelleProprietes parcelle) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/Modifierparcelles.fxml"));
//            Parent root = loader.load();
//
//            Modifierparcelles controller = loader.getController();
//            controller.setParcelleToEdit(parcelle);
//            controller.setRefreshCallback(this::loadData);
//
//            Stage stage = new Stage();
//            stage.setScene(new Scene(root));
//            stage.show();
//        } catch (IOException e) {
//            showAlert("Error", "Could not open modification window");
//        }
        try {
            // 1. Charger le layout de base
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            // 2. Charger le contenu Modifierparcelles
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/Modifierparcelles.fxml"));
            Parent content = contentLoader.load();

            // 3. Injecter le contenu dans le layout de base
            baseController.getContentPane().getChildren().setAll(content);

            // 4. Initialiser le contrôleur de modification
            Modifierparcelles controller = contentLoader.getController();
            controller.setParcelleToEdit(parcelle);
            controller.setRefreshCallback(() -> {
                loadData(); // Recharge les données après modification
                ((Stage) baseRoot.getScene().getWindow()).close(); // Ferme la fenêtre de modif
            });

            // 5. Afficher dans une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setScene(new Scene(baseRoot));
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur",
                    "Impossible d'ouvrir l'éditeur :\n"
                            + "Vérifiez que les fichiers existent :\n"
                            + "- /FrontOffice/baseFront.fxml\n"
                            + "- /FrontOffice/parcelles/Modifierparcelles.fxml\n"
                            + "Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleActualiser() {
        loadData();
    }

    @FXML
    private void handleRetour() {
//        try {
//            Parent root = FXMLLoader.load(getClass().getResource("/FrontOffice/parcelles/Ajouterparcelles.fxml"));
//            Stage stage = (Stage) gridPane.getScene().getWindow();
//            stage.setScene(new Scene(root));
//            stage.show();
//        } catch (IOException e) {
//            showAlert("Erreur", "Impossible de charger la vue d'ajout");
//        }

        try {
            // 1. Charger le layout de base (baseFront.fxml)
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml")); // Chemin corrigé
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            // 2. Charger le contenu Afficherparcelles.fxml
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/Ajouterparcelles.fxml")); // Chemin exact
            Parent content = contentLoader.load();

            // 3. Injecter le contenu dans la zone prévue de baseFront.fxml
            baseController.getContentPane().getChildren().setAll(content);

            // 4. Afficher la scène complète (base + contenu)
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(baseRoot));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // showAlert("Erreur", "Impossible d'ouvrir la vue des parcelles.");
        }


    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}