package utils;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeminiService {

    // Mets ici ta vraie clé API
    private static final String API_KEY = "AIzaSyBigwqPu6qqcTvx1xmMPZktz5Foljts4No";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public static boolean isSuitable(String offreComp, String employeComp) throws IOException {
        // 1. Créer le prompt
        String prompt = "Voici les compétences requises pour une offre : " + offreComp + ".\n" +
                "Voici les compétences d'un employé : " + employeComp + ".\n" +
                "Cet employé est-il adapté au poste ? Réponds uniquement par 'Oui' ou 'Non'.";

        // 2. Construire le JSON pour l'API
        String jsonInputString = "{\n" +
                "  \"contents\": [{\n" +
                "    \"parts\": [{\"text\": \"" + prompt.replace("\"", "\\\"") + "\"}]\n" +
                "  }]\n" +
                "}";

        // 3. Envoyer la requête
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 4. Lire la réponse
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        conn.disconnect();

        // 5. Vérifier si la réponse contient "Oui"
        return response.toString().toLowerCase().contains("oui");
    }
}