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
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
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
        
        // Setup export buttons
        exportPdfButton.setOnAction(event -> exportToPdf());
        exportExcelButton.setOnAction(event -> exportToExcel());
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
        try {
            // Create a file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF Report");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(exportPdfButton.getScene().getWindow());
            
            if (file != null) {
                // Create PDF document
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                
                // Add title
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("Farm Management Report", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("\n"));
                
                // Add report type
                Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                Paragraph reportType = new Paragraph("Report Type: " + 
                        reportTypeComboBox.getSelectionModel().getSelectedItem(), subtitleFont);
                document.add(reportType);
                document.add(new Paragraph("\n"));
                
                // Add timestamp
                document.add(new Paragraph("Generated on: " + 
                        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                document.add(new Paragraph("\n"));
                
                // Take snapshot of the charts and add to PDF
                if (reportTypeComboBox.getValue().equals("Product Type Distribution")) {
                    // Add pie chart
                    document.add(new Paragraph("Product Type Distribution:", subtitleFont));
                    document.add(new Paragraph("\n"));
                    addChartToPdf(document, productTypeChart);
                } else if (reportTypeComboBox.getValue().equals("Stock Level Report")) {
                    // Add bar chart
                    document.add(new Paragraph("Stock Level Analysis:", subtitleFont));
                    document.add(new Paragraph("\n"));
                    addChartToPdf(document, stockLevelChart);
                }
                
                document.close();
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText("PDF Export Complete");
                alert.setContentText("The report has been successfully exported to:\n" + file.getAbsolutePath());
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText("PDF Export Error");
            alert.setContentText("Failed to export the report: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void addChartToPdf(Document document, javafx.scene.Node chart) throws DocumentException, IOException {
        // Take snapshot of the chart
        WritableImage snapshot = chart.snapshot(new SnapshotParameters(), null);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
        
        // Convert to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        
        // Add image to PDF
        Image chartImage = Image.getInstance(imageBytes);
        float pageWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
        float chartWidth = pageWidth * 0.9f;
        chartImage.scaleToFit(chartWidth, chartWidth * 0.75f);
        chartImage.setAlignment(Element.ALIGN_CENTER);
        document.add(chartImage);
        document.add(new Paragraph("\n"));
    }
    
    private void exportToExcel() {
        // Excel export functionality will be implemented here
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Coming Soon");
        alert.setHeaderText("Excel Export");
        alert.setContentText("Excel export functionality will be implemented in a future update.");
        alert.showAndWait();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 