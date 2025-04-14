package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import models.Stock;
import service.ProductService;
import service.ProductTypeService;
import service.StockService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    @FXML
    private BorderPane mainBorderPane;
    
    @FXML
    private Label totalProductsLabel;
    
    @FXML
    private Label totalProductTypesLabel;
    
    @FXML
    private Label lowStockItemsLabel;
    
    private ProductService productService;
    private ProductTypeService productTypeService;
    private StockService stockService;
    
    @FXML
    private void initialize() {
        productService = new ProductService();
        productTypeService = new ProductTypeService();
        stockService = new StockService();
        
        // Load the dashboard content view initially
        loadView("/fxml/DashboardContent.fxml");
    }
    
    public void updateDashboardStats() {
        try {
            // Get counts for dashboard
            int productCount = productService.getAll().size();
            int productTypeCount = productTypeService.getAll().size();
            List<Stock> lowStockItems = stockService.findLowStocks();
            
            // Update UI
            totalProductsLabel.setText(String.valueOf(productCount));
            totalProductTypesLabel.setText(String.valueOf(productTypeCount));
            lowStockItemsLabel.setText(String.valueOf(lowStockItems.size()));
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not retrieve dashboard statistics.\n" + e.getMessage());
        }
    }
    
    @FXML
    private void loadDashboard() {
        // Load the Dashboard content view instead of Dashboard.fxml
        loadView("/fxml/DashboardContent.fxml");
    }
    
    @FXML
    private void loadProductManagement() {
        loadView("/fxml/ProductManagement.fxml");
    }
    
    @FXML
    private void loadProductTypeManagement() {
        loadView("/fxml/ProductTypeManagement.fxml");
    }
    
    @FXML
    private void loadStockManagement() {
        loadView("/fxml/StockManagement.fxml");
    }
    
    @FXML
    private void loadReports() {
        loadView("/fxml/Reports.fxml");
    }
    
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            // Clear the center pane content before setting new content
            mainBorderPane.setCenter(null);
            mainBorderPane.setCenter(view);
            
            // If we're loading the dashboard content, make sure we update the stats
            if (fxmlPath.contains("DashboardContent.fxml")) {
                DashboardContentController controller = loader.getController();
                controller.updateDashboardStats();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the requested view.\n" + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 