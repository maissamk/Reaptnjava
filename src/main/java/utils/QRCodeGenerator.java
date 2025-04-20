package utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import models.Product;
import models.ProductType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    /**
     * Generates a QR code containing product information
     * 
     * @param product The product to generate QR code for
     * @param size The size of the QR code (width and height)
     * @return JavaFX Image containing the QR code
     */
    public static Image generateQRCodeForProduct(Product product, int size) {
        try {
            String productInfo = buildProductInfoString(product);
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(productInfo, BarcodeFormat.QR_CODE, size, size, hints);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return new Image(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (Exception e) {
            System.out.println("Error generating QR code: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static String buildProductInfoString(Product product) {
        StringBuilder info = new StringBuilder();
        
        info.append("Product: ").append(product.getCategory()).append("\n");
        info.append("ID: ").append(product.getId()).append("\n");
        info.append("Price: $").append(String.format("%.2f", product.getPrice())).append("\n");
        info.append("Weight: ").append(product.getWeight()).append(" kg\n");
        info.append("Available: ").append(product.getQuantity()).append("\n");
        
        ProductType type = product.getType();
        if (type != null) {
            info.append("\nProduct Details:\n");
            info.append("Season: ").append(type.getSeason()).append("\n");
            info.append("Production Method: ").append(type.getProductionMethod()).append("\n");
            
            if (type.getHarvestDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                info.append("Harvest Date: ").append(dateFormat.format(type.getHarvestDate())).append("\n");
            }
            
            info.append("Preservation Duration: ").append(type.getPreservationDuration()).append("\n");
        }
        
        return info.toString();
    }
} 