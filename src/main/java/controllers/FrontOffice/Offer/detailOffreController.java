package controllers.FrontOffice.Offer;

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

public class detailOffreController {

    @FXML private Label titreLabel;
    @FXML private Label descLabel;
    @FXML private Label competenceLabel;
    @FXML private Label statutLabel;

    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private Offre currentOffre;


    public void setOffre(Offre offre) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/modifierOffre.fxml"));
            Scene scene = new Scene(loader.load());
            modifierOffreController controller = loader.getController();
            controller.prefillForm(currentOffre);  // Set the current data

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
            // Delete the offer (assuming delete logic is implemented)
            OffreService service = new OffreService();
            service.delete(currentOffre.getId());

            // Return to indexOffre.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/indexOffre.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) deleteButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //*******************************************************************************************************//
    //////////////////////////////////////PARTIE EMPLOYE///////////////////////////////////////////////////////

    @FXML private TextField userIdentifierField;
    @FXML private TextField compField;

    @FXML private TableView<Employe> employeTable;
    @FXML private TableColumn<Employe, Integer> user_identifierColumn;
    @FXML private TableColumn<Employe, String> compColumn;

    private EmployeService employeService = new EmployeService();




    private void loadEmployesForOffre(int offreId) {
        user_identifierColumn.setCellValueFactory(new PropertyValueFactory<>("user_identifier"));
        compColumn.setCellValueFactory(new PropertyValueFactory<>("comp"));

        ObservableList<Employe> employes = FXCollections.observableArrayList(
                employeService.getEmployesByOffreId(offreId)
        );
        employeTable.setItems(employes);
    }



}
