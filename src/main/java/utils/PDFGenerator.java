package utils;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import Models.MaterielLocation;
import Models.MaterielVente;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class PDFGenerator {

    // Font definitions
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font HIGHLIGHT_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(0, 102, 204));

    // Colors
    private static final BaseColor HEADER_BG_COLOR = new BaseColor(0, 102, 204);
    private static final BaseColor ROW_ALT_COLOR = new BaseColor(240, 240, 240);

    // Logo configuration
    private static final String LOGO_PATH = "src/main/resources/images/logo.png";
    private static final float LOGO_WIDTH = 100f;
    private static final float LOGO_HEIGHT = 50f;

    /**
     * Generates a PDF for sale material
     */
    public static File generateMaterialVentePDF(MaterielVente materiel) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 90, 36);
        File file = File.createTempFile("materiel-vente-", ".pdf");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            PdfWriter writer = PdfWriter.getInstance(document, fos);

            // Add header/footer
            writer.setPageEvent(new PdfHeaderFooter());

            document.open();

            // Add logo
            addLogo(document);

            // Add title
            addTitle(document, "MATERIAL SALE DETAILS");

            // Add material details
            addVenteDetails(document, materiel);

            // Add image if available
            addMaterialImage(document, materiel.getImage());

            // Add footer note
            addFooterNote(document);

            document.close();
        }
        return file;
    }

    /**
     * Generates a PDF for rental material
     */
    public static File generateMaterialLocationPDF(MaterielLocation materiel) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 90, 36);
        File file = File.createTempFile("materiel-location-", ".pdf");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            PdfWriter writer = PdfWriter.getInstance(document, fos);

            // Add header/footer
            writer.setPageEvent(new PdfHeaderFooter());

            document.open();

            // Add logo
            addLogo(document);

            // Add title
            addTitle(document, "MATERIAL RENTAL DETAILS");

            // Add material details
            addLocationDetails(document, materiel);

            // Add image if available
            addMaterialImage(document, materiel.getImage());

            // Add footer note
            addFooterNote(document);

            document.close();
        }
        return file;
    }

    private static void addLogo(Document document) throws DocumentException {
        try {
            Image logo = Image.getInstance(LOGO_PATH);
            logo.scaleToFit(LOGO_WIDTH, LOGO_HEIGHT);
            logo.setAbsolutePosition(36, document.getPageSize().getHeight() - 72);
            document.add(logo);
        } catch (Exception e) {
            // Logo not critical, so we can continue without it
            System.err.println("Logo not found at: " + LOGO_PATH);
        }
    }

    private static void addTitle(Document document, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, TITLE_FONT);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(20f);
        document.add(p);

        // Add current date
        Paragraph date = new Paragraph(
                "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                NORMAL_FONT
        );
        date.setAlignment(Element.ALIGN_CENTER);
        date.setSpacingAfter(30f);
        document.add(date);
    }

    private static void addVenteDetails(Document document, MaterielVente materiel) throws DocumentException {
        // Main details table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20f);
        table.setSpacingAfter(30f);

        // Table header
        addTableHeader(table, "PROPERTY", "VALUE");

        // Material details
        addDetailRow(table, "Name", materiel.getNom());
        addDetailRow(table, "Price", String.format("%.2f TND", materiel.getPrix()));
        addDetailRow(table, "Description", materiel.getDescription());
        addDetailRow(table, "Availability", materiel.isDisponibilite() ? "Available" : "Not Available");
        addDetailRow(table, "Category ID", materiel.getCategorie() != null ? materiel.getCategorieId().toString() : "N/A");


        document.add(table);
    }

    private static void addLocationDetails(Document document, MaterielLocation materiel) throws DocumentException {
        // Main details table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20f);
        table.setSpacingAfter(30f);

        // Table header
        addTableHeader(table, "PROPERTY", "VALUE");

        // Material details

        addDetailRow(table, "Name", materiel.getNom());
        addDetailRow(table, "Daily Price", String.format("%.2f TND/day", materiel.getPrix()));
        addDetailRow(table, "Description", materiel.getDescription());
        addDetailRow(table, "Availability", materiel.isDisponibilite() ? "Available" : "Not Available");
        addDetailRow(table, "Assigned User", materiel.getUserIdMaterielLocationId() != null ?
                materiel.getUserIdMaterielLocationId().toString() : "Not assigned");

        document.add(table);
    }

    private static void addMaterialImage(Document document, String imagePath) throws DocumentException {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Paragraph imageTitle = new Paragraph("Material Image:", SUBTITLE_FONT);
                imageTitle.setSpacingBefore(30f);
                imageTitle.setSpacingAfter(10f);
                document.add(imageTitle);

                Image image = Image.getInstance("file:src/main/resources/images_materiels/" + imagePath);
                image.scaleToFit(300, 300);
                image.setAlignment(Image.MIDDLE);
                document.add(image);
            } catch (Exception e) {
                Paragraph noImage = new Paragraph("Image not available", NORMAL_FONT);
                noImage.setSpacingBefore(30f);
                document.add(noImage);
            }
        }
    }

    private static void addFooterNote(Document document) throws DocumentException {
        Paragraph footer = new Paragraph(
                "This document was automatically generated by the Material Management System.\n" +
                        "For any questions, please contact support@yourcompany.com",
                new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY)
        );
        footer.setSpacingBefore(40f);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private static void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(HEADER_BG_COLOR);
            cell.setPadding(8);
            table.addCell(cell);
        }
    }

    private static void addDetailRow(PdfPTable table, String property, String value) {
        // Property cell
        PdfPCell propCell = new PdfPCell(new Phrase(property, HIGHLIGHT_FONT));
        propCell.setPadding(6);
        propCell.setBackgroundColor(ROW_ALT_COLOR);
        table.addCell(propCell);

        // Value cell
        PdfPCell valCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valCell.setPadding(6);
        table.addCell(valCell);
    }

    /**
     * Inner class for header and footer
     */
    private static class PdfHeaderFooter extends PdfPageEventHelper {
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                // Header
                PdfPTable header = new PdfPTable(1);
                header.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());

                Paragraph title = new Paragraph("MATERIAL MANAGEMENT SYSTEM",
                        new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.GRAY));
                title.setAlignment(Element.ALIGN_RIGHT);

                PdfPCell cell = new PdfPCell(title);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(10f);
                header.addCell(cell);

                header.writeSelectedRows(0, -1, document.leftMargin(),
                        document.getPageSize().getHeight() - 20, writer.getDirectContent());

                // Footer
                PdfPTable footer = new PdfPTable(1);
                footer.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());

                Paragraph pageNum = new Paragraph(
                        "Page " + writer.getPageNumber(),
                        new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY));
                pageNum.setAlignment(Element.ALIGN_CENTER);

                cell = new PdfPCell(pageNum);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingBottom(10f);
                footer.addCell(cell);

                footer.writeSelectedRows(0, -1, document.leftMargin(),
                        30, writer.getDirectContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}