package controllers.FrontOffice.contrats;

import controllers.FrontOffice.BaseFrontController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import models.Contrat;
import services.ContratService;


import javafx.event.ActionEvent;

import java.io.IOException;
import java.sql.Date;

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

    private final ContratService contratService = new ContratService();

    @FXML
    public void initialize() {
        addContratButton.setOnAction(event -> addContrat());
    }


    private void addContrat() {
        try {
            String nomAcheteur = nomAcheteurField.getText();
            String nomVendeur = nomVendeurField.getText();
            Date dateDebut = Date.valueOf(dateDebutField.getValue());
            Date dateFin = Date.valueOf(dateFinField.getValue());
            String infoContrat = infoContratField.getText();
            String signature = signatureField.getText();

            // Créer le contrat avec les données saisies
            Contrat contrat = new Contrat(
                    null,  // Parcelle ID à mettre si nécessaire
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
           // showAlert("Erreur", "Erreur d'ajout : " + e.getMessage());
        }

    }
    @FXML
    private void goToAfficherContrats(ActionEvent event) {
//        try {
//            Parent root = FXMLLoader.load(getClass().getResource("/FrontOffice/contrats/Affichercontrat.fxml"));
//            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//            stage.setScene(new Scene(root));
//            stage.show();
//        } catch (IOException e) {
//            //showAlert("Erreur", e.getMessage());
//        }

        /// /new code
        try {
            // 1. Charger le layout de base
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            // 2. Charger le contenu Affichercontrat
            Parent content = FXMLLoader.load(getClass().getResource("/FrontOffice/contrats/Affichercontrat.fxml"));

            // 3. Injecter le contenu
            baseController.getContentPane().getChildren().setAll(content);

            // 4. Récupérer la fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(baseRoot));

        } catch (IOException e) {
//            showAlert("Erreur Navigation",
//                    "Impossible de charger les contrats :\n"
//                            + "Vérifiez que les fichiers existent :\n"
//                            + "- /FrontOffice/baseFront.fxml\n"
//                            + "- /FrontOffice/contrats/Affichercontrat.fxml\n"
//                            + "Erreur : " + e.getMessage());
        }


    }
}
