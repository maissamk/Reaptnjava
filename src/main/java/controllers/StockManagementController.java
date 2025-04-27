package controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import models.Product;
import models.Stock;
import service.ProductService;
import service.StockService;
import utils.EmailService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StockManagementController {

    @FXML
    private TableView<Stock> stockTable;
    
    @FXML
    private TableColumn<Stock, Integer> idColumn;
    
    @FXML
    private TableColumn<Stock, Product> productColumn;
    
    @FXML
    private TableColumn<Stock, Integer> availableQuantityColumn;
    
    @FXML
    private TableColumn<Stock, Float> stockMinimumColumn;
    
    @FXML
    private TableColumn<Stock, Float> stockMaximumColumn;
    
    @FXML
    private TableColumn<Stock, LocalDateTime> entryDateColumn;
    
    @FXML
    private TableColumn<Stock, LocalDateTime> exitDateColumn;
    
    @FXML
    private ComboBox<Product> productComboBox;
    
    @FXML
    private TextField availableQuantityField;
    
    @FXML
    private TextField stockMinimumField;
    
    @FXML
    private TextField stockMaximumField;
    
    @FXML
    private DatePicker entryDatePicker;
    
    @FXML
    private DatePicker exitDatePicker;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button updateButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Button showLowStockButton;
    
    private StockService stockService;
    private ProductService productService;
    private ObservableList<Stock> stockList;
    private Stock selectedStock;
    private EmailService emailService;
    
    @FXML
    public void initialize() {
        stockService = new StockService();
        productService = new ProductService();
        stockList = FXCollections.observableArrayList();
        emailService = new EmailService();
        
        // Set up table columns
        // idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productColumn.setCellValueFactory(cellData -> {
            return new SimpleObjectProperty<>(cellData.getValue().getProduct());
        });
        productColumn.setCellFactory(column -> new TableCell<Stock, Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCategory());
                }
            }
        });
        availableQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        stockMinimumColumn.setCellValueFactory(new PropertyValueFactory<>("stockMinimum"));
        stockMaximumColumn.setCellValueFactory(new PropertyValueFactory<>("stockMaximum"));
        entryDateColumn.setCellValueFactory(new PropertyValueFactory<>("entryDate"));
        entryDateColumn.setCellFactory(column -> new TableCell<Stock, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalDate().toString());
                }
            }
        });
        exitDateColumn.setCellValueFactory(new PropertyValueFactory<>("exitDate"));
        exitDateColumn.setCellFactory(column -> new TableCell<Stock, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalDate().toString());
                }
            }
        });

        // Remove ID column from the table view
        stockTable.getColumns().remove(idColumn);

        // Load products for combobox
        loadProducts();
        
        // Set up product ComboBox converter
        productComboBox.setConverter(new StringConverter<Product>() {
            @Override
            public String toString(Product product) {
                return product != null ? product.getCategory() : "";
            }

            @Override
            public Product fromString(String string) {
                return productComboBox.getItems().stream()
                        .filter(product -> product.getCategory().equals(string))
                        .findFirst().orElse(null);
            }
        });

        // Load stock entries
        loadStockEntries();
        
        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterStockEntries(newValue);
        });
        
        // Set up table row selection handler
        stockTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        displayStockDetails(newSelection);
                    }
                });
        
        setupButtonHandlers();
    }
    
    private void loadAllStocks() {
        try {
            List<Stock> stocks = stockService.findAll();
            stockList.clear();
            stockList.addAll(stocks);
            stockTable.setItems(stockList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading stocks: " + e.getMessage());
        }
    }
    
    private void loadProducts() {
        try {
            List<Product> products = productService.getAll();
            productComboBox.getItems().clear();
            productComboBox.getItems().addAll(products);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }
    }
    
    private void loadStockEntries() {
        try {
            List<Stock> stocks = stockService.findAll();
            stockTable.getItems().clear();
            stockTable.getItems().addAll(stocks);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load stock entries: " + e.getMessage());
        }
    }
    
    private void filterStockEntries(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            loadStockEntries();
            return;
        }
        
        try {
            List<Stock> filteredList = stockService.findAll().stream()
                .filter(stock -> 
                    stock.getProduct().getCategory().toLowerCase().contains(searchText.toLowerCase()) ||
                    String.valueOf(stock.getAvailableQuantity()).contains(searchText) ||
                    String.valueOf(stock.getStockMinimum()).contains(searchText) ||
                    String.valueOf(stock.getStockMaximum()).contains(searchText))
                .collect(Collectors.toList());
            
            stockTable.getItems().clear();
            stockTable.getItems().addAll(filteredList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to filter stock entries: " + e.getMessage());
        }
    }
    
    private void displayStockDetails(Stock stock) {
        if (stock != null) {
            selectedStock = stock;
            productComboBox.setValue(stock.getProduct());
            availableQuantityField.setText(String.valueOf(stock.getAvailableQuantity()));
            stockMinimumField.setText(String.valueOf(stock.getStockMinimum()));
            stockMaximumField.setText(String.valueOf(stock.getStockMaximum()));
            entryDatePicker.setValue(stock.getEntryDate() != null ? stock.getEntryDate().toLocalDate() : null);
            exitDatePicker.setValue(stock.getExitDate() != null ? stock.getExitDate().toLocalDate() : null);
        }
    }
    
    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAddAction());
        updateButton.setOnAction(event -> handleUpdateAction());
        deleteButton.setOnAction(event -> handleDeleteAction());
        clearButton.setOnAction(event -> handleClearAction());
        showLowStockButton.setOnAction(event -> showLowStockItems());
    }
    
    @FXML
    private void handleAddAction() {
        try {
            // Validation
            if (productComboBox.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a product.");
                return;
            }
            
            if (availableQuantityField.getText().isEmpty() || stockMinimumField.getText().isEmpty() || stockMaximumField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity, minimum and maximum stock values are required.");
                return;
            }
            
            if (entryDatePicker.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Entry date is required.");
                return;
            }
            
            // Parse values
            int availableQty = Integer.parseInt(availableQuantityField.getText());
            int minStock = Integer.parseInt(stockMinimumField.getText());
            int maxStock = Integer.parseInt(stockMaximumField.getText());
            
            if (minStock > maxStock) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Minimum stock cannot be greater than maximum stock.");
                return;
            }
            
            if (availableQty < 0 || minStock < 0 || maxStock < 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Stock values cannot be negative.");
                return;
            }
            
            // Create stock object
            Stock stock = new Stock();
            stock.setProduct(productComboBox.getValue());
            stock.setAvailableQuantity(availableQty);
            stock.setStockMinimum(minStock);
            stock.setStockMaximum(maxStock);
            stock.setEntryDate(entryDatePicker.getValue() != null ? 
                               entryDatePicker.getValue().atStartOfDay() : null);
            stock.setExitDate(exitDatePicker.getValue() != null ? 
                             exitDatePicker.getValue().atStartOfDay() : null);
            
            // Save to database
            stockService.add(stock);
            
            // Refresh table and clear form
            loadStockEntries();
            clearForm();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Stock entry added successfully.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numeric values for quantity and stock levels.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add stock entry: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdateAction() {
        if (selectedStock == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a stock entry to update.");
            return;
        }
        
        try {
            // Validation
            if (productComboBox.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a product.");
                return;
            }
            
            if (availableQuantityField.getText().isEmpty() || stockMinimumField.getText().isEmpty() || stockMaximumField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity, minimum and maximum stock values are required.");
                return;
            }
            
            if (entryDatePicker.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Entry date is required.");
                return;
            }
            
            // Parse values
            int availableQty = Integer.parseInt(availableQuantityField.getText());
            int minStock = Integer.parseInt(stockMinimumField.getText());
            int maxStock = Integer.parseInt(stockMaximumField.getText());
            
            if (minStock > maxStock) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Minimum stock cannot be greater than maximum stock.");
                return;
            }
            
            if (availableQty < 0 || minStock < 0 || maxStock < 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Stock values cannot be negative.");
                return;
            }
            
            // Update stock object
            selectedStock.setProduct(productComboBox.getValue());
            selectedStock.setAvailableQuantity(availableQty);
            selectedStock.setStockMinimum(minStock);
            selectedStock.setStockMaximum(maxStock);
            selectedStock.setEntryDate(entryDatePicker.getValue() != null ? 
                               entryDatePicker.getValue().atStartOfDay() : null);
            selectedStock.setExitDate(exitDatePicker.getValue() != null ? 
                             exitDatePicker.getValue().atStartOfDay() : null);
            
            // Save to database
            stockService.update(selectedStock);
            
            // Refresh table and clear form
            loadStockEntries();
            clearForm();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Stock entry updated successfully.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numeric values for quantity and stock levels.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update stock entry: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteAction() {
        if (selectedStock == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a stock entry to delete.");
            return;
        }
        
        try {
            // Confirm deletion
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Delete");
            confirmDialog.setHeaderText("Delete Stock Entry");
            confirmDialog.setContentText("Are you sure you want to delete this stock entry?");
            
            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Delete from database
                stockService.delete(selectedStock.getId());
                
                // Refresh table and clear form
                loadStockEntries();
                clearForm();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Stock entry deleted successfully.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete stock entry: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearAction() {
        clearForm();
    }
    
    private void clearForm() {
        selectedStock = null;
        productComboBox.setValue(null);
        availableQuantityField.clear();
        stockMinimumField.clear();
        stockMaximumField.clear();
        entryDatePicker.setValue(null);
        exitDatePicker.setValue(null);
        stockTable.getSelectionModel().clearSelection();
    }
    
    private void showLowStockItems() {
        try {
            List<Stock> lowStockItems = stockService.findLowStocks();
            stockList.clear();
            stockList.addAll(lowStockItems);
            stockTable.setItems(stockList);
            
            if (lowStockItems.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Low Stock Items", "No low stock items found.");
            } else {
                // Show low stock alert
                Alert lowStockAlert = new Alert(Alert.AlertType.WARNING);
                lowStockAlert.setTitle("Low Stock Alert");
                lowStockAlert.setHeaderText("Low Stock Items Detected");
                
                StringBuilder content = new StringBuilder();
                content.append("The following items are below minimum stock levels:\n\n");
                
                for (Stock stock : lowStockItems) {
                    content.append("• ").append(stock.getProduct().getCategory())
                           .append(" - Current: ").append(stock.getAvailableQuantity())
                           .append(", Minimum: ").append((int)stock.getStockMinimum())
                           .append("\n");
                }
                
                // Add information about automatic email
                content.append("\nAn email notification is being sent automatically.");
                lowStockAlert.setContentText(content.toString());
                
                // Show the alert
                lowStockAlert.showAndWait();
                
                // Send email automatically
                sendLowStockEmailAutomatically(lowStockItems);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading low stock items: " + e.getMessage());
        }
    }
    
    private void sendLowStockEmailAutomatically(List<Stock> lowStockItems) {
        // Default recipient email - could be stored in application settings
        String recipientEmail = "manager@fruitables.com";
        
        // Show sending indicator
        Alert sendingAlert = new Alert(Alert.AlertType.INFORMATION);
        sendingAlert.setTitle("Sending Email");
        sendingAlert.setHeaderText(null);
        sendingAlert.setContentText("Automatically sending low stock alert email...");
        sendingAlert.show();
        
        // Use separate thread for email sending to avoid freezing UI
        new Thread(() -> {
            boolean success = emailService.sendLowStockAlert(recipientEmail, lowStockItems);
            
            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                sendingAlert.close();
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Email Sent", 
                        "Low stock alert email has been automatically sent to " + recipientEmail);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Email Error", 
                        "Failed to send automatic email. Please check Mailtrap credentials in EmailService.java");
                }
            });
        }).start();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 