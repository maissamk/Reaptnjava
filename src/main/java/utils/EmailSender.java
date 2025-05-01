package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailSender {



    private static final String SMTP_HOST = "longevityplus.store";
    private static final int SMTP_PORT = 465;
    private static final String SMTP_USERNAME = "maissa@longevityplus.store";
    private static final String SMTP_PASSWORD = "MaissaMaissa1";

    private static final Logger logger = Logger.getLogger(EmailSender.class.getName());


    public static boolean sendMaterialAddedEmail(String recipientEmail, String materialType,
                                                 File pdfAttachment, String materialName) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.port", SMTP_PORT);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "true"); // Enable debug logging
            Session session = Session.getInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                        }
                    });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("New " + materialType + " Material Added: " + materialName);

            Multipart multipart = new MimeMultipart();

            // HTML content
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(buildMaterialEmailContent(materialType, materialName),
                    "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // PDF attachment
            if (pdfAttachment != null && pdfAttachment.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(pdfAttachment);
                attachmentPart.setFileName(materialType + "-details.pdf");
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to send material email", e);
            return false;
        } finally {
            if (pdfAttachment != null && pdfAttachment.exists()) {
                pdfAttachment.delete();
            }
        }
    }

    private static String buildMaterialEmailContent(String materialType, String materialName) {
        String color = materialType.equals("Location") ? "#2196F3" : "#4CAF50";

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <title>New Material Added</title>" +
                "  <style>" +
                "    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "    .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "    .header { background-color: " + color + "; padding: 20px; text-align: center; color: white; }" +
                "    .content { padding: 20px; background-color: #f9f9f9; }" +
                "    .footer { text-align: center; padding: 10px; font-size: 12px; color: #777; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class='container'>" +
                "    <div class='header'>" +
                "      <h1>New " + materialType + " Material Added</h1>" +
                "    </div>" +
                "    <div class='content'>" +
                "      <p>Hello,</p>" +
                "      <p>A new " + materialType.toLowerCase() + " material <strong>" + materialName + "</strong> has been added to the system.</p>" +
                "      <p>Please find attached the complete details in PDF format.</p>" +
                "      <p>Best regards,<br>The Materials Management Team</p>" +
                "    </div>" +
                "    <div class='footer'>" +
                "      <p>© " + java.time.Year.now().getValue() + " Your Company. All rights reserved.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }
}
