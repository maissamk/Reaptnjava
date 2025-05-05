package controllers.FrontOffice.parcelles;

import org.json.JSONArray;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OSMGeocoder {
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";

    public static List<String> getLocationSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(NOMINATIM_URL + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&countrycodes=tn"))
                    .header("User-Agent", "Reaptn/1.0")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray results = new JSONArray(response.body());
            for (int i = 0; i < results.length(); i++) {
                suggestions.add(results.getJSONObject(i).getString("display_name"));
            }
        } catch (Exception e) {
            System.err.println("Erreur API: " + e.getMessage());
        }
        return suggestions;
    }
}