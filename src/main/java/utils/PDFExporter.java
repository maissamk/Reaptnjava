package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import models.Product;
import models.ProductType;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.util.Map;

/**
 * Utility class for exporting data to PDF files
 */
public class PDFExporter {
    
    // Improved fonts with better typography
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, new BaseColor(44, 62, 80));
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.ITALIC, new BaseColor(52, 73, 94));
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(44, 62, 80));
    private static final Font TOTAL_FONT = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(44, 62, 80));
    
    // Primary colors for the application
    private static final BaseColor PRIMARY_COLOR = new BaseColor(39, 174, 96);
    private static final BaseColor SECONDARY_COLOR = new BaseColor(26, 115, 64);
    private static final BaseColor ACCENT_COLOR = new BaseColor(230, 126, 34);
    private static final BaseColor LIGHT_GRAY = new BaseColor(241, 241, 241);
    
    /**
     * Export product inventory report to PDF
     * 
     * @param products List of products to export
     * @param filePath Path where to save the PDF file
     * @return True if export was successful, false otherwise
     */
    public static boolean exportProductInventory(List<Product> products, String filePath) {
        try {
            Document document = new Document(PageSize.A4, 36, 36, 60, 36);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            
            // Add header and footer
            writer.setPageEvent(new HeaderFooterPageEvent("Fruitables - Comprehensive Inventory Report"));
            
            document.open();
            
            // Add logo and title section
            addLogoAndTitle(document, "Fruitables Comprehensive Inventory Report");
            
            // Add report timestamp with icon
            addReportTimestamp(document);
            
            // Add comprehensive summary information
            addComprehensiveSummary(document, products);
            
            // Add detailed inventory table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(15f);
            table.setSpacingAfter(15f);
            
            // Set column widths
            float[] columnWidths = {1.2f, 2.5f, 1.5f, 1.5f, 1.5f, 1.5f};
            table.setWidths(columnWidths);
            
            // Add table headers
            addTableHeader(table, new String[]{"ID", "Product", "Category", "Price ($)", "Weight (kg)", "Quantity"});
            
            // Add table data with alternating row colors
            double totalValue = 0.0;
            boolean alternateRow = false;
            for (Product product : products) {
                addTableRow(table, alternateRow,
                    String.valueOf(product.getId()),
                    product.getCategory(),
                    product.getCategory(),
                    String.format("%.2f", product.getPrice()),
                    String.format("%.2f", product.getWeight()),
                    String.valueOf(product.getQuantity()));
                
                totalValue += product.getPrice() * product.getQuantity();
                alternateRow = !alternateRow;
            }
            
            document.add(table);
            
            // Add price analysis section
            addPriceAnalysis(document, products);
            
            // Add stock analysis section
            addStockAnalysis(document, products);
            
            // Add category analysis section
            addCategoryAnalysis(document, products);
            
            // Add total value with enhanced styling
            addTotalSection(document, "Total Inventory Value:", totalValue);
            
            // Add notes section
            addNotesSection(document);
            
            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Export product sales report to PDF
     * 
     * @param products List of products to include in the report
     * @param startDate Start date for the report period
     * @param endDate End date for the report period
     * @param filePath Path where to save the PDF file
     * @return True if export was successful, false otherwise
     */
    public static boolean exportSalesReport(List<Product> products, Date startDate, Date endDate, String filePath) {
        try {
            Document document = new Document(PageSize.A4, 36, 36, 60, 36);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            
            // Add header and footer
            writer.setPageEvent(new HeaderFooterPageEvent("Fruitables - Sales Report"));
            
            document.open();
            
            // Add logo and title
            addLogoAndTitle(document, "Fruitables Sales Report");
            
            // Add date range with enhanced styling
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Paragraph dateParagraph = new Paragraph();
            dateParagraph.setAlignment(Element.ALIGN_CENTER);
            dateParagraph.setSpacingAfter(5f);
            
            Chunk dateRangeLabel = new Chunk("Period: ", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, SECONDARY_COLOR));
            Chunk dateRange = new Chunk(dateFormat.format(startDate) + " to " + dateFormat.format(endDate), SUBTITLE_FONT);
            dateParagraph.add(dateRangeLabel);
            dateParagraph.add(dateRange);
            document.add(dateParagraph);
            
            // Add report timestamp
            addReportTimestamp(document);
            
            // Add table with sales data
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(15f);
            
            // Set column widths
            float[] columnWidths = {1.2f, 3f, 1.5f, 1.5f, 2f};
            table.setWidths(columnWidths);
            
            // Add table headers
            addTableHeader(table, new String[]{"ID", "Product", "Units Sold", "Unit Price ($)", "Total Revenue ($)"});
            
            // Add dummy sales data with alternating row colors
            double totalRevenue = 0.0;
            boolean alternateRow = false;
            for (Product product : products) {
                // Dummy sales numbers (would come from real sales data)
                int unitsSold = (int) (Math.random() * 100);
                double revenue = unitsSold * product.getPrice();
                
                addTableRow(table, alternateRow,
                    String.valueOf(product.getId()),
                    product.getCategory(),
                    String.valueOf(unitsSold),
                    String.format("%.2f", product.getPrice()),
                    String.format("%.2f", revenue));
                
                totalRevenue += revenue;
                alternateRow = !alternateRow;
            }
            
            document.add(table);
            
            // Add total revenue with enhanced styling
            addTotalSection(document, "Total Revenue:", totalRevenue);
            
            // Add notes section
            addNotesSection(document);
            
            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Add a logo and title section to the document
     */
    private static void addLogoAndTitle(Document document, String title) throws DocumentException, IOException {
        // Create a table for logo and title to align them side by side
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1f, 4f});
        headerTable.setSpacingAfter(15f);
        
        // Logo cell (left side)
        Image logoImage = Image.getInstance(createLogoPlaceholder());
        logoImage.scaleToFit(70, 70);
        PdfPCell logoCell = new PdfPCell(logoImage);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPaddingRight(10f);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        // Title cell (right side)
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        Paragraph titleParagraph = new Paragraph(title, TITLE_FONT);
        titleParagraph.setAlignment(Element.ALIGN_LEFT);
        titleCell.addElement(titleParagraph);
        
        headerTable.addCell(logoCell);
        headerTable.addCell(titleCell);
        document.add(headerTable);
        
        // Add a decorative line after title
        LineSeparator line = new LineSeparator();
        line.setLineColor(PRIMARY_COLOR);
        line.setLineWidth(2);
        line.setPercentage(100f);
        line.setAlignment(Element.ALIGN_CENTER);
        document.add(new Chunk(line));
    }
    
    /**
     * Create a placeholder for the logo (would be replaced with actual logo in production)
     */
    private static Image createLogoPlaceholder() throws DocumentException, IOException {
        // In a production app, you would load an actual logo file
        // This is creating a very basic image as a placeholder
        
        // Create a BufferedImage for the logo
        java.awt.image.BufferedImage awtImage = new java.awt.image.BufferedImage(
            60, 60, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = awtImage.createGraphics();
        
        // Draw green background
        g2d.setColor(new java.awt.Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue()));
        g2d.fillRect(0, 0, 60, 60);
        
        // Draw "F" letter
        g2d.setColor(java.awt.Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString("F", (60 - fm.stringWidth("F")) / 2, 
                      ((60 - fm.getHeight()) / 2) + fm.getAscent());
        g2d.dispose();
        
        // Convert to iText Image
        return Image.getInstance(awtImage, null);
    }
    
    /**
     * Add report timestamp to the document
     */
    private static void addReportTimestamp(Document document) throws DocumentException {
        Paragraph dateParagraph = new Paragraph();
        dateParagraph.setAlignment(Element.ALIGN_CENTER);
        dateParagraph.setSpacingAfter(15f);
        
        Chunk generatedLabel = new Chunk("Generated on: ", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, SECONDARY_COLOR));
        Chunk dateTimeValue = new Chunk(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 
                              new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(52, 73, 94)));
        dateParagraph.add(generatedLabel);
        dateParagraph.add(dateTimeValue);
        document.add(dateParagraph);
    }
    
    /**
     * Add comprehensive summary information to the document
     */
    private static void addComprehensiveSummary(Document document, List<Product> products) throws DocumentException {
        int totalProducts = products.size();
        int totalStock = products.stream().mapToInt(Product::getQuantity).sum();
        double totalWeight = products.stream().mapToDouble(p -> p.getWeight() * p.getQuantity()).sum();
        double totalValue = products.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
        
        // Calculate additional statistics
        double minPrice = products.stream().mapToDouble(Product::getPrice).min().orElse(0);
        double maxPrice = products.stream().mapToDouble(Product::getPrice).max().orElse(0);
        double avgPrice = products.stream().mapToDouble(Product::getPrice).average().orElse(0);
        double avgWeight = products.stream().mapToDouble(Product::getWeight).average().orElse(0);
        int lowStockItems = (int) products.stream().filter(p -> p.getQuantity() < 10).count();
        int outOfStockItems = (int) products.stream().filter(p -> p.getQuantity() == 0).count();
        
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(80);
        summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.setSpacingBefore(15f);
        summaryTable.setSpacingAfter(15f);
        
        // Summary table title with enhanced styling
        Paragraph summaryTitle = new Paragraph("Comprehensive Inventory Summary", 
                                 new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, PRIMARY_COLOR));
        summaryTitle.setAlignment(Element.ALIGN_CENTER);
        summaryTitle.setSpacingAfter(10f);
        document.add(summaryTitle);
        
        // Style for summary table header
        PdfPCell headerCell = new PdfPCell(new Phrase("Key Performance Indicators", HEADER_FONT));
        headerCell.setBackgroundColor(PRIMARY_COLOR);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8f);
        headerCell.setColspan(2);
        summaryTable.addCell(headerCell);
        
        // Add summary rows with improved styling
        addSummaryRow(summaryTable, "Total Products", String.valueOf(totalProducts));
        addSummaryRow(summaryTable, "Total Items in Stock", String.valueOf(totalStock));
        addSummaryRow(summaryTable, "Total Weight (kg)", String.format("%.2f", totalWeight));
        addSummaryRow(summaryTable, "Total Inventory Value ($)", String.format("%.2f", totalValue));
        addSummaryRow(summaryTable, "Average Price ($)", String.format("%.2f", avgPrice));
        addSummaryRow(summaryTable, "Price Range ($)", String.format("%.2f - %.2f", minPrice, maxPrice));
        addSummaryRow(summaryTable, "Average Weight (kg)", String.format("%.2f", avgWeight));
        addSummaryRow(summaryTable, "Low Stock Items (<10)", String.valueOf(lowStockItems));
        addSummaryRow(summaryTable, "Out of Stock Items", String.valueOf(outOfStockItems));
        
        document.add(summaryTable);
    }
    
    private static void addPriceAnalysis(Document document, List<Product> products) throws DocumentException {
        // Price analysis title
        Paragraph priceTitle = new Paragraph("Price Analysis", 
                                 new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, SECONDARY_COLOR));
        priceTitle.setAlignment(Element.ALIGN_CENTER);
        priceTitle.setSpacingBefore(20f);
        priceTitle.setSpacingAfter(10f);
        document.add(priceTitle);
        
        // Create price analysis table
        PdfPTable priceTable = new PdfPTable(2);
        priceTable.setWidthPercentage(60);
        priceTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        priceTable.setSpacingBefore(10f);
        priceTable.setSpacingAfter(15f);
        
        // Add table header
        PdfPCell headerCell = new PdfPCell(new Phrase("Price Statistics", HEADER_FONT));
        headerCell.setBackgroundColor(SECONDARY_COLOR);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8f);
        headerCell.setColspan(2);
        priceTable.addCell(headerCell);
        
        // Calculate price statistics
        double minPrice = products.stream().mapToDouble(Product::getPrice).min().orElse(0);
        double maxPrice = products.stream().mapToDouble(Product::getPrice).max().orElse(0);
        double avgPrice = products.stream().mapToDouble(Product::getPrice).average().orElse(0);
        double medianPrice = products.stream()
            .mapToDouble(Product::getPrice)
            .sorted()
            .skip(products.size() / 2)
            .findFirst()
            .orElse(0);
        
        // Add price statistics
        addSummaryRow(priceTable, "Minimum Price ($)", String.format("%.2f", minPrice));
        addSummaryRow(priceTable, "Maximum Price ($)", String.format("%.2f", maxPrice));
        addSummaryRow(priceTable, "Average Price ($)", String.format("%.2f", avgPrice));
        addSummaryRow(priceTable, "Median Price ($)", String.format("%.2f", medianPrice));
        
        document.add(priceTable);
    }
    
    private static void addStockAnalysis(Document document, List<Product> products) throws DocumentException {
        // Stock analysis title
        Paragraph stockTitle = new Paragraph("Stock Level Analysis", 
                                 new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, SECONDARY_COLOR));
        stockTitle.setAlignment(Element.ALIGN_CENTER);
        stockTitle.setSpacingBefore(20f);
        stockTitle.setSpacingAfter(10f);
        document.add(stockTitle);
        
        // Create stock analysis table
        PdfPTable stockTable = new PdfPTable(2);
        stockTable.setWidthPercentage(60);
        stockTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        stockTable.setSpacingBefore(10f);
        stockTable.setSpacingAfter(15f);
        
        // Add table header
        PdfPCell headerCell = new PdfPCell(new Phrase("Stock Statistics", HEADER_FONT));
        headerCell.setBackgroundColor(SECONDARY_COLOR);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8f);
        headerCell.setColspan(2);
        stockTable.addCell(headerCell);
        
        // Calculate stock statistics
        int totalStock = products.stream().mapToInt(Product::getQuantity).sum();
        int lowStockItems = (int) products.stream().filter(p -> p.getQuantity() < 10).count();
        int outOfStockItems = (int) products.stream().filter(p -> p.getQuantity() == 0).count();
        double avgStock = products.stream().mapToInt(Product::getQuantity).average().orElse(0);
        
        // Add stock statistics
        addSummaryRow(stockTable, "Total Items in Stock", String.valueOf(totalStock));
        addSummaryRow(stockTable, "Average Stock per Item", String.format("%.2f", avgStock));
        addSummaryRow(stockTable, "Low Stock Items (<10)", String.valueOf(lowStockItems));
        addSummaryRow(stockTable, "Out of Stock Items", String.valueOf(outOfStockItems));
        
        document.add(stockTable);
    }
    
    private static void addCategoryAnalysis(Document document, List<Product> products) throws DocumentException {
        // Category analysis title
        Paragraph categoryTitle = new Paragraph("Category Analysis", 
                                 new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, SECONDARY_COLOR));
        categoryTitle.setAlignment(Element.ALIGN_CENTER);
        categoryTitle.setSpacingBefore(20f);
        categoryTitle.setSpacingAfter(10f);
        document.add(categoryTitle);
        
        // Create category analysis table
        PdfPTable categoryTable = new PdfPTable(3);
        categoryTable.setWidthPercentage(80);
        categoryTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        categoryTable.setSpacingBefore(10f);
        categoryTable.setSpacingAfter(15f);
        
        // Add table header
        PdfPCell headerCell = new PdfPCell(new Phrase("Category Statistics", HEADER_FONT));
        headerCell.setBackgroundColor(SECONDARY_COLOR);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8f);
        headerCell.setColspan(3);
        categoryTable.addCell(headerCell);
        
        // Add column headers
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
    
    /**
     * Add a row to the summary table with consistent styling
     */
    private static void addSummaryRow(PdfPTable table, String label, String value) {
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(52, 73, 94));
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, new BaseColor(44, 62, 80));
        
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(LIGHT_GRAY);
        labelCell.setPadding(8f);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8f);
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(valueCell);
    }
    
    /**
     * Add header row to the table
     */
    private static void addTableHeader(PdfPTable table, String[] headers) {
        // Table title cell
        PdfPCell titleCell = new PdfPCell(new Phrase("Detailed Information", HEADER_FONT));
        titleCell.setBackgroundColor(SECONDARY_COLOR);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setPadding(10);
        titleCell.setColspan(headers.length);
        table.addCell(titleCell);
        
        // Column headers
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
    }
    
    /**
     * Add a row to the data table with alternating row colors
     */
    private static void addTableRow(PdfPTable table, boolean alternateRow, String... values) {
        BaseColor rowColor = alternateRow ? LIGHT_GRAY : BaseColor.WHITE;
        
        for (int i = 0; i < values.length; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(values[i], NORMAL_FONT));
            cell.setBackgroundColor(rowColor);
            
            // Center alignment for numeric columns
            if (i > 1) {
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            } else {
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            }
            
            cell.setPadding(7);
            cell.setBorderWidth(0.5f);
            cell.setBorderColor(new BaseColor(220, 220, 220));
            table.addCell(cell);
        }
    }
    
    /**
     * Add total section with enhanced styling
     */
    private static void addTotalSection(Document document, String label, double value) throws DocumentException {
        // Create a table for better alignment
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(40);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.setSpacingBefore(10f);
        totalTable.setSpacingAfter(15f);
        
        // Total label
        PdfPCell labelCell = new PdfPCell(new Phrase(label, TOTAL_FONT));
        labelCell.setBorder(Rectangle.TOP);
        labelCell.setBorderColorTop(PRIMARY_COLOR);
        labelCell.setBorderWidthTop(2f);
        labelCell.setPadding(8f);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        // Total value
        PdfPCell valueCell = new PdfPCell(new Phrase("$" + String.format("%.2f", value), TOTAL_FONT));
        valueCell.setBorder(Rectangle.TOP);
        valueCell.setBorderColorTop(PRIMARY_COLOR);
        valueCell.setBorderWidthTop(2f);
        valueCell.setPadding(8f);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        totalTable.addCell(labelCell);
        totalTable.addCell(valueCell);
        document.add(totalTable);
    }
    
    /**
     * Add a notes section to the document
     */
    private static void addNotesSection(Document document) throws DocumentException {
        // Add a separator
        LineSeparator line = new LineSeparator();
        line.setLineColor(new BaseColor(200, 200, 200));
        line.setPercentage(100f);
        line.setAlignment(Element.ALIGN_CENTER);
        document.add(new Chunk(line));
        
        // Notes title
        Paragraph notesTitle = new Paragraph("Notes", 
                               new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, SECONDARY_COLOR));
        notesTitle.setSpacingBefore(15f);
        notesTitle.setSpacingAfter(5f);
        document.add(notesTitle);
        
        // Notes content
        Paragraph notesContent = new Paragraph(
            "This report was automatically generated by the Fruitables Management System. " +
            "For any questions or issues, please contact system administrator.",
            new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, new BaseColor(100, 100, 100))
        );
        document.add(notesContent);
    }
    
    /**
     * Page event handler for adding headers and footers
     */
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        private String title;
        
        public HeaderFooterPageEvent(String title) {
            this.title = title;
        }
        
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            
            // Add stylish footer with page number
            Rectangle rect = document.getPageSize();
            float footerY = rect.getBottom() + 30;
            
            // Add footer line
            cb.setColorStroke(new BaseColor(220, 220, 220));
            cb.moveTo(rect.getLeft() + 30, footerY + 15);
            cb.lineTo(rect.getRight() - 30, footerY + 15);
            cb.stroke();
            
            try {
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                cb.setFontAndSize(bf, 9);
                cb.setColorFill(new BaseColor(100, 100, 100));
                
                // Add Page number with stylish format
                String pageText = "Page " + writer.getPageNumber();
                cb.beginText();
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, pageText, (rect.getRight() + rect.getLeft()) / 2, footerY, 0);
                cb.endText();
                
                // Add footer text
                cb.beginText();
                cb.showTextAligned(PdfContentByte.ALIGN_LEFT, title, rect.getLeft() + 36, footerY, 0);
                cb.endText();
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                cb.beginText();
                cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, "Generated: " + sdf.format(new Date()), rect.getRight() - 36, footerY, 0);
                cb.endText();
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }
    }
} 