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
import models.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
                LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                        .setPort(LOCAL_RECEIVER_PORT)
                        .setCallbackPath("/Callback")
                        .build();

                try {
                    // Open browser for user to select account
                    String authorizationUrl = flow.newAuthorizationUrl()
                            .setRedirectUri(receiver.getRedirectUri())
                            .build();

                    System.out.println("Opening browser to: " + authorizationUrl);
                    openBrowser(authorizationUrl);

                    // Wait for authorization code
                    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

                    if (credential == null) {
                        throw new RuntimeException("Failed to obtain credentials");
                    }

                    // Get user info
                    Oauth2 oauth2 = new Oauth2.Builder(flow.getTransport(), JSON_FACTORY, credential)
                            .setApplicationName("YourAppName")
                            .build();

                    Userinfo userInfo = oauth2.userinfo().get().execute();
                    return createOrUpdateUser(userInfo);
                } finally {
                    receiver.stop();
                }
            }
        };
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