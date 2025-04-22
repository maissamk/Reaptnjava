package controllers.FrontOffice.Offer;
import controllers.FrontOffice.BaseFrontController;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import models.Offre;
import javafx.event.ActionEvent;
import services.OffreService;
import models.Employe;
import services.EmployeService;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;


public class detailOffreController {

    @FXML private Label titreLabel;
    @FXML private Label descLabel;
    @FXML private Label competenceLabel;
    @FXML private Label statutLabel;

    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private Offre currentOffre;


    public void setOffre(Offre offre) {

        System.out.println("Selected offer ID: " + offre.getId()); // ⬅️ Add this

        if (offre != null) {
            currentOffre = offre; // ✅ This was missing!
            titreLabel.setText(offre.getTitre());
            descLabel.setText(offre.getDescr());
            competenceLabel.setText(offre.getComp());
            statutLabel.setText(offre.isStatut() ? "Validé" : "Non Validé");

            //THE EMPLOYEES TABLE
            loadEmployesForOffre(offre.getId());

        }
    }

    @FXML
    private void handleEditOffre(ActionEvent event) {
        try {

            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/Frontoffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();


            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/modifierOffre.fxml"));
            Parent content = contentLoader.load(); // content with its own controller & methods


            // Inject the page content into base layout
            baseController.getContentPane().getChildren().setAll(content);

            modifierOffreController modifierController = contentLoader.getController();
            modifierController.prefillForm(currentOffre);  // Set the current data

            // Now show the complete scene
            Scene scene = new Scene(baseRoot);
            Stage stage = (Stage) editButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteOffre(ActionEvent event) {
        try {
            OffreService service = new OffreService();
            service.delete(currentOffre.getId());

            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/indexOffre.fxml"));
            Parent content = contentLoader.load();

            baseController.getContentPane().getChildren().setAll(content);

            Scene scene = new Scene(baseRoot);
            Stage stage = (Stage) deleteButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //*******************************************************************************************************//
    //////////////////////////////////////PARTIE EMPLOYE///////////////////////////////////////////////////////





    @FXML private TableView<Employe> employeTable;
    @FXML private TableColumn<Employe, Integer> user_identifierColumn;
    @FXML private TableColumn<Employe, String> compColumn;

    @FXML
    private TextField userIdField;
    @FXML
    private TextField compField;



    private EmployeService employeService = new EmployeService();




    private void loadEmployesForOffre(int offreId) {
        user_identifierColumn.setCellValueFactory(new PropertyValueFactory<>("user_identifier"));
        compColumn.setCellValueFactory(new PropertyValueFactory<>("comp"));

        ObservableList<Employe> employes = FXCollections.observableArrayList(
                employeService.getEmployesByOffreId(offreId)
        );
        employeTable.setItems(employes);
    }

    @FXML
    private void handleAddEmploye() {
        String userIdText = userIdField.getText().trim();
        String competence = compField.getText().trim();


        // 1. Vérifier les champs vides
        if (userIdText.isEmpty() || competence.isEmpty()) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        int userIdentifier;
        try {
            userIdentifier = Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "L'identifiant utilisateur doit être un nombre entier.");
            return;
        }



        try {
            if (currentOffre == null) {
                showAlert("Erreur", "Offre non définie. Impossible de postuler.");
                return;
            }

            Employe newEmploye = new Employe();
            newEmploye.setUser_identifier(userIdentifier);
            newEmploye.setComp(competence);
            newEmploye.setOffre_id(currentOffre.getId()); // safe because we checked it's not null

            employeService.add(newEmploye); // this will set date_join inside

            showAlert("Succès", "Candidature envoyée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de l'envoi de la candidature.");
        }
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteEmploye() {
        String userIdText = userIdField.getText().trim();

        if (userIdText.isEmpty()) {
            showAlert("Champ manquant", "Veuillez entrer l'identifiant de l'utilisateur.");
            return;
        }

        int userIdentifier;
        try {
            userIdentifier = Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "L'identifiant utilisateur doit être un nombre entier.");
            return;
        }

        if (currentOffre == null) {
            showAlert("Erreur", "Offre non définie. Impossible de supprimer la candidature.");
            return;
        }

        try {
            boolean success = employeService.deleteByUserAndOffre(userIdentifier, currentOffre.getId());

            if (success) {
                showAlert("Succès", "Candidature supprimée avec succès !");
                loadEmployesForOffre(currentOffre.getId()); // Refresh the table
            } else {
                showAlert("Erreur", "Aucune candidature trouvée pour cet utilisateur.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de la suppression.");
        }
    }

}
