package utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailSender {
    private static final Logger logger = Logger.getLogger(EmailSender.class.getName());

    // Configuration with fallback options
    private static final String[] SMTP_HOSTS = {"smtp.gmail.com", "smtp-relay.gmail.com"};
    private static final int[] PORTS = {587, 465}; // Trying both common ports
    private static final String USERNAME = "romdhani.nour789@gmail.com";
    private static final String PASSWORD = "kzba vzlg itxm orok";

    // Timeout settings (in milliseconds)
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int SOCKET_TIMEOUT = 10000;

    private static Session createSession(String host, int port) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(port == 587)); // Only for port 587
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // Timeout configurations
        props.put("mail.smtp.connectiontimeout", CONNECTION_TIMEOUT);
        props.put("mail.smtp.timeout", SOCKET_TIMEOUT);

        // SSL configurations for port 465
        if (port == 465) {
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        }

        // Security protocols
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", host);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    public static void sendEmailWithRetry(String recipientEmail, String subject, String content) throws MessagingException {
        MessagingException lastException = null;

        // Try all combinations of hosts and ports
        for (String host : SMTP_HOSTS) {
            for (int port : PORTS) {
                try {
                    Session session = createSession(host, port);
                    Message message = createEmailMessage(session, recipientEmail, subject, content);
                    Transport.send(message);
                    logger.info("Email sent successfully via " + host + ":" + port);
                    return; // Success - exit method
                } catch (MessagingException e) {
                    lastException = e;
                    logger.log(Level.WARNING, "Failed to send via " + host + ":" + port, e);
                    // Continue to try next combination
                }
            }
        }

        // If all attempts failed
        if (lastException != null) {
            logger.log(Level.SEVERE, "All email sending attempts failed");
            throw new MessagingException("Failed to send email after multiple attempts. Please check network and SMTP settings.", lastException);
        }
    }

    private static Message createEmailMessage(Session session, String recipientEmail, String subject, String content) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject(subject);

        // Create both HTML and plain text versions
        MimeMultipart multipart = new MimeMultipart("alternative");

        // Plain text version
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(content, "utf-8");

        // HTML version
        MimeBodyPart htmlPart = new MimeBodyPart();
        String htmlContent = "<html><body>" + content.replace("\n", "<br>") + "</body></html>";
        htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);
        message.setContent(multipart);

        return message;
    }

    public static void sendVerificationEmail(String recipientEmail, String code) throws MessagingException {
        String subject = "Verify Your Account";
        String content = "Your verification code is: " + code + "\n\n"
                + "This code will expire in 24 hours.";

        sendEmailWithRetry(recipientEmail, subject, content);
    }

    public static void sendPasswordResetEmail(String recipientEmail, String verificationCode) throws MessagingException {
        String subject = "Password Reset Verification Code";
        String content = "Your password reset verification code is: " + verificationCode + "\n\n"
                + "Enter this code in the application to reset your password.\n\n"
                + "This code will expire in 24 hours.\n\n"
                + "If you didn't request this, please ignore this email.";

        sendEmailWithRetry(recipientEmail, subject, content);
    }
}