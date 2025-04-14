package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import models.Stock;
import service.ProductService;
import service.ProductTypeService;
import service.StockService;

import java.sql.SQLException;
import java.util.List;

public class DashboardContentController {

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
        
        updateDashboardStats();
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
            System.err.println("Could not retrieve dashboard statistics: " + e.getMessage());
        }
    }
} 