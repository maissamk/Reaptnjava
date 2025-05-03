package services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;


import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import javafx.concurrent.Task;
import Models.user;

import java.io.*;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class GoogleAuthGIS {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(
            "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile");
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final int LOCAL_RECEIVER_PORT = 8888;

    private final GoogleAuthorizationCodeFlow flow;

    public GoogleAuthGIS() throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Load client secrets
        InputStream in = GoogleAuthGIS.class.getResourceAsStream("/client_secret.json");
        if (in == null) {
            throw new FileNotFoundException("client_secret.json not found in resources");
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        this.flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }

    public Task<user> authenticate() {
        return new Task<user>() {
            @Override
            protected user call() throws Exception {
                // Try multiple ports in sequence
                for (int port : new int[]{8888, 8889, 8890, 0}) { // 0 = random available port
                    LocalServerReceiver receiver = null;
                    try {
                        // First check if port is available
                        if (port != 0 && !isPortAvailable(port)) {
                            System.out.println("Port " + port + " is in use, skipping...");
                            continue;
                        }

                        receiver = new LocalServerReceiver.Builder()
                                .setPort(port)
                                .setCallbackPath("/Callback")
                                .build();

                        System.out.println("Attempting OAuth flow on port: " + port);

                        // Only open browser for the first available port
                        if (port == 8888 || port == 0) {
                            String authorizationUrl = flow.newAuthorizationUrl()
                                    .setRedirectUri(receiver.getRedirectUri())
                                    .set("prompt", "select_account")

                                    .build();
                            openBrowser(authorizationUrl);
                        }

                        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                                .authorize("user");

                        // Success case
                        Oauth2 oauth2 = new Oauth2.Builder(flow.getTransport(), JSON_FACTORY, credential)
                                .setApplicationName("YourAppName")
                                .build();

                        Userinfo userInfo = oauth2.userinfo().get().execute();
                        return createOrUpdateUser(userInfo);

                    } catch (IOException e) {
                        if (e.getMessage().contains("Address already in use")) {
                            System.out.println("Port " + port + " failed: " + e.getMessage());
                            continue; // Try next port
                        }
                        throw e; // Re-throw other exceptions
                    } finally {
                        if (receiver != null) {
                            receiver.stop();
                        }
                    }
                }
                throw new RuntimeException("Failed to find available port for OAuth flow");
            }
        };
    }

    // Helper method to check port availability
    private boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    private void openBrowser(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            System.err.println("Failed to open browser: " + e.getMessage());
        }
    }

    private user createOrUpdateUser(Userinfo userInfo) throws SQLException {
        UserServices userService = new UserServices();
        user existingUser = userService.getUserByEmail(userInfo.getEmail());

        if (existingUser != null) {
            // Update existing user
            existingUser.setNom(userInfo.getFamilyName());
            existingUser.setPrenom(userInfo.getGivenName());
            existingUser.setAvatar(userInfo.getPicture());
            existingUser.setStatus("Active");
            userService.update(existingUser);
            return existingUser;
        } else {
            // Create new user
            user newUser = new user();
            newUser.setEmail(userInfo.getEmail());
            newUser.setPassword(""); // Empty password for Google users
            newUser.setRoles("ROLE_USER");
            newUser.setNom(userInfo.getFamilyName());
            newUser.setPrenom(userInfo.getGivenName());
            newUser.setTelephone("");
            newUser.setStatus("Active");
            newUser.setAvatar(userInfo.getPicture());

            userService.add(newUser);
            return userService.getUserByEmail(userInfo.getEmail());
        }
    }
}