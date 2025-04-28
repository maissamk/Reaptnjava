package utils;

import Models.user;
import javafx.animation.Timeline;
import services.UserServices;
import java.io.*;
import java.util.Properties;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class SessionManager {
    private static SessionManager instance;
    private user currentUser;
    private static final String SESSION_FILE = "session.properties";
    private static final String ENCRYPTION_KEY = "your-strong-secret-key-123!";
    private final EventDispatcher eventDispatcher;
    private final UserServices userService;
    private Timeline statusChecker;

    private SessionManager() {
        this.eventDispatcher = new EventDispatcher();
        this.userService = new UserServices();
        setupStatusListeners();
        setupStatusChecker();
        loadSession();
    }
    private void setupStatusChecker() {
        statusChecker = new Timeline(
                new KeyFrame(Duration.seconds(30), event -> checkAccountStatus())
        );
        statusChecker.setCycleCount(Timeline.INDEFINITE);
        statusChecker.play();
    }

    public void checkAccountStatus() {
        if (currentUser != null) {
            user updatedUser = userService.getUserById(currentUser.getId());
            if (updatedUser != null && "Blocked".equalsIgnoreCase(updatedUser.getStatus())) {
                logout();
                Platform.runLater(() -> {
                    showAlert("Account Blocked",
                            "Your account has been blocked",
                            "You have been logged out of the system");
                    NavigationUtil.navigateTo("/FrontOffice/Login.fxml", null);
                });
            }
        }
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }


    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }


    private void setupStatusListeners() {
        // Database status updater
        eventDispatcher.addListener("user_status_changed", event -> {
            UserStatusService.StatusChangeEventData data =
                    (UserStatusService.StatusChangeEventData) event.getData();

            if (currentUser != null) {
                currentUser.setStatus(data.getNewStatus().name());
                userService.update(currentUser);
            }
        });

        // Notification listener
        eventDispatcher.addListener("user_logged_in", event -> {
            String email = (String) event.getData();
            System.out.println("User logged in: " + email);
            // Add any notification logic here
        });

        eventDispatcher.addListener("user_logged_out", event -> {
            String email = (String) event.getData();
            System.out.println("User logged out: " + email);
            // Add any notification logic here
        });
    }

    public void startSession(user user) {
        if (user == null || "Blocked".equalsIgnoreCase(user.getStatus())) {
            System.out.println("Attempt to start session with blocked/null user");
            return;
        }

        this.currentUser = user;
        updateUserStatus(UserStatusService.UserStatus.ACTIVE);
        saveSession();
    }
    private void showAlert(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }



    public boolean isAccountActive(user user) {
        return user != null && !"Blocked".equalsIgnoreCase(user.getStatus());
    }

    public void logout() {
        if (currentUser != null) {
            updateUserStatus(UserStatusService.UserStatus.INACTIVE);
            eventDispatcher.dispatch(new Event("user_logged_out", currentUser.getEmail()));
            this.currentUser = null;
            clearSession();
        }
    }

    private void updateUserStatus(UserStatusService.UserStatus status) {
        if (currentUser != null) {
            currentUser.setStatus(status.name());
            eventDispatcher.dispatch(new Event(
                    "user_status_changed",
                    new UserStatusService.StatusChangeEventData(status, currentUser.getId()) // Add user ID here
            ));
        }
    }

    // Your existing session persistence methods
    public void loadSession() {
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            try (InputStream input = new FileInputStream(SESSION_FILE)) {
                Properties prop = new Properties();
                prop.load(input);

                if (prop.containsKey("userId")) {
                    String encryptedId = prop.getProperty("userId");
                    int userId = Integer.parseInt(decrypt(encryptedId));

                    this.currentUser = userService.getUserById(userId);
                    if (this.currentUser != null) {
                        updateUserStatus(UserStatusService.UserStatus.ACTIVE);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error loading session: " + ex.getMessage());
                clearSession();
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

    // Simple XOR encryption
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

    // Your existing utility methods
    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRoles().contains(role);
    }

    public boolean isOwner(int userId) {
        return currentUser != null && currentUser.getId() == userId;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public user getCurrentUser() {
        return currentUser;
    }
    public boolean canUserLogin(user user) {
        return user != null && !UserStatusService.UserStatus.BLOQUE.toString().equals(user.getStatus());
    }
    private static user pendingUser;

    public static void setPendingUser(user user) {
        pendingUser = user;
    }

    public static user getPendingUser() {
        return pendingUser;
    }

    public static void clearPendingUser() {
        pendingUser = null;
    }


}