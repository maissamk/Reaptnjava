package controllers.BackOffice.Employe;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import Models.Employe;
import services.EmployeService;
import java.time.LocalDateTime;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class indexEmployeBack {

    @FXML
    private TableView<Employe> employeTable; // Table to display Employe records

    @FXML
    private TableColumn<Employe, Integer> idColumn; // Column for ID
    @FXML
    private TableColumn<Employe, String> compColumn; // Column for Comp
    @FXML
    private TableColumn<Employe, Integer> offreIdColumn; // Column for Offre ID
    @FXML
    private TableColumn<Employe, Integer> userIdColumn; // Column for User ID
    @FXML
    private TableColumn<Employe, String> dispoColumn; // Column for Dispo
    @FXML
    private TableColumn<Employe, Boolean> confColumn; // Column for Conf
    @FXML
    private TableColumn<Employe, Boolean> suggestedColumn; // Column for Suggested
    @FXML
    private TableColumn<Employe, LocalDateTime> dateJoinColumn; // Column for Date Join

    @FXML
    private TextField userIdField; // TextField for User ID input
    @FXML
    private TextField compField; // TextField for Comp input
    @FXML
    private TextField offreIdField; // TextField for Offre ID input

    @FXML
    private Button addButton; // Button to add new Employe
    @FXML
    private Button updateButton; // Button to update selected Employe
    @FXML
    private Button deleteButton; // Button to delete selected Employe

    private EmployeService employeService;

    public indexEmployeBack() {
        employeService = new EmployeService();
    }

    @FXML
    public void initialize() {
        // Set cell value factories for each column
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        compColumn.setCellValueFactory(new PropertyValueFactory<>("comp"));
        offreIdColumn.setCellValueFactory(new PropertyValueFactory<>("offre_id"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("user_identifier"));
        dispoColumn.setCellValueFactory(new PropertyValueFactory<>("dispo"));
        confColumn.setCellValueFactory(new PropertyValueFactory<>("conf"));
        suggestedColumn.setCellValueFactory(new PropertyValueFactory<>("suggested"));
        dateJoinColumn.setCellValueFactory(new PropertyValueFactory<>("dateJoin"));

        loadEmployeData(); // Load data into the table
    }

    private void loadEmployeData() {
        try {
            List<Employe> employes = employeService.select();
            employeTable.getItems().setAll(employes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addEmploye() {
        // Logic to add a new Employe
        Employe employe = new Employe();
        employe.setUser_identifier(Integer.parseInt(userIdField.getText()));
        employe.setComp(compField.getText());
        employe.setOffre_id(Integer.parseInt(offreIdField.getText()));
        // Set other fields as necessary

        try {
            employeService.add(employe);
            loadEmployeData(); // Refresh the table
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateEmploye() {
        // Logic to update the selected Employe
        Employe selectedEmploye = employeTable.getSelectionModel().getSelectedItem();
        if (selectedEmploye != null) {
            selectedEmploye.setUser_identifier(Integer.parseInt(userIdField.getText()));
            selectedEmploye.setComp(compField.getText());
            selectedEmploye.setOffre_id(Integer.parseInt(offreIdField.getText()));
            // Set other fields as necessary

            try {
                employeService.update(selectedEmploye);
                loadEmployeData(); // Refresh the table
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void deleteEmploye() {
        // Logic to delete the selected Employe
        Employe selectedEmploye = employeTable.getSelectionModel().getSelectedItem();
        if (selectedEmploye != null) {
            try {
                employeService.delete(selectedEmploye.getId());
                loadEmployeData(); // Refresh the table
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}