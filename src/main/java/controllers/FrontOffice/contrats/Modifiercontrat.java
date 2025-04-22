package controllers.FrontOffice.contrats;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Contrat;
import services.ContratService;

import java.sql.Date;
import java.time.LocalDate;

public class Modifiercontrat {

    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField nomAcheteurField;
    @FXML private TextField nomVendeurField;
    @FXML private TextField infoContratField;
    @FXML private TextField signatureField;

    private Contrat currentContrat;
    private final ContratService contratService = new ContratService();

    public void initData(Contrat contrat) {
        this.currentContrat = contrat;

        // Conversion des dates SQL en LocalDate
//        if (contrat.getDate_debut_contrat() != null) {
//            dateDebutPicker.setValue(contrat.getDate_debut_contrat().toLocalDate());
//        }
//        if (contrat.getDatefin_contrat() != null) {
//            dateFinPicker.setValue(contrat.getDatefin_contrat().toLocalDate());
//        }

        nomAcheteurField.setText(contrat.getNom_acheteur());
        nomVendeurField.setText(contrat.getNom_vendeur());
        infoContratField.setText(contrat.getInformation_contrat());
        signatureField.setText(contrat.getSignature_id());
    }

    @FXML
    private void handleSave() {
        try {
            // Validation des champs obligatoires
            if (nomAcheteurField.getText().isEmpty() || nomVendeurField.getText().isEmpty()) {
                throw new IllegalArgumentException("Les champs Acheteur et Vendeur sont obligatoires");
            }

            LocalDate localDebut = dateDebutPicker.getValue();
            LocalDate localFin = dateFinPicker.getValue();

            if (localDebut == null || localFin == null) {
                throw new IllegalArgumentException("Les dates sont obligatoires");
            }

            // Mise à jour de l'objet Contrat
            currentContrat.setDate_debut_contrat(Date.valueOf(localDebut));
            currentContrat.setDatefin_contrat(Date.valueOf(localFin));
            currentContrat.setNom_acheteur(nomAcheteurField.getText().trim());
            currentContrat.setNom_vendeur(nomVendeurField.getText().trim());
            currentContrat.setInformation_contrat(infoContratField.getText().trim());
            currentContrat.setSignature_id(signatureField.getText().trim());

            // Appel du service de mise à jour
            contratService.update(currentContrat);

            // Fermeture de la fenêtre
            closeWindow();

        } catch (IllegalArgumentException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) dateDebutPicker.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}