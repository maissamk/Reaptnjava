package utils;

import models.Stock;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles email communication using Mailtrap for testing
 */
public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    
    // Mailtrap credentials - Replace with your own Mailtrap credentials
    private static final String HOST = "sandbox.smtp.mailtrap.io";
    private static final int PORT = 2525;
    private static final String USERNAME = "58758cbf58c694"; // TODO: Replace with your Mailtrap username
    private static final String PASSWORD = "310f4b9ed48247"; // TODO: Replace with your Mailtrap password
    
    private static final String FROM_EMAIL = "fruitables@example.com";
    private static final String FROM_NAME = "Fruitables Stock System";
    
    /**
     * Send an email alert for low stock items
     * 
     * @param recipient Email address to receive the alert
     * @param lowStockItems List of stock items that are below minimum level
     * @return True if email was sent successfully, false otherwise
     */
    public boolean sendLowStockAlert(String recipient, List<Stock> lowStockItems) {
        try {
            // Set up mail server properties
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", HOST);
            properties.put("mail.smtp.port", PORT);
            
            // Create session with authenticator
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject("ALERT: Low Stock Items Detected");
            
            // Compose email body
            StringBuilder emailBody = new StringBuilder();
            emailBody.append("<html><body>");
            emailBody.append("<h2 style='color:#e74c3c;'>Low Stock Alert</h2>");
            emailBody.append("<p>The following items are below their minimum stock levels and require attention:</p>");
            
            // Add table with low stock items
            emailBody.append("<table border='1' cellpadding='5' style='border-collapse: collapse;'>");
            emailBody.append("<tr style='background-color:#f8f9fa;'>");
            emailBody.append("<th>Product</th>");
            emailBody.append("<th>Current Quantity</th>");
            emailBody.append("<th>Minimum Level</th>");
            emailBody.append("<th>Status</th>");
            emailBody.append("</tr>");
            
            for (Stock stock : lowStockItems) {
                // Calculate how critical the stock level is
                int currentQty = stock.getAvailableQuantity();
                float minQty = stock.getStockMinimum();
                String status;
                String rowColor;
                
                if (currentQty == 0) {
                    status = "OUT OF STOCK";
                    rowColor = "#ffcccc"; // Light red
                } else if (currentQty < minQty * 0.5) {
                    status = "CRITICAL";
                    rowColor = "#ffe6e6"; // Very light red
                } else {
                    status = "LOW";
                    rowColor = "#fff7e6"; // Light yellow
                }
                
                emailBody.append("<tr style='background-color:").append(rowColor).append(";'>");
                emailBody.append("<td>").append(stock.getProduct().getCategory()).append("</td>");
                emailBody.append("<td align='center'>").append(currentQty).append("</td>");
                emailBody.append("<td align='center'>").append((int)minQty).append("</td>");
                emailBody.append("<td align='center'><strong>").append(status).append("</strong></td>");
                emailBody.append("</tr>");
            }
            
            emailBody.append("</table>");
            
            // Add action instructions
            emailBody.append("<p style='margin-top:20px;'>Please restock these items at your earliest convenience.</p>");
            
            // Add footer
            emailBody.append("<p style='color:#7f8c8d; font-size:12px; margin-top:30px;'>");
            emailBody.append("This is an automated message from the Fruitables Inventory Management System.<br>");
            emailBody.append("Generated on: ").append(java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            emailBody.append("</p>");
            
            emailBody.append("</body></html>");
            
            // Set email content as HTML
            message.setContent(emailBody.toString(), "text/html; charset=utf-8");
            
            // Send message
            Transport.send(message);
            
            LOGGER.info("Low stock alert email sent successfully to: " + recipient);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send email: " + e.getMessage(), e);
            return false;
        }
    }
} 