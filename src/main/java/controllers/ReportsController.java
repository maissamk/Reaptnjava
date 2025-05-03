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
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
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
                Document document = new Document(PageSize.A4, 36, 36, 60, 36);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
                
                // Add header and footer
                writer.setPageEvent(new HeaderFooterPageEvent("Fruitables - Comprehensive Statistics Report"));
                
                document.open();
                
                // Add title
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
                Paragraph title = new Paragraph("Fruitables Comprehensive Statistics Report", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20f);
                document.add(title);
                
                // Add timestamp
                Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
                Paragraph timestamp = new Paragraph("Generated on: " + 
                        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), subtitleFont);
                timestamp.setAlignment(Element.ALIGN_CENTER);
                timestamp.setSpacingAfter(20f);
                document.add(timestamp);
                
                // Add all charts
                document.add(new Paragraph("Product Type Distribution", subtitleFont));
                document.add(new Paragraph("\n"));
                addChartToPdf(document, productTypeChart);
                
                document.add(new Paragraph("\n\nStock Level Analysis", subtitleFont));
                document.add(new Paragraph("\n"));
                addChartToPdf(document, stockLevelChart);
                
                // Add detailed statistics
                addDetailedStatistics(document);
                
                document.close();
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText("PDF Export Complete");
                alert.setContentText("The comprehensive report has been successfully exported to:\n" + file.getAbsolutePath());
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
    
    private void addDetailedStatistics(Document document) throws DocumentException {
        try {
            // Get all products for statistics
            List<Product> products = productService.getAll();
            
            // Add comprehensive statistics section
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph statsTitle = new Paragraph("Detailed Statistics Analysis", sectionFont);
            statsTitle.setAlignment(Element.ALIGN_CENTER);
            statsTitle.setSpacingBefore(30f);
            statsTitle.setSpacingAfter(20f);
            document.add(statsTitle);
            
            // Create statistics table
            PdfPTable statsTable = new PdfPTable(2);
            statsTable.setWidthPercentage(80);
            statsTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            statsTable.setSpacingBefore(15f);
            statsTable.setSpacingAfter(15f);
            
            // Calculate statistics
            int totalProducts = products.size();
            int totalStock = products.stream().mapToInt(Product::getQuantity).sum();
            double totalWeight = products.stream().mapToDouble(p -> p.getWeight() * p.getQuantity()).sum();
            double totalValue = products.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
            double avgPrice = products.stream().mapToDouble(Product::getPrice).average().orElse(0);
            double minPrice = products.stream().mapToDouble(Product::getPrice).min().orElse(0);
            double maxPrice = products.stream().mapToDouble(Product::getPrice).max().orElse(0);
            double avgWeight = products.stream().mapToDouble(Product::getWeight).average().orElse(0);
            int lowStockItems = (int) products.stream().filter(p -> p.getQuantity() < 10).count();
            int outOfStockItems = (int) products.stream().filter(p -> p.getQuantity() == 0).count();
            
            // Add statistics rows
            addStatRow(statsTable, "Total Products", String.valueOf(totalProducts));
            addStatRow(statsTable, "Total Items in Stock", String.valueOf(totalStock));
            addStatRow(statsTable, "Total Weight (kg)", String.format("%.2f", totalWeight));
            addStatRow(statsTable, "Total Inventory Value ($)", String.format("%.2f", totalValue));
            addStatRow(statsTable, "Average Price ($)", String.format("%.2f", avgPrice));
            addStatRow(statsTable, "Price Range ($)", String.format("%.2f - %.2f", minPrice, maxPrice));
            addStatRow(statsTable, "Average Weight (kg)", String.format("%.2f", avgWeight));
            addStatRow(statsTable, "Low Stock Items (<10)", String.valueOf(lowStockItems));
            addStatRow(statsTable, "Out of Stock Items", String.valueOf(outOfStockItems));
            
            document.add(statsTable);
            
            // Add category analysis
            addCategoryAnalysis(document, products);
            
            // Add seasonal analysis
            addSeasonalAnalysis(document, products);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void addStatRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8f);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8f);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }
    
    private void addCategoryAnalysis(Document document, List<Product> products) throws DocumentException {
        // Category analysis title
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph categoryTitle = new Paragraph("Category Analysis", sectionFont);
        categoryTitle.setAlignment(Element.ALIGN_CENTER);
        categoryTitle.setSpacingBefore(30f);
        categoryTitle.setSpacingAfter(20f);
        document.add(categoryTitle);
        
        // Create category analysis table
        PdfPTable categoryTable = new PdfPTable(3);
        categoryTable.setWidthPercentage(80);
        categoryTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        categoryTable.setSpacingBefore(15f);
        categoryTable.setSpacingAfter(15f);
        
        // Add table headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        addTableHeader(categoryTable, new String[]{"Category", "Number of Products", "Total Value ($)"});
        
        // Group by category and calculate statistics
        Map<String, List<Product>> productsByCategory = products.stream()
            .collect(java.util.stream.Collectors.groupingBy(Product::getCategory));
        
        for (Map.Entry<String, List<Product>> entry : productsByCategory.entrySet()) {
            String category = entry.getKey();
            List<Product> categoryProducts = entry.getValue();
            int count = categoryProducts.size();
            double totalValue = categoryProducts.stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
            
            addTableRow(categoryTable, false,
                category,
                String.valueOf(count),
                String.format("%.2f", totalValue));
        }
        
        document.add(categoryTable);
    }
    
    private void addSeasonalAnalysis(Document document, List<Product> products) throws DocumentException {
        // Seasonal analysis title
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph seasonalTitle = new Paragraph("Seasonal Analysis", sectionFont);
        seasonalTitle.setAlignment(Element.ALIGN_CENTER);
        seasonalTitle.setSpacingBefore(30f);
        seasonalTitle.setSpacingAfter(20f);
        document.add(seasonalTitle);
        
        // Create seasonal analysis table
        PdfPTable seasonalTable = new PdfPTable(3);
        seasonalTable.setWidthPercentage(80);
        seasonalTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        seasonalTable.setSpacingBefore(15f);
        seasonalTable.setSpacingAfter(15f);
        
        // Add table headers
        addTableHeader(seasonalTable, new String[]{"Season", "Number of Products", "Total Value ($)"});
        
        // Group by season and calculate statistics
        Map<String, List<Product>> productsBySeason = products.stream()
            .collect(java.util.stream.Collectors.groupingBy(p -> p.getType().getSeason()));
        
        for (Map.Entry<String, List<Product>> entry : productsBySeason.entrySet()) {
            String season = entry.getKey();
            List<Product> seasonProducts = entry.getValue();
            int count = seasonProducts.size();
            double totalValue = seasonProducts.stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
            
            addTableRow(seasonalTable, false,
                season,
                String.valueOf(count),
                String.format("%.2f", totalValue));
        }
        
        document.add(seasonalTable);
    }
    
    private void addTableHeader(PdfPTable table, String[] headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new BaseColor(220, 220, 220));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8f);
            table.addCell(cell);
        }
    }
    
    private void addTableRow(PdfPTable table, boolean alternateRow, String... values) {
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        BaseColor rowColor = alternateRow ? new BaseColor(245, 245, 245) : BaseColor.WHITE;
        
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value, valueFont));
            cell.setBackgroundColor(rowColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8f);
            table.addCell(cell);
        }
    }
    
    private class HeaderFooterPageEvent extends PdfPageEventHelper {
        private String title;
        
        public HeaderFooterPageEvent(String title) {
            this.title = title;
        }
        
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            // Add header
            PdfPTable header = new PdfPTable(1);
            header.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
            header.setLockedWidth(true);
            
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            PdfPCell cell = new PdfPCell(new Phrase(title, headerFont));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.addCell(cell);
            
            header.writeSelectedRows(0, -1, document.leftMargin(), document.getPageSize().getHeight() - 20, writer.getDirectContent());
            
            // Add footer
            PdfPTable footer = new PdfPTable(1);
            footer.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
            footer.setLockedWidth(true);
            
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            cell = new PdfPCell(new Phrase("Page " + writer.getPageNumber(), footerFont));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footer.addCell(cell);
            
            footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin(), writer.getDirectContent());
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