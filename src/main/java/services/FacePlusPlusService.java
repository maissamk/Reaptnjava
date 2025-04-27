package services;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;

public class FacePlusPlusService {
    private static final String API_KEY = "r4zeyVpJYyZgY8FZpBUPzomn_177H5Wm";
    private static final String API_SECRET = "aJxBvnCf820QWd9Hsppjmy-eI-Laq9uz";
    private static final String DETECT_URL = "https://api-us.faceplusplus.com/facepp/v3/detect";
    private static final String COMPARE_URL = "https://api-us.faceplusplus.com/facepp/v3/compare";

    private final OkHttpClient client = new OkHttpClient();

    public String detectFace(File imageFile) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api_key", API_KEY)
                .addFormDataPart("api_secret", API_SECRET)
                .addFormDataPart("return_landmark", "0")
                .addFormDataPart("return_attributes", "")
                .addFormDataPart("image_file", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/*")))
                .build();

        Request request = new Request.Builder()
                .url(DETECT_URL)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseString = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseString);

            if (jsonResponse.has("faces") && jsonResponse.getJSONArray("faces").length() > 0) {
                return jsonResponse.getJSONArray("faces").getJSONObject(0).getString("face_token");
            }
            return null;
        }
    }

    public boolean compareFaces(String faceToken1, String faceToken2) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        RequestBody body = new FormBody.Builder()
                .add("api_key", API_KEY)
                .add("api_secret", API_SECRET)
                .add("face_token1", faceToken1)
                .add("face_token2", faceToken2)
                .build();

        Request request = new Request.Builder()
                .url(COMPARE_URL)
                .post(body)
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            JSONObject jsonResponse = new JSONObject(response.body().string());
            return jsonResponse.has("confidence") &&
                    jsonResponse.getDouble("confidence") > 70.0;
        }
    }
}