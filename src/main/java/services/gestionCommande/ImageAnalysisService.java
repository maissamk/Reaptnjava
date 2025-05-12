package services.gestionCommande;

import Models.gestionCommande.DiagnosticModel;
import Models.gestionCommande.PlantSuggestion;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class ImageAnalysisService {

    private static final String API_KEY = "ooUzMFoaPMBQS0vI4akDzsV7v9ovO3GsebnKIo4jlUJboP4dxr";
    private static final String API_URL = "https://plant.id/api/v3/health_assessment";

    private static final Gson gson = new GsonBuilder().create();

    /**
     * Analyse l’image et retourne le modèle de diagnostic.
     * @param imagePath le chemin du fichier image sur disque
     */


    public static DiagnosticModel analyzePlantImage(Path imagePath) throws Exception {
        // 1. Lire et encoder en Base64
        byte[] bytes = Files.readAllBytes(imagePath);
        String base64 = Base64.getEncoder().encodeToString(bytes);
      //  c’est la forme attendue par l’API (dataUri) w lfoug 3malna convertion mtee3 image
        String dataUri = "data:image/jpeg;base64," + base64;

        // 2. Construire le JSON de la requête
        Map<String,Object> payload = Map.of(
                "images", new String[]{ dataUri },
                "latitude", 36.8065,
                "longitude", 10.1815,
                "similar_images", true
        );
        String jsonPayload = gson.toJson(payload);


        // 3. Préparer et envoyer la requête HTTP
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Api-Key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 4. Gérer la réponse et extraire "result.disease.suggestions"
        int status = response.statusCode();
        if (status == 200 || status == 201) {
            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray suggestionsArray = root
                    .getAsJsonObject("result")
                    .getAsJsonObject("disease")
                    .getAsJsonArray("suggestions");

            // Convertir en List<PlantSuggestion>
            List<PlantSuggestion> suggestions = gson.fromJson(
                    suggestionsArray,
                    new TypeToken<List<PlantSuggestion>>(){}.getType()
            );

            return new DiagnosticModel(suggestions);
        } else {
            throw new RuntimeException(
                    "API error " + status + " : " + response.body()
            );
        }
    }
}
