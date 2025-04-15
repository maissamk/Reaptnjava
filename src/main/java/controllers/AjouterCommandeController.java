package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Commande;
import services.CommandeService;

import java.time.LocalDate;
import java.util.Date;
public class AjouterCommandeController {

    @FXML private TextField quantiteField;
    @FXML private TextField totaleField;
    @FXML private DatePicker dateField;

    @FXML private TableView<Commande> tableCommandes;
    @FXML private TableColumn<Commande, Integer> colId;
    @FXML private TableColumn<Commande, Integer> colQuantite;
    @FXML private TableColumn<Commande, Date> colDate;
    @FXML private TableColumn<Commande, Float> colTotale;

    private final CommandeService commandeService = new CommandeService();
    private final ObservableList<Commande> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colQuantite.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantite()).asObject());
        colDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate_commande()));
        colTotale.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getTotale()).asObject());

        refreshTable();
    }

    public void refreshTable() {
        data.setAll(commandeService.getAll());
        tableCommandes.setItems(data);
    }

    @FXML
    public void handleAjouter() {
        int quantite = Integer.parseInt(quantiteField.getText());
        float totale = Float.parseFloat(totaleField.getText());
        LocalDate localDate = dateField.getValue();
        Date date = java.sql.Date.valueOf(localDate);

        Commande c = new Commande(quantite, date, totale);
        commandeService.add(c);
        refreshTable();
        clearFields();
    }

    @FXML
    public void handleModifier() {
        Commande selected = tableCommandes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setQuantite(Integer.parseInt(quantiteField.getText()));
            selected.setTotale(Float.parseFloat(totaleField.getText()));
            selected.setDate_commande(java.sql.Date.valueOf(dateField.getValue()));

            commandeService.update(selected);
            refreshTable();
            clearFields();
        }
    }

    @FXML
    public void handleSupprimer() {
        Commande selected = tableCommandes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            commandeService.delete(selected);
            refreshTable();
            clearFields();
        }
    }

    private void clearFields() {
        quantiteField.clear();
        totaleField.clear();
        dateField.setValue(null);
    }
}
