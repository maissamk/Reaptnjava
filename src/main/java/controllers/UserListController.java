package controllers;

import Models.user;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.UserServices;

public class UserListController {
    @FXML private TableView<user> usersTable;
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    private final UserServices userService = new UserServices();
    private ObservableList<user> usersList = FXCollections.observableArrayList();
    private FilteredList<user> filteredData;

    @FXML
    public void initialize() {
        loadUsers();
        setupSearch();
    }

    private void loadUsers() {
        usersList.setAll(userService.getAll());
        countLabel.setText("Total Users: " + usersList.size());
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(usersList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (user.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getPrenom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getRoles().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<user> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedData);
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    @FXML
    private void handleSearch() {
        // Search is handled by the listener
    }
}