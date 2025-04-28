package utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailSender {
    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 587;
    private static final String USERNAME = "romdhani.nour789@gmail.com"; // Your Gmail
    private static final String PASSWORD = "kzba vzlg itxm orok"; // Replace with new app password

    public static void sendVerificationEmail(String recipientEmail, String code) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // Trust Gmail's certificate

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD); // Use your credentials
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME)); // Send from your Gmail
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject("Verify Your Account");
            message.setText("Your code: " + code);

            Transport.send(message);
        } catch (AuthenticationFailedException e) {
            throw new MessagingException("Gmail authentication failed. Check app password.", e);
        }
    }
    public static void sendPasswordResetEmail(String recipientEmail, String verificationCode) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("Password Reset Verification Code");

        String emailContent = "Your password reset verification code is: " + verificationCode +
                "\n\nEnter this code in the application to reset your password." +
                "\n\nThis code will expire in 24 hours." +
                "\n\nIf you didn't request this, please ignore this email.";

        message.setText(emailContent);
        Transport.send(message);
    }
}