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

/**
 * Utility class for exporting data to PDF files
 */
public class PDFExporter {
    
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font TOTAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
    
    /**
     * Export product inventory report to PDF
     * 
     * @param products List of products to export
     * @param filePath Path where to save the PDF file
     * @return True if export was successful, false otherwise
     */
    public static boolean exportProductInventory(List<Product> products, String filePath) {
        try {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            
            // Add header and footer
            writer.setPageEvent(new HeaderFooterPageEvent("Fruitables - Inventory Report"));
            
            document.open();
            
            // Add title
            addTitle(document, "Fruitables Inventory Report");
            addSubtitle(document, "Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            // Add summary information
            addSummaryInfo(document, products);
            
            // Add table with inventory data
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            
            // Set column widths
            float[] columnWidths = {1.5f, 3f, 1.5f, 1.5f, 1.5f};
            table.setWidths(columnWidths);
            
            // Add table headers
            addTableHeader(table, new String[]{"ID", "Product", "Price ($)", "Weight (kg)", "Quantity"});
            
            // Add table data
            double totalValue = 0.0;
            for (Product product : products) {
                PdfPCell idCell = new PdfPCell(new Phrase(String.valueOf(product.getId()), NORMAL_FONT));
                PdfPCell nameCell = new PdfPCell(new Phrase(product.getCategory(), NORMAL_FONT));
                PdfPCell priceCell = new PdfPCell(new Phrase(String.format("%.2f", product.getPrice()), NORMAL_FONT));
                PdfPCell weightCell = new PdfPCell(new Phrase(String.format("%.2f", product.getWeight()), NORMAL_FONT));
                PdfPCell quantityCell = new PdfPCell(new Phrase(String.valueOf(product.getQuantity()), NORMAL_FONT));
                
                idCell.setPadding(5);
                nameCell.setPadding(5);
                priceCell.setPadding(5);
                weightCell.setPadding(5);
                quantityCell.setPadding(5);
                
                table.addCell(idCell);
                table.addCell(nameCell);
                table.addCell(priceCell);
                table.addCell(weightCell);
                table.addCell(quantityCell);
                
                totalValue += product.getPrice() * product.getQuantity();
            }
            
            document.add(table);
            
            // Add total value
            Paragraph totalParagraph = new Paragraph("Total Inventory Value: $" + String.format("%.2f", totalValue), TOTAL_FONT);
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalParagraph);
            
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
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            
            // Add header and footer
            writer.setPageEvent(new HeaderFooterPageEvent("Fruitables - Sales Report"));
            
            document.open();
            
            // Add title
            addTitle(document, "Fruitables Sales Report");
            
            // Add date range
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            addSubtitle(document, "Period: " + dateFormat.format(startDate) + " to " + dateFormat.format(endDate));
            addSubtitle(document, "Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            // Add table with sales data
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            
            // Set column widths
            float[] columnWidths = {1.5f, 3f, 1.5f, 1.5f, 2f};
            table.setWidths(columnWidths);
            
            // Add table headers
            addTableHeader(table, new String[]{"ID", "Product", "Units Sold", "Unit Price ($)", "Total Revenue ($)"});
            
            // Add dummy sales data (in real app, this would come from sales records)
            double totalRevenue = 0.0;
            for (Product product : products) {
                // Dummy sales numbers (would come from real sales data)
                int unitsSold = (int) (Math.random() * 100);
                double revenue = unitsSold * product.getPrice();
                
                PdfPCell idCell = new PdfPCell(new Phrase(String.valueOf(product.getId()), NORMAL_FONT));
                PdfPCell nameCell = new PdfPCell(new Phrase(product.getCategory(), NORMAL_FONT));
                PdfPCell unitsSoldCell = new PdfPCell(new Phrase(String.valueOf(unitsSold), NORMAL_FONT));
                PdfPCell priceCell = new PdfPCell(new Phrase(String.format("%.2f", product.getPrice()), NORMAL_FONT));
                PdfPCell revenueCell = new PdfPCell(new Phrase(String.format("%.2f", revenue), NORMAL_FONT));
                
                idCell.setPadding(5);
                nameCell.setPadding(5);
                unitsSoldCell.setPadding(5);
                priceCell.setPadding(5);
                revenueCell.setPadding(5);
                
                table.addCell(idCell);
                table.addCell(nameCell);
                table.addCell(unitsSoldCell);
                table.addCell(priceCell);
                table.addCell(revenueCell);
                
                totalRevenue += revenue;
            }
            
            document.add(table);
            
            // Add total revenue
            Paragraph totalParagraph = new Paragraph("Total Revenue: $" + String.format("%.2f", totalRevenue), TOTAL_FONT);
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalParagraph);
            
            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Add a title to the document
     */
    private static void addTitle(Document document, String title) throws DocumentException {
        Paragraph titleParagraph = new Paragraph(title, TITLE_FONT);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.setSpacingAfter(10f);
        document.add(titleParagraph);
        
        // Add a line after title
        LineSeparator line = new LineSeparator();
        line.setPercentage(95f);
        line.setAlignment(Element.ALIGN_CENTER);
        document.add(new Chunk(line));
    }
    
    /**
     * Add a subtitle to the document
     */
    private static void addSubtitle(Document document, String subtitle) throws DocumentException {
        Paragraph subtitleParagraph = new Paragraph(subtitle, SUBTITLE_FONT);
        subtitleParagraph.setAlignment(Element.ALIGN_CENTER);
        subtitleParagraph.setSpacingAfter(10f);
        document.add(subtitleParagraph);
    }
    
    /**
     * Add summary information to the document
     */
    private static void addSummaryInfo(Document document, List<Product> products) throws DocumentException {
        int totalProducts = products.size();
        int totalStock = products.stream().mapToInt(Product::getQuantity).sum();
        
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.setSpacingBefore(15f);
        summaryTable.setSpacingAfter(15f);
        
        // Style for summary table
        PdfPCell headerCell = new PdfPCell(new Phrase("Summary", HEADER_FONT));
        headerCell.setBackgroundColor(new BaseColor(39, 174, 96));
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8f);
        headerCell.setColspan(2);
        summaryTable.addCell(headerCell);
        
        // Add summary rows
        PdfPCell labelCell = new PdfPCell(new Phrase("Total Products", NORMAL_FONT));
        labelCell.setPadding(5f);
        summaryTable.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(String.valueOf(totalProducts), NORMAL_FONT));
        valueCell.setPadding(5f);
        summaryTable.addCell(valueCell);
        
        labelCell = new PdfPCell(new Phrase("Total Items in Stock", NORMAL_FONT));
        labelCell.setPadding(5f);
        summaryTable.addCell(labelCell);
        
        valueCell = new PdfPCell(new Phrase(String.valueOf(totalStock), NORMAL_FONT));
        valueCell.setPadding(5f);
        summaryTable.addCell(valueCell);
        
        document.add(summaryTable);
    }
    
    /**
     * Add header row to the table
     */
    private static void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(new BaseColor(39, 174, 96));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
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
            
            // Add footer with page number
            Rectangle rect = document.getPageSize();
            cb.setRGBColorFill(100, 100, 100);
            
            try {
                BaseFont bf = BaseFont.createFont();
                cb.setFontAndSize(bf, 10);
                
                // Add Page number
                String pageText = "Page " + writer.getPageNumber();
                float textWidth = bf.getWidthPoint(pageText, 10);
                cb.beginText();
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, pageText, (rect.getRight() + rect.getLeft()) / 2, rect.getBottom() + 30, 0);
                cb.endText();
                
                // Add footer text
                cb.beginText();
                cb.showTextAligned(PdfContentByte.ALIGN_LEFT, title, rect.getLeft() + 36, rect.getBottom() + 30, 0);
                cb.endText();
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                cb.beginText();
                cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, "Generated: " + sdf.format(new Date()), rect.getRight() - 36, rect.getBottom() + 30, 0);
                cb.endText();
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }
    }
} 