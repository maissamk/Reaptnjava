package controllers.BackOffice.user;

import models.user;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import services.UserServices;
import utils.NavigationUtil;
import utils.SessionManager;

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
        setupTableColumns();
        loadUsers();
        setupSearch();
        addDeleteButtonToTable();
        usersTable.setEditable(true); // Enable table editing
    }

    private void setupTableColumns() {
        TableColumn<user, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(50);

        TableColumn<user, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nameColumn.setPrefWidth(150);

        TableColumn<user, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setPrefWidth(200);

        TableColumn<user, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roles"));
        roleColumn.setPrefWidth(100);

        // Status column with ComboBox
        TableColumn<user, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            private final ComboBox<String> statusComboBox = new ComboBox<>();

            {
                statusComboBox.getItems().addAll("Active", "Inactive", "Blocked");

                statusComboBox.setOnAction(event -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        user user = getTableRow().getItem();
                        String newStatus = statusComboBox.getValue();
                        String oldStatus = user.getStatus();
                        user.setStatus(newStatus);

                        try {
                            userService.update(user);

                            // If blocking current user or admin blocked another user
                            if ("Blocked".equalsIgnoreCase(newStatus)) {
                                // Check if the blocked user is currently logged in
                                if (SessionManager.getInstance().getCurrentUser() != null &&
                                        SessionManager.getInstance().getCurrentUser().getId() == user.getId()) {

                                    SessionManager.getInstance().logout();

                                    // Show alert using the controller's method
                                    showAlert("Account Blocked",
                                            "Your account has been blocked",
                                            "You have been logged out of the system");

                                    NavigationUtil.navigateTo("/FrontOffice/Login.fxml", null);
                                }
                            }

                            updateComboBoxStyle(newStatus);
                        } catch (Exception e) {
                            // Revert the status if update fails
                            statusComboBox.setValue(oldStatus);
                            showAlert("Error", "Status Update Failed", e.getMessage());
                        }
                    }
                });
            }
            private void showAlert(String title, String header, String content) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(content);
                alert.showAndWait();
            }

            private void showErrorAlert(String title, String header, String content) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(content);
                alert.showAndWait();
            }

            private void updateComboBoxStyle(String status) {
                if (status == null) return;

                switch (status.toLowerCase()) {
                    case "active":
                        statusComboBox.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                        break;
                    case "inactive":
                        statusComboBox.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
                        break;
                    case "blocked":
                        statusComboBox.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                        break;
                    default:
                        statusComboBox.setStyle("");
                }
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    statusComboBox.setValue(status);
                    updateComboBoxStyle(status);
                    setGraphic(statusComboBox);
                    setText(null);
                }
            }
        });
        statusColumn.setPrefWidth(100);

        usersTable.getColumns().setAll(idColumn, nameColumn, emailColumn, roleColumn, statusColumn);
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
                } else if (user.getPrenom() != null && user.getPrenom().toLowerCase().contains(lowerCaseFilter)) {
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
        TableColumn<user, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(150);
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(deleteBtn);

            {
                deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    user user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        usersTable.getColumns().add(actionsColumn);
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