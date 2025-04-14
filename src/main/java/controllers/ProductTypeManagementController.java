package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.ProductType;
import service.ProductTypeService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ProductTypeManagementController {

    @FXML
    private TableView<ProductType> productTypeTable;
    
    @FXML
    private TableColumn<ProductType, Integer> idColumn;
    
    @FXML
    private TableColumn<ProductType, String> seasonColumn;
    
    @FXML
    private TableColumn<ProductType, String> productionMethodColumn;
    
    @FXML
    private TableColumn<ProductType, Date> harvestDateColumn;
    
    @FXML
    private TableColumn<ProductType, String> preservationDurationColumn;
    
    @FXML
    private TextField seasonField;
    
    @FXML
    private TextField productionMethodField;
    
    @FXML
    private DatePicker harvestDatePicker;
    
    @FXML
    private TextField preservationDurationField;
    
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
    
    private ProductTypeService productTypeService;
    private ObservableList<ProductType> productTypeList;
    private ProductType selectedProductType;
    
    @FXML
    private void initialize() {
        productTypeService = new ProductTypeService();
        productTypeList = FXCollections.observableArrayList();
        
        setupTableColumns();
        loadAllProductTypes();
        setupSearchFieldListener();
        setupButtonHandlers();
        setupTableSelectionListener();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        seasonColumn.setCellValueFactory(new PropertyValueFactory<>("season"));
        productionMethodColumn.setCellValueFactory(new PropertyValueFactory<>("productionMethod"));
        harvestDateColumn.setCellValueFactory(new PropertyValueFactory<>("harvestDate"));
        preservationDurationColumn.setCellValueFactory(new PropertyValueFactory<>("preservationDuration"));
    }
    
    private void loadAllProductTypes() {
        try {
            List<ProductType> productTypes = productTypeService.getAll();
            productTypeList.clear();
            productTypeList.addAll(productTypes);
            productTypeTable.setItems(productTypeList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading product types: " + e.getMessage());
        }
    }
    
    private void setupSearchFieldListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                loadAllProductTypes();
            } else {
                filterProductTypes(newValue);
            }
        });
    }
    
    private void filterProductTypes(String searchText) {
        productTypeList.clear();
        try {
            List<ProductType> allProductTypes = productTypeService.getAll();
            for (ProductType type : allProductTypes) {
                if (type.getSeason().toLowerCase().contains(searchText.toLowerCase()) || 
                    type.getProductionMethod().toLowerCase().contains(searchText.toLowerCase())) {
                    productTypeList.add(type);
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error filtering product types: " + e.getMessage());
        }
    }
    
    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAddProductType());
        updateButton.setOnAction(event -> handleUpdateProductType());
        deleteButton.setOnAction(event -> handleDeleteProductType());
        clearButton.setOnAction(event -> clearFields());
    }
    
    private void setupTableSelectionListener() {
        productTypeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProductType = newSelection;
                populateFields(newSelection);
            }
        });
    }
    
    private void populateFields(ProductType productType) {
        seasonField.setText(productType.getSeason());
        productionMethodField.setText(productType.getProductionMethod());
        
        if (productType.getHarvestDate() != null) {
            LocalDate localDate = productType.getHarvestDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            harvestDatePicker.setValue(localDate);
        } else {
            harvestDatePicker.setValue(null);
        }
        
        preservationDurationField.setText(productType.getPreservationDuration());
    }
    
    private void clearFields() {
        seasonField.clear();
        productionMethodField.clear();
        harvestDatePicker.setValue(null);
        preservationDurationField.clear();
        selectedProductType = null;
        productTypeTable.getSelectionModel().clearSelection();
    }
    
    private void handleAddProductType() {
        if (!validateFields()) {
            return;
        }
        
        try {
            Date harvestDate = Date.from(harvestDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            ProductType productType = new ProductType(
                    seasonField.getText(),
                    productionMethodField.getText(),
                    harvestDate,
                    preservationDurationField.getText()
            );
            
            productTypeService.add(productType);
            loadAllProductTypes();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product Type added successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error adding product type: " + e.getMessage());
        }
    }
    
    private void handleUpdateProductType() {
        if (selectedProductType == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a product type to update.");
            return;
        }
        
        if (!validateFields()) {
            return;
        }
        
        try {
            Date harvestDate = Date.from(harvestDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            selectedProductType.setSeason(seasonField.getText());
            selectedProductType.setProductionMethod(productionMethodField.getText());
            selectedProductType.setHarvestDate(harvestDate);
            selectedProductType.setPreservationDuration(preservationDurationField.getText());
            
            productTypeService.update(selectedProductType);
            loadAllProductTypes();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product Type updated successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error updating product type: " + e.getMessage());
        }
    }
    
    private void handleDeleteProductType() {
        if (selectedProductType == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a product type to delete.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete this product type? This may affect related products.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productTypeService.delete(selectedProductType.getId());
                    loadAllProductTypes();
                    clearFields();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product Type deleted successfully!");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Error deleting product type: " + e.getMessage());
                }
            }
        });
    }
    
    private boolean validateFields() {
        if (seasonField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a season.");
            return false;
        }
        
        if (productionMethodField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a production method.");
            return false;
        }
        
        if (harvestDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a harvest date.");
            return false;
        }
        
        if (preservationDurationField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a preservation duration.");
            return false;
        }
        
        return true;
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 