package controllers.BackOffice.user;

import Models.user;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import services.UserServices;

import java.io.IOException;
import java.util.Map;

public class UserStatsController {
    @FXML private PieChart overviewChart;
    @FXML private BarChart<String, Number> attemptsChart;
    @FXML private TableView<user> usersTable;
    @FXML private TableColumn<user, String> nameColumn;
    @FXML private TableColumn<user, String> emailColumn;
    @FXML private TableColumn<user, Integer> attemptsColumn;
    @FXML private TableColumn<user, String> statusColumn;
    @FXML private Label totalUsersLabel;
    @FXML private Label blockedUsersLabel;
    @FXML private Label highAttemptsLabel;
    @FXML private TextField searchField;


    private final UserServices userService = new UserServices();
    private ObservableList<user> allUsers;

    @FXML
    public void initialize() {
        setupTable();
        loadUserData();
        setupDynamicSearch();
        loadStats();
    }
    @FXML
    private StackPane contentPane;


    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        attemptsColumn.setCellValueFactory(new PropertyValueFactory<>("loginAttempts"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add cell factory to format status properly
        statusColumn.setCellFactory(column -> new TableCell<user, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Format status text
                    String formattedStatus = item.equals("machine") ? "BLOCKED" :
                            item.equals("ACTIVE") ? "ACTIVE" : item;
                    setText(formattedStatus);
                }
            }
        });
    }

    private void loadStats() {
        // Load overview stats
        Map<String, Integer> stats = userService.getLoginAttemptStats();
        totalUsersLabel.setText(String.valueOf(stats.get("total")));
        blockedUsersLabel.setText(String.valueOf(stats.get("blocked")));
        highAttemptsLabel.setText(String.valueOf(stats.get("highAttempts")));

        // Setup pie chart
        overviewChart.getData().clear();  // Clear existing data
        PieChart.Data blockedData = new PieChart.Data("Blocked", stats.get("blocked"));
        PieChart.Data highAttemptsData = new PieChart.Data("High Attempts", stats.get("highAttempts"));
        PieChart.Data normalData = new PieChart.Data("Normal",
                stats.get("total") - stats.get("blocked") - stats.get("highAttempts"));

        overviewChart.getData().addAll(blockedData, highAttemptsData, normalData);
    }
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            usersTable.setItems(allUsers);
        } else {
            FilteredList<user> filteredData = new FilteredList<>(allUsers);
            filteredData.setPredicate(user -> {
                return user.getNom().toLowerCase().contains(searchText) ||
                        user.getEmail().toLowerCase().contains(searchText);
            });
            usersTable.setItems(filteredData);
        }
    }
    private void setupDynamicSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            FilteredList<user> filteredData = new FilteredList<>(allUsers);
            filteredData.setPredicate(user -> {
                // If search text is empty, display all users
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Convert search text to lowercase for case-insensitive search
                String lowerCaseFilter = newValue.toLowerCase();

                // Check if name or email contains the search text
                return user.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        user.getEmail().toLowerCase().contains(lowerCaseFilter);
            });

            // Apply the filtered data to the table
            usersTable.setItems(filteredData);

            // Update the bar chart to show only filtered results
            updateBarChart(filteredData);
        });
    }

    private void loadUserData() {
        try {
            // Load all users with their login attempts
            allUsers = FXCollections.observableArrayList(userService.getAllUsersWithLoginAttempts());
            usersTable.setItems(allUsers);

            // Initialize the bar chart with all users' data
            updateBarChart(new FilteredList<>(allUsers));

        } catch (Exception e) {
            System.err.println("Error loading user data: " + e.getMessage());
            // Show error to user (optional)
            new Alert(Alert.AlertType.ERROR, "Failed to load user data").show();
        }
    }

    // Helper method to update the bar chart with filtered data
    private void updateBarChart(FilteredList<user> filteredData) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Login Attempts");

        for (user user : filteredData) {
            series.getData().add(new XYChart.Data<>(
                    user.getNom() + " " + user.getPrenom(),
                    user.getLoginAttempts()
            ));
        }

        attemptsChart.getData().clear();
        attemptsChart.getData().add(series);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // Just load the dashboard content
        try {
            loadContent("/BackOffice/HomeBack.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentPane.getChildren().setAll(content);
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
        }
    }
}