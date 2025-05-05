package services;

import Models.user;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GoogleAuthService {
    private static final String CLIENT_ID = "868926917287-n1210nkaag30d91b8nu2176d96p4vi0e.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String TOKEN_SERVER_URL = "https://oauth2.googleapis.com/token";
    private static final String AUTHORIZATION_SERVER_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    private final UserServices userService = new UserServices();

    public Task<user> authenticate() {
        return new Task<user>() {
            @Override
            protected user call() throws Exception {
                try {
                    // 1. Use MemoryDataStoreFactory instead of FileDataStoreFactory to prevent credential persistence
                    AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                            BearerToken.authorizationHeaderAccessMethod(),
                            new NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            new GenericUrl(TOKEN_SERVER_URL),
                            new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
                            CLIENT_ID,
                            AUTHORIZATION_SERVER_URL)
                            .setScopes(SCOPES)
                            .setDataStoreFactory(new MemoryDataStoreFactory()) // Temporary in-memory storage
                            .build();

                    // 2. Force account selection by adding prompt=select_account
                    String authorizationUrl = flow.newAuthorizationUrl()
                            .setRedirectUri("http://localhost:8888/Callback")
                            .set("prompt", "select_account") // Force account selection
                            .build();

                    // 3. Create receiver with custom handling
                    LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                            .setPort(8888)
                            .build();

                    // 4. Execute with timeout
                    Credential credential = CompletableFuture.supplyAsync(() -> {
                        try {
                            // Manually open browser with authorization URL
                            java.awt.Desktop.getDesktop().browse(java.net.URI.create(authorizationUrl));

                            // Wait for authorization code
                            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
                        } catch (Exception e) {
                            throw new CompletionException("Authorization failed: " + e.getMessage(), e);
                        }
                    }).get(2, TimeUnit.MINUTES);

                    // 5. Get user info
                    user googleUser = getUserInfo(credential);
                    return processGoogleUser(googleUser);

                } catch (TimeoutException e) {
                    throw new IOException("Authentication timed out after 2 minutes");
                } catch (Exception e) {
                    throw new IOException("Google authentication failed: " + e.getMessage());
                }
            }
        };
    }


    public user getUserInfo(Credential credential) throws IOException {
        // Create request to Google's userinfo endpoint
        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(credential);
        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v1/userinfo?alt=json");
        HttpRequest request = requestFactory.buildGetRequest(url);

        // Execute the request and parse the response
        HttpResponse response = request.execute();
        String json = response.parseAsString();

        // Parse JSON into a user object using the simpler method
        return parseGoogleResponse(json);
    }

    private user parseGoogleResponse(String json) {
        // Simple JSON parsing
        String id = extractFromJson(json, "id");
        String email = extractFromJson(json, "email");
        String name = extractFromJson(json, "name");
        String givenName = extractFromJson(json, "given_name");
        String familyName = extractFromJson(json, "family_name");
        String picture = extractFromJson(json, "picture");

        user user = new user();
        user.setGoogleId(id);
        user.setEmail(email);
        user.setPrenom(givenName);
        user.setNom(familyName);
        user.setAvatar(picture);
        user.setPassword(""); // No password needed for Google users
        user.setTelephone("00000000"); // Default phone number
        user.setRoles("[\"ROLE_CLIENT\"]");
        user.setStatus("Active");
        user.setFace_token(""); // Initialize empty face token
        user.setVerificationCode(""); // Initialize empty verification code

        return user;
    }

    private user processGoogleUser(user googleUser) {
        user existingUser = userService.getUserByGoogleId(googleUser.getGoogleId());
        if (existingUser == null) {
            existingUser = userService.getUserByEmail(googleUser.getEmail());
        }

        if (existingUser != null) {
            // Update existing user with Google info
            existingUser.setGoogleId(googleUser.getGoogleId());
            existingUser.setAvatar(googleUser.getAvatar());
            userService.update(existingUser);
            return existingUser;
        } else {
            // Create new user
            userService.add(googleUser);
            return googleUser;
        }
    }

    private String extractFromJson(String json, String key) {
        // Simple extraction - consider using a proper JSON parser for production
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey) + searchKey.length();
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) endIndex = json.indexOf("}", startIndex);

        String value = json.substring(startIndex, endIndex).trim();
        return value.startsWith("\"") ? value.substring(1, value.length() - 1) : value;
    }

    private void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new IOException("Failed to delete " + file.getAbsolutePath());
                    }
                }
            }
        }
        if (!directory.delete()) {
            throw new IOException("Failed to delete " + directory.getAbsolutePath());
        }
    }
}