package controllers.FrontOffice.contrats;

import controllers.FrontOffice.Home;
import controllers.FrontOffice.parcelles.DetailParcelle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Models.Contrat;
import services.ContratService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class DetailContrat {

    @FXML private Label labelId;
    @FXML private Label labelDateDebutFin;
    @FXML private Label labelAcheteur;
    @FXML private Label labelVendeur;
    @FXML private Label labelInformation;
    @FXML private Label labelCreation;
    @FXML private Label labelStatus;
    @FXML private Button btnRetour;
    @FXML private Button btnVoirParcelle;
    @FXML private VBox detailsContainer;

    private final ContratService contratService = new ContratService();
    private Contrat contratCourant;

    public void initData(Contrat contrat) {
        Contrat contratComplet = contratService.getByIdWithParcelle(contrat.getId());
        this.contratCourant = contratComplet;

        // Ajoutez cette ligne pour peupler les labels
        populateContratDetails();

        btnVoirParcelle.setDisable(contratComplet.getParcelleProprietes() == null);
    }

    private void populateContratDetails() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        labelId.setText("Contrat N°" + contratCourant.getId());

        String dateRange = String.format("Du %s au %s",
                sdf.format(contratCourant.getDate_debut_contrat()),
                sdf.format(contratCourant.getDatefin_contrat()));
        labelDateDebutFin.setText(dateRange);

        labelAcheteur.setText(contratCourant.getNom_acheteur());
        labelVendeur.setText(contratCourant.getNom_vendeur());
        labelInformation.setText(contratCourant.getInformation_contrat());
        labelCreation.setText("Créé le : " + sdf.format(contratCourant.getDatecreation_contrat()));

        // Afficher le statut s'il existe
        if (contratCourant.getStatus() != null && !contratCourant.getStatus().isEmpty()) {
            labelStatus.setText("Statut : " + contratCourant.getStatus());

            // Appliquer des styles selon le statut
            if (contratCourant.getStatus().contains("Signé")) {
                labelStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else if (contratCourant.getStatus().contains("Envoyé")) {
                labelStatus.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            } else {
                labelStatus.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            }
        } else {
            labelStatus.setText("Statut : Non défini");
            labelStatus.setStyle("-fx-text-fill: #7f8c8d;");
        }

        // Activer ou désactiver le bouton de parcelle selon que la parcelle est associée ou non
        btnVoirParcelle.setDisable(contratCourant.getParcelleProprietes() == null);
    }

    @FXML
    private void handleVoirParcelle() {
        try {
            // Load Home.fxml which contains the navbar
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent homeRoot = homeLoader.load();
            Home homeController = homeLoader.getController();

            // Load the parcelle detail content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/DetailParcelle.fxml"));
            Parent content = contentLoader.load();

            // Get the detail controller and pass the parcelle data
            DetailParcelle controller = contentLoader.getController();
            controller.initData(contratCourant.getParcelleProprietes());

            // Set the content in Home's content pane
            homeController.getMainContentPane().getChildren().setAll(content);

            // Update the stage
            Stage stage = (Stage) btnVoirParcelle.getScene().getWindow();
            stage.setScene(new Scene(homeRoot));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", e.getMessage());
        }
    }


    @FXML
    private void handleRetour() {
        try {
            // Load Home.fxml which contains the navbar
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent homeRoot = homeLoader.load();
            Home homeController = homeLoader.getController();

            // Load the contracts list content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/contrats/Affichercontrat.fxml"));
            Parent content = contentLoader.load();

            // Set the content in Home's content pane
            homeController.getMainContentPane().getChildren().setAll(content);

            // Update the stage
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(homeRoot));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Navigation impossible : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}