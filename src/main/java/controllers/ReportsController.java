package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import models.Product;
import models.ProductType;
import models.Stock;
import service.ProductService;
import service.ProductTypeService;
import service.StockService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsController {

    @FXML
    private PieChart productTypeChart;
    
    @FXML
    private BarChart<String, Number> stockLevelChart;
    
    @FXML
    private ComboBox<String> reportTypeComboBox;
    
    @FXML
    private Button generateReportButton;
    
    @FXML
    private Button exportPdfButton;
    
    @FXML
    private Button exportExcelButton;
    
    private ProductService productService;
    private ProductTypeService productTypeService;
    private StockService stockService;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @FXML
    private void initialize() {
        productService = new ProductService();
        productTypeService = new ProductTypeService();
        stockService = new StockService();
        
        setupComboBox();
        setupButtonHandlers();
        
        // Generate default charts
        generateProductTypeDistribution();
        generateStockLevelReport();
    }
    
    private void setupComboBox() {
        ObservableList<String> reportTypes = FXCollections.observableArrayList(
                "Product Type Distribution",
                "Stock Level Report",
                "Low Stock Items",
                "Seasonal Products",
                "Product Price Analysis"
        );
        reportTypeComboBox.setItems(reportTypes);
        reportTypeComboBox.setValue("Product Type Distribution");
    }
    
    private void setupButtonHandlers() {
        generateReportButton.setOnAction(event -> generateReport());
        exportPdfButton.setOnAction(event -> exportToPdf());
        exportExcelButton.setOnAction(event -> exportToExcel());
    }
    
    private void generateReport() {
        String reportType = reportTypeComboBox.getValue();
        
        if (reportType == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a report type.");
            return;
        }
        
        switch (reportType) {
            case "Product Type Distribution":
                generateProductTypeDistribution();
                break;
            case "Stock Level Report":
                generateStockLevelReport();
                break;
            case "Low Stock Items":
                generateLowStockReport();
                break;
            case "Seasonal Products":
                generateSeasonalProductsReport();
                break;
            case "Product Price Analysis":
                generatePriceAnalysisReport();
                break;
            default:
                showAlert(Alert.AlertType.WARNING, "Invalid Selection", "Please select a valid report type.");
        }
    }
    
    private void generateProductTypeDistribution() {
        try {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            List<ProductType> productTypes = productTypeService.getAll();
            
            // Count products by type
            Map<String, Integer> typeCounts = new HashMap<>();
            for (ProductType type : productTypes) {
                typeCounts.put(type.getSeason() + " - " + type.getProductionMethod(), 0);
            }
            
            List<Product> products = productService.getAll();
            for (Product product : products) {
                ProductType type = product.getType();
                if (type != null) {
                    String key = type.getSeason() + " - " + type.getProductionMethod();
                    typeCounts.put(key, typeCounts.getOrDefault(key, 0) + 1);
                }
            }
            
            // Create pie chart data
            for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
                if (entry.getValue() > 0) {
                    pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
            }
            
            productTypeChart.setData(pieChartData);
            productTypeChart.setTitle("Product Distribution by Type");
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error generating product type distribution: " + e.getMessage());
        }
    }
    
    private void generateStockLevelReport() {
        try {
            stockLevelChart.getData().clear();
            XYChart.Series<String, Number> availableSeries = new XYChart.Series<>();
            availableSeries.setName("Available Quantity");
            
            XYChart.Series<String, Number> minimumSeries = new XYChart.Series<>();
            minimumSeries.setName("Minimum Level");
            
            XYChart.Series<String, Number> maximumSeries = new XYChart.Series<>();
            maximumSeries.setName("Maximum Level");
            
            List<Stock> stocks = stockService.findAll();
            for (Stock stock : stocks) {
                Product product = stock.getProduct();
                if (product != null) {
                    String productName = product.getCategory();
                    availableSeries.getData().add(new XYChart.Data<>(productName, stock.getAvailableQuantity()));
                    minimumSeries.getData().add(new XYChart.Data<>(productName, stock.getStockMinimum()));
                    maximumSeries.getData().add(new XYChart.Data<>(productName, stock.getStockMaximum()));
                }
            }
            
            stockLevelChart.getData().addAll(availableSeries, minimumSeries, maximumSeries);
            stockLevelChart.setTitle("Stock Level Report");
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error generating stock level report: " + e.getMessage());
        }
    }
    
    private void generateLowStockReport() {
        try {
            stockLevelChart.getData().clear();
            XYChart.Series<String, Number> availableSeries = new XYChart.Series<>();
            availableSeries.setName("Available Quantity");
            
            XYChart.Series<String, Number> minimumSeries = new XYChart.Series<>();
            minimumSeries.setName("Minimum Level");
            
            List<Stock> lowStockItems = stockService.findLowStocks();
            for (Stock stock : lowStockItems) {
                Product product = stock.getProduct();
                if (product != null) {
                    String productName = product.getCategory();
                    availableSeries.getData().add(new XYChart.Data<>(productName, stock.getAvailableQuantity()));
                    minimumSeries.getData().add(new XYChart.Data<>(productName, stock.getStockMinimum()));
                }
            }
            
            stockLevelChart.getData().addAll(availableSeries, minimumSeries);
            stockLevelChart.setTitle("Low Stock Items Report");
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error generating low stock report: " + e.getMessage());
        }
    }
    
    private void generateSeasonalProductsReport() {
        try {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            // Count products by season
            Map<String, Integer> seasonCounts = new HashMap<>();
            List<Product> products = productService.getAll();
            
            for (Product product : products) {
                ProductType type = product.getType();
                if (type != null && type.getSeason() != null) {
                    String season = type.getSeason();
                    seasonCounts.put(season, seasonCounts.getOrDefault(season, 0) + 1);
                }
            }
            
            // Create pie chart data
            for (Map.Entry<String, Integer> entry : seasonCounts.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            
            productTypeChart.setData(pieChartData);
            productTypeChart.setTitle("Products by Season");
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error generating seasonal products report: " + e.getMessage());
        }
    }
    
    private void generatePriceAnalysisReport() {
        try {
            stockLevelChart.getData().clear();
            XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
            priceSeries.setName("Price");
            
            List<Product> products = productService.getAll();
            for (Product product : products) {
                priceSeries.getData().add(new XYChart.Data<>(product.getCategory(), product.getPrice()));
            }
            
            stockLevelChart.getData().add(priceSeries);
            stockLevelChart.setTitle("Product Price Analysis");
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error generating price analysis report: " + e.getMessage());
        }
    }
    
    private void exportToPdf() {
        showAlert(Alert.AlertType.INFORMATION, "Feature Coming Soon", 
                 "Export to PDF functionality will be available in a future update.");
    }
    
    private void exportToExcel() {
        showAlert(Alert.AlertType.INFORMATION, "Feature Coming Soon", 
                 "Export to Excel functionality will be available in a future update.");
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 