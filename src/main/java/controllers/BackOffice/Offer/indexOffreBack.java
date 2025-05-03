package controllers.BackOffice.Offer;

import controllers.FrontOffice.BaseFrontController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Offre;
import services.OffreService;
import javafx.scene.control.CheckBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class indexOffreBack {

    @FXML
    private TableView<Offre> offreTable;


    @FXML
    private TableColumn<Offre, String> titreColumn;
    @FXML
    private TableColumn<Offre, String> descrColumn;
    @FXML
    private TableColumn<Offre, String> compColumn;
    @FXML
    private TableColumn<Offre, Boolean> statutColumn;

    @FXML
    private TextField idaField;
    @FXML
    private TextField ideField;
    @FXML
    private TextField titreField;
    @FXML
    private TextField descrField;
    @FXML
    private TextField compField;
    @FXML
    private CheckBox statutCheckBox;


    @FXML
    private Button statisticsBtn;


    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;


    private OffreService offreService;

    public indexOffreBack() {
        offreService = new OffreService();
    }

    @FXML
    public void initialize() {

        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descrColumn.setCellValueFactory(new PropertyValueFactory<>("descr"));
        compColumn.setCellValueFactory(new PropertyValueFactory<>("comp"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        loadOffreData();
    }

    private void loadOffreData() {
        try {
            List<Offre> offres = offreService.select();
            offreTable.getItems().setAll(offres);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addOffre() {
        Offre offre = new Offre();
        offre.setIda(Integer.parseInt(idaField.getText()));
        offre.setIde(Integer.parseInt(ideField.getText()));
        offre.setTitre(titreField.getText());
        offre.setDescr(descrField.getText());
        offre.setComp(compField.getText());
        offre.setStatut(statutCheckBox.isSelected());


        try {
            offreService.add(offre);
            loadOffreData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateOffre() {
        Offre selectedOffre = offreTable.getSelectionModel().getSelectedItem();
        if (selectedOffre != null) {
            selectedOffre.setIda(Integer.parseInt(idaField.getText()));
            selectedOffre.setIde(Integer.parseInt(ideField.getText()));
            selectedOffre.setTitre(titreField.getText());
            selectedOffre.setDescr(descrField.getText());
            selectedOffre.setComp(compField.getText());
            selectedOffre.setStatut(statutCheckBox.isSelected());

            try {
                offreService.update(selectedOffre);
                loadOffreData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void deleteOffre() {
        Offre selectedOffre = offreTable.getSelectionModel().getSelectedItem();
        if (selectedOffre != null) {
            try {
                offreService.delete(selectedOffre.getId());
                loadOffreData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void statisticsNav() {
        System.out.println("vers statistics clicked");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Backoffice/Offre/statistiques.fxml"));
            Parent root = loader.load();


            // Now show the complete scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) statisticsBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
