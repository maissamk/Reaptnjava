package controllers.BackOffice.material;

import Models.MaterielLocation;
import Models.MaterielVente;
import Models.user;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import services.MaterielService;
import services.UserServices;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HistoriqueController implements Initializable {

    private final MaterielService materielService = new MaterielService();
    private final UserServices userService = new UserServices();

    @FXML private TableView<MaterielLocation> locationTable;
    @FXML private TableColumn<MaterielLocation, String> locUserMaterialCol;
    @FXML private TableColumn<MaterielLocation, Double> locPriceCol;

    @FXML private TableView<MaterielVente> venteTable;
    @FXML private TableColumn<MaterielVente, String> ventUserMaterialCol;
    @FXML private TableColumn<MaterielVente, Double> ventPriceCol;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        loadData();
    }

    private void setupTables() {
        // Setup location table
        locUserMaterialCol.setCellValueFactory(cellData -> {
            MaterielLocation material = cellData.getValue();
            Integer userId = material.getUserIdMaterielLocationId();
            String materialName = material.getNom();

            if (userId != null) {
                user u = userService.getUserById(userId);
                String userName = (u != null) ? u.getNom() + " " + u.getPrenom() : "User ID: " + userId;
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> String.format("%s - %s", userName, materialName)
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> String.format("N/A - %s", materialName)
            );
        });
        locPriceCol.setCellValueFactory(new PropertyValueFactory<>("prix"));

        // Setup vente table
        ventUserMaterialCol.setCellValueFactory(cellData -> {
            MaterielVente material = cellData.getValue();
            Integer userId = material.getUserIdMaterielVenteId();
            String materialName = material.getNom();

            if (userId != null) {
                user u = userService.getUserById(userId);
                String userName = (u != null) ? u.getNom() + " " + u.getPrenom() : "User ID: " + userId;
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> String.format("%s - %s", userName, materialName)
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> String.format("N/A - %s", materialName)
            );
        });
        ventPriceCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
    }

    private void loadData() {
        // Load rented materials (only those with user IDs)
        List<MaterielLocation> locations = materielService.findAllLocation()
                .stream()
                .filter(m -> m.getUserIdMaterielLocationId() != null)
                .toList();
        locationTable.setItems(FXCollections.observableArrayList(locations));

        // Load purchased materials (only those with user IDs)
        List<MaterielVente> ventes = materielService.findAllVente()
                .stream()
                .filter(m -> m.getUserIdMaterielVenteId() != null)
                .toList();
        venteTable.setItems(FXCollections.observableArrayList(ventes));
    }
}