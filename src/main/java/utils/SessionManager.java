package utils;

import Models.user;

public class SessionManager {
    private static SessionManager instance;
    private user currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void startSession(user user) {
        this.currentUser = user;
    }

    public user getCurrentUser() {
        return currentUser;
    }

    public void endSession() {
        this.currentUser = null;
    }

    // In SessionManager.java
    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRoles().contains(role);
    }

    public boolean isOwner(int userId) {
        return currentUser != null && currentUser.getId() == userId;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        endSession(); // This will set currentUser to null
    }
}