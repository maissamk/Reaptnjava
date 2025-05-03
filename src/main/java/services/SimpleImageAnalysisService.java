package services;

import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SimpleImageAnalysisService {
    private static final String API_URL = "https://api-inference.huggingface.co/models/google/vit-base-patch16-224";
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey;

    public SimpleImageAnalysisService(String apiKey) {
        this.apiKey = apiKey;
    }

    public String classifyImage(File imageFile) throws IOException {
        // Verify image
        if (!imageFile.exists()) {
            throw new IOException("Image file does not exist");
        }

        // Read image bytes
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

        // Create request
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(imageBytes, MediaType.parse("image/png")))
                .build();

        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code() + " - " + response.body().string());
            }
            return response.body().string(); // Returns raw JSON
        }
    }
}