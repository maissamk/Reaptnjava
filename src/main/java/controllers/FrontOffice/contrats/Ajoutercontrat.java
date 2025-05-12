package controllers.FrontOffice.contrats;

import controllers.FrontOffice.BaseFrontController;
import controllers.FrontOffice.Home;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Models.Contrat;
import services.ContratService;


import javafx.event.ActionEvent;

import java.io.IOException;
import java.sql.Date;

import static utils.NavigationUtil.showAlert;

public class Ajoutercontrat {

    @FXML
    private TextField nomAcheteurField;
    @FXML
    private TextField nomVendeurField;
    @FXML
    private DatePicker dateDebutField;
    @FXML
    private DatePicker dateFinField;
    @FXML
    private TextField infoContratField;
    @FXML
    private TextField signatureField;
    @FXML
    private Button addContratButton;

    // Nouveaux champs pour les informations de parcelle
    @FXML
    private Label parcelleInfoLabel;
    @FXML
    private TextField parcelleTitreField;

    private Integer parcelleId = null;
    private String parcelleTitle = null;

    private final ContratService contratService = new ContratService();

    @FXML
    public void initialize() {
        addContratButton.setOnAction(event -> addContrat());

        // Initialiser le label d'info parcelle comme invisible par défaut
        if (parcelleInfoLabel != null) {
            parcelleInfoLabel.setVisible(false);
        }
    }

    /**
     * Initialise le formulaire avec les informations de la parcelle
     * @param parcelleId ID de la parcelle
     * @param parcelleTitle Titre de la parcelle
     */
    public void initWithParcelleInfo(int parcelleId, String parcelleTitle) {
        this.parcelleId = parcelleId;
        this.parcelleTitle = parcelleTitle;

        // Mettre à jour l'interface utilisateur
        if (parcelleTitreField != null) {
            parcelleTitreField.setText(parcelleTitle);
        }

        if (parcelleInfoLabel != null) {
            parcelleInfoLabel.setText("Contrat associé à la parcelle: " + parcelleTitle + " (ID: " + parcelleId + ")");
            parcelleInfoLabel.setVisible(true);
        }
    }

    private void addContrat() {
        try {
            String nomAcheteur = nomAcheteurField.getText();
            String nomVendeur = nomVendeurField.getText();
            Date dateDebut = Date.valueOf(dateDebutField.getValue());
            Date dateFin = Date.valueOf(dateFinField.getValue());
            String infoContrat = infoContratField.getText();
            String signature = signatureField.getText();

            // Mettre à jour le titre de la parcelle si le champ a été modifié
            if (parcelleTitreField != null && !parcelleTitreField.getText().isEmpty()) {
                parcelleTitle = parcelleTitreField.getText();
            }

            // Créer le contrat avec les données saisies
            Contrat contrat = new Contrat(
                    parcelleId,  // Utiliser l'ID de la parcelle si disponible
                    dateDebut,
                    dateFin,
                    nomAcheteur,
                    nomVendeur,
                    infoContrat,
                    new java.util.Date(),
                    null,  // User ID à mettre si nécessaire
                    signature,
                    null,  // Document ID à mettre si nécessaire
                    null   // Signer ID à mettre si nécessaire
            );

            // Appeler le service pour ajouter le contrat
            contratService.add(contrat);

            goToAfficherContrats(null);
        } catch (Exception e) {
            // TODO: Ajouter un gestionnaire d'erreur approprié
            System.err.println("Erreur lors de l'ajout du contrat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAfficherContrats(ActionEvent event) {
        try {
            // 1. Load Home.fxml which contains the navbar
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent homeRoot = homeLoader.load();
            Home homeController = homeLoader.getController();

            // 2. Load the Affichercontrat content
            Parent content = FXMLLoader.load(getClass().getResource("/FrontOffice/contrats/Affichercontrat.fxml"));

            // 3. Inject the content into Home's content pane
            homeController.getMainContentPane().getChildren().setAll(content);

            // 4. Get the current window
            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) addContratButton.getScene().getWindow();
            }

            // 5. Update the stage with the new scene
            stage.setScene(new Scene(homeRoot));
            stage.show();

        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
            // You might want to show an alert to the user here
            showAlert("Navigation Error", "Failed to load contracts view: " + e.getMessage());
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