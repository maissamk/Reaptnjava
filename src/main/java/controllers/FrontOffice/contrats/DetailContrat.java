package controllers.FrontOffice.contrats;

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
        this.contratCourant = contrat;

        // Charger les détails complets du contrat avec la parcelle
        Contrat contratAvecParcelle = contratService.getByIdWithParcelle(contrat.getId());
        if (contratAvecParcelle != null) {
            this.contratCourant = contratAvecParcelle;
            populateContratDetails();
        } else {
            showAlert("Erreur", "Impossible de charger les détails complets du contrat.");
        }
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
        if (contratCourant.getParcelleProprietes() == null) {
            showAlert("Information", "Aucune parcelle n'est associée à ce contrat.");
            return;
        }

        try {
            // Modifier le chemin pour qu'il corresponde au fichier FXML réel de DetailParcelle
            // (Assurez-vous que ce chemin est correct dans votre projet)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/DetailParcelle.fxml"));
            Parent root = loader.load();

            DetailParcelle controller = loader.getController();
            controller.initData(contratCourant.getParcelleProprietes());

            // Créer une nouvelle scène dans une nouvelle fenêtre pour afficher les détails de la parcelle
            Stage stage = new Stage();
            stage.setTitle("Détails de la Parcelle");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'afficher les détails de la parcelle : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/contrats/Affichercontrat.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));

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