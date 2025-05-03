package services;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class VerificationService {
    private static final int CODE_EXPIRY_MINUTES = 15;
    private static final Map<String, VerificationAttempt> attempts = new ConcurrentHashMap<>();

    public static String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public static void trackAttempt(String email, String code) {
        attempts.put(email, new VerificationAttempt(code, LocalDateTime.now()));
    }

    public static boolean verifyCode(String email, String enteredCode) {
        VerificationAttempt attempt = attempts.get(email);
        if (attempt == null) return false;

        boolean isValid = attempt.code.equals(enteredCode) &&
                !attempt.isExpired();

        if (isValid) {
            attempts.remove(email);
        }
        return isValid;
    }

    public static boolean canResend(String email) {
        VerificationAttempt attempt = attempts.get(email);
        return attempt == null || attempt.isExpired();
    }

    private static class VerificationAttempt {
        String code;
        LocalDateTime sentAt;

        VerificationAttempt(String code, LocalDateTime sentAt) {
            this.code = code;
            this.sentAt = sentAt;
        }

        boolean isExpired() {
            return sentAt.plusMinutes(CODE_EXPIRY_MINUTES).isBefore(LocalDateTime.now());
        }
    }
}