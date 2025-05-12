package controllers.BackOffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import Models.Product;
import Models.ProductType;
import Models.Stock;
import service.ProductService;
import service.ProductTypeService;
import service.StockService;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
        try {
            // Create file chooser for saving the Excel file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel Report");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            fileChooser.setInitialFileName("Reaptn_Report_" + 
                java.time.LocalDate.now().format(dateFormatter) + ".xlsx");
            
            File file = fileChooser.showSaveDialog(exportExcelButton.getScene().getWindow());
            if (file == null) {
                return; // User canceled the save dialog
            }
            
            // Create workbook and sheet
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Reaptn Report");
            
            // Get the selected report type
            String reportType = reportTypeComboBox.getValue();
            
            // Create title row with report type
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reaptn Agricultural Report - " + reportType);
            
            // Apply styling to title
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            // Add date row
            Row dateRow = sheet.createRow(1);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Generated on: " + java.time.LocalDate.now().format(dateFormatter));
            
            // Add empty row for spacing
            sheet.createRow(2);
            
            int rowIndex = 3;
            
            // Based on report type, export different data
            switch (reportType) {
                case "Product Type Distribution":
                    rowIndex = exportProductTypeDistribution(workbook, sheet, rowIndex);
                    break;
                case "Stock Level Report":
                    rowIndex = exportStockLevelReport(workbook, sheet, rowIndex);
                    break;
                case "Low Stock Items":
                    rowIndex = exportLowStockReport(workbook, sheet, rowIndex);
                    break;
                case "Seasonal Products":
                    rowIndex = exportSeasonalProductsReport(workbook, sheet, rowIndex);
                    break;
                case "Product Price Analysis":
                    rowIndex = exportPriceAnalysisReport(workbook, sheet, rowIndex);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown report type: " + reportType);
            }
            
            // Auto-size columns for better readability
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write the workbook to the file
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            workbook.close();
            
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", 
                    "Report has been successfully exported to " + file.getAbsolutePath());
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Export Error", 
                    "An error occurred while exporting to Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int exportProductTypeDistribution(XSSFWorkbook workbook, XSSFSheet sheet, int startRowIndex) 
            throws SQLException {
        int rowIndex = startRowIndex;
        
        // Create header row
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Product Type", "Season", "Production Method", "Count", "Percentage"};
        
        // Apply header style
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Get data
        List<ProductType> productTypes = productTypeService.getAll();
        List<Product> products = productService.getAll();
        
        // Count products by type
        Map<ProductType, Integer> typeCounts = new HashMap<>();
        for (ProductType type : productTypes) {
            typeCounts.put(type, 0);
        }
        
        int totalProducts = 0;
        for (Product product : products) {
            ProductType type = product.getType();
            if (type != null) {
                typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
                totalProducts++;
            }
        }
        
        // Create data rows
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.LEFT);
        
        // Create alternate row style
        CellStyle alternateStyle = workbook.createCellStyle();
        alternateStyle.setAlignment(HorizontalAlignment.LEFT);
        alternateStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        alternateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        int rowCounter = 0;
        for (Map.Entry<ProductType, Integer> entry : typeCounts.entrySet()) {
            if (entry.getValue() > 0) {
                ProductType type = entry.getKey();
                int count = entry.getValue();
                double percentage = (double) count / totalProducts * 100;
                
                Row row = sheet.createRow(rowIndex++);
                
                // Apply alternating row styles
                CellStyle rowStyle = (rowCounter % 2 == 0) ? dataStyle : alternateStyle;
                rowCounter++;
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(type.getProductionMethod());
                cell0.setCellStyle(rowStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(type.getSeason());
                cell1.setCellStyle(rowStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(type.getProductionMethod());
                cell2.setCellStyle(rowStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(count);
                cell3.setCellStyle(rowStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(String.format("%.2f%%", percentage));
                cell4.setCellStyle(rowStyle);
            }
        }
        
        // Add empty row
        rowIndex++;
        
        // Add total row
        Row totalRow = sheet.createRow(rowIndex++);
        CellStyle totalStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font totalFont = workbook.createFont();
        totalFont.setBold(true);
        totalStyle.setFont(totalFont);
        
        Cell totalLabelCell = totalRow.createCell(0);
        totalLabelCell.setCellValue("Total Products:");
        totalLabelCell.setCellStyle(totalStyle);
        
        Cell totalValueCell = totalRow.createCell(3);
        totalValueCell.setCellValue(totalProducts);
        totalValueCell.setCellStyle(totalStyle);
        
        return rowIndex;
    }
    
    private int exportStockLevelReport(XSSFWorkbook workbook, XSSFSheet sheet, int startRowIndex) 
            throws Exception {
        int rowIndex = startRowIndex;
        
        // Create header row
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Product", "Category", "Available Quantity", "Minimum Level", "Maximum Level", "Status"};
        
        // Apply header style
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Get data
        List<Stock> stocks = stockService.findAll();
        
        // Create styles for different stock levels
        CellStyle normalStyle = workbook.createCellStyle();
        
        CellStyle lowStockStyle = workbook.createCellStyle();
        lowStockStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        lowStockStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        CellStyle criticalStyle = workbook.createCellStyle();
        criticalStyle.setFillForegroundColor(IndexedColors.RED1.getIndex());
        criticalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        CellStyle overstockStyle = workbook.createCellStyle();
        overstockStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        overstockStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Create data rows
        for (Stock stock : stocks) {
            Product product = stock.getProduct();
            if (product != null) {
                Row row = sheet.createRow(rowIndex++);
                
                // Determine row style based on stock level
                CellStyle rowStyle = normalStyle;
                String status = "Normal";
                
                if (stock.getAvailableQuantity() < stock.getStockMinimum()) {
                    rowStyle = lowStockStyle;
                    status = "Low Stock";
                    
                    if (stock.getAvailableQuantity() < stock.getStockMinimum() * 0.5) {
                        rowStyle = criticalStyle;
                        status = "Critical";
                    }
                } else if (stock.getAvailableQuantity() > stock.getStockMaximum()) {
                    rowStyle = overstockStyle;
                    status = "Overstock";
                }
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(product.getCategory());
                cell0.setCellStyle(rowStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(product.getCategory());
                cell1.setCellStyle(rowStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(stock.getAvailableQuantity());
                cell2.setCellStyle(rowStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(stock.getStockMinimum());
                cell3.setCellStyle(rowStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(stock.getStockMaximum());
                cell4.setCellStyle(rowStyle);
                
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(status);
                cell5.setCellStyle(rowStyle);
            }
        }
        
        return rowIndex;
    }
    
    private int exportLowStockReport(XSSFWorkbook workbook, XSSFSheet sheet, int startRowIndex) 
            throws Exception {
        int rowIndex = startRowIndex;
        
        // Create header row
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Product", "Category", "Available Quantity", "Minimum Level", "Shortage"};
        
        // Apply header style
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Get data
        List<Stock> lowStockItems = stockService.findLowStocks();
        
        // Create styles
        CellStyle lowStockStyle = workbook.createCellStyle();
        lowStockStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        lowStockStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        CellStyle criticalStyle = workbook.createCellStyle();
        criticalStyle.setFillForegroundColor(IndexedColors.RED1.getIndex());
        criticalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Create data rows
        for (Stock stock : lowStockItems) {
            Product product = stock.getProduct();
            if (product != null) {
                Row row = sheet.createRow(rowIndex++);
                
                // Determine row style based on severity
                CellStyle rowStyle = lowStockStyle;
                if (stock.getAvailableQuantity() < stock.getStockMinimum() * 0.5) {
                    rowStyle = criticalStyle;
                }
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(product.getCategory());
                cell0.setCellStyle(rowStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(product.getCategory());
                cell1.setCellStyle(rowStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(stock.getAvailableQuantity());
                cell2.setCellStyle(rowStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(stock.getStockMinimum());
                cell3.setCellStyle(rowStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(stock.getStockMinimum() - stock.getAvailableQuantity());
                cell4.setCellStyle(rowStyle);
            }
        }
        
        return rowIndex;
    }
    
    private int exportSeasonalProductsReport(XSSFWorkbook workbook, XSSFSheet sheet, int startRowIndex) 
            throws SQLException {
        int rowIndex = startRowIndex;
        
        // Create header row
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Season", "Number of Products", "Total Value (DT)"};
        
        // Apply header style
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Get data
        List<Product> products = productService.getAll();
        
        // Group by season and calculate statistics
        Map<String, List<Product>> productsBySeason = products.stream()
            .filter(p -> p.getType() != null && p.getType().getSeason() != null)
            .collect(java.util.stream.Collectors.groupingBy(p -> p.getType().getSeason()));
        
        // Create styles for different seasons
        Map<String, CellStyle> seasonStyles = new HashMap<>();
        
        CellStyle springStyle = workbook.createCellStyle();
        springStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        springStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        seasonStyles.put("Spring", springStyle);
        
        CellStyle summerStyle = workbook.createCellStyle();
        summerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        summerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        seasonStyles.put("Summer", summerStyle);
        
        CellStyle fallStyle = workbook.createCellStyle();
        fallStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        fallStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        seasonStyles.put("Fall", fallStyle);
        
        CellStyle winterStyle = workbook.createCellStyle();
        winterStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        winterStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        seasonStyles.put("Winter", winterStyle);
        
        CellStyle defaultStyle = workbook.createCellStyle();
        
        // Create data rows
        for (Map.Entry<String, List<Product>> entry : productsBySeason.entrySet()) {
            String season = entry.getKey();
            List<Product> seasonProducts = entry.getValue();
            int count = seasonProducts.size();
            double totalValue = seasonProducts.stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
            
            Row row = sheet.createRow(rowIndex++);
            
            // Apply season-specific style if available
            CellStyle rowStyle = seasonStyles.getOrDefault(season, defaultStyle);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(season);
            cell0.setCellStyle(rowStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(count);
            cell1.setCellStyle(rowStyle);
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(totalValue);
            cell2.setCellStyle(rowStyle);
        }
        
        return rowIndex;
    }
    
    private int exportPriceAnalysisReport(XSSFWorkbook workbook, XSSFSheet sheet, int startRowIndex) 
            throws SQLException {
        int rowIndex = startRowIndex;
        
        // Create header row
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Category", "Avg Price (DT)", "Min Price (DT)", "Max Price (DT)", "Product Count"};
        
        // Apply header style
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Get data
        List<Product> products = productService.getAll();
        
        // Group by category
        Map<String, List<Product>> productsByCategory = products.stream()
            .collect(java.util.stream.Collectors.groupingBy(Product::getCategory));
        
        // Create alternating row styles
        CellStyle style1 = workbook.createCellStyle();
        
        CellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Create data rows
        int rowCounter = 0;
        for (Map.Entry<String, List<Product>> entry : productsByCategory.entrySet()) {
            String category = entry.getKey();
            List<Product> categoryProducts = entry.getValue();
            
            double avgPrice = categoryProducts.stream()
                .mapToDouble(Product::getPrice)
                .average()
                .orElse(0);
            
            double minPrice = categoryProducts.stream()
                .mapToDouble(Product::getPrice)
                .min()
                .orElse(0);
            
            double maxPrice = categoryProducts.stream()
                .mapToDouble(Product::getPrice)
                .max()
                .orElse(0);
            
            int count = categoryProducts.size();
            
            Row row = sheet.createRow(rowIndex++);
            
            // Apply alternating row style
            CellStyle rowStyle = (rowCounter % 2 == 0) ? style1 : style2;
            rowCounter++;
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(category);
            cell0.setCellStyle(rowStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(avgPrice);
            cell1.setCellStyle(rowStyle);
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(minPrice);
            cell2.setCellStyle(rowStyle);
            
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(maxPrice);
            cell3.setCellStyle(rowStyle);
            
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(count);
            cell4.setCellStyle(rowStyle);
        }
        
        return rowIndex;
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 