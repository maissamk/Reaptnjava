package utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    private static final int WORKLOAD = 12; // Cost factor

    public static boolean checkSymfonyPassword(String plainPassword, String symfonyHash) {
        String javaHash = symfonyHash.replaceFirst("\\$2y\\$", "\\$2a\\$");
        return BCrypt.checkpw(plainPassword, javaHash);
    }

    public static String hashForSymfony(String plainPassword) {
        String javaHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        return javaHash.replaceFirst("\\$2a\\$", "\\$2y\\$");
    }
}