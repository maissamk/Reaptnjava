package controllers.BackOffice.user;

import Models.user;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.UserServices;
import utils.NavigationUtil;

public class UserListController {
    @FXML private TableView<user> usersTable;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private Label welcomeLabel;

    private final UserServices userService = new UserServices();
    private ObservableList<user> usersList = FXCollections.observableArrayList();
    private FilteredList<user> filteredData;

    @FXML
    public void initialize() {
        loadUsers();
        setupSearch();
        addDeleteButtonToTable();
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

    private void addDeleteButtonToTable() {
        TableColumn<user, Void> colBtn = new TableColumn<>("Actions");

        colBtn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(event -> {
                    user user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
                deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        usersTable.getColumns().add(colBtn);
    }

    private void handleDeleteUser(user user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete " + user.getNom() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                userService.delete(user);
                usersList.remove(user);
                countLabel.setText("Total Users: " + usersList.size());
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    @FXML
    private void handleSearch() {
        // Search is handled by the listener
    }
    @FXML
    private void handleBack() {
        NavigationUtil.navigateTo("/FrontOffice/Home.fxml", welcomeLabel);
    }
}