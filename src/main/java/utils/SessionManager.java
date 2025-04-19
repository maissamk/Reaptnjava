package utils;

import Models.user;
import services.UserServices;
import java.io.*;
import java.util.Properties;

public class SessionManager {
    private static SessionManager instance;
    private user currentUser;
    private static final String SESSION_FILE = "session.properties";
    private static final String ENCRYPTION_KEY = "your-strong-secret-key-123!"; // Change this!

    private SessionManager() {
        loadSession();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void startSession(user user) {
        this.currentUser = user;
        saveSession();
    }

    public user getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
        clearSession();
    }

    public void loadSession() {
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            try (InputStream input = new FileInputStream(SESSION_FILE)) {
                Properties prop = new Properties();
                prop.load(input);

                if (prop.containsKey("userId")) {
                    String encryptedId = prop.getProperty("userId");
                    int userId = Integer.parseInt(decrypt(encryptedId));

                    UserServices userService = new UserServices();
                    this.currentUser = userService.getUserById(userId);
                }
            } catch (Exception ex) {
                System.err.println("Error loading session: " + ex.getMessage());
                clearSession(); // Clear corrupted session
            }
        }
    }

    private void saveSession() {
        if (currentUser != null) {
            try (OutputStream output = new FileOutputStream(SESSION_FILE)) {
                Properties prop = new Properties();
                prop.setProperty("userId", encrypt(String.valueOf(currentUser.getId())));
                prop.setProperty("email", encrypt(currentUser.getEmail()));
                prop.setProperty("nom", encrypt(currentUser.getNom()));
                prop.setProperty("roles", encrypt(currentUser.getRoles()));

                prop.store(output, "User Session");
            } catch (Exception e) {
                System.err.println("Error saving session: " + e.getMessage());
            }
        }
    }

    private void clearSession() {
        new File(SESSION_FILE).delete();
    }

    // Simple XOR encryption (replace with AES for production)
    private String encrypt(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            result.append((char) (input.charAt(i) ^ ENCRYPTION_KEY.charAt(i % ENCRYPTION_KEY.length())));
        }
        return result.toString();
    }

    private String decrypt(String input) {
        return encrypt(input); // XOR is symmetric
    }

    // Your existing methods
    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRoles().contains(role);
    }

    public boolean isOwner(int userId) {
        return currentUser != null && currentUser.getId() == userId;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}