package utils;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class LanguageManager {

    public static String selectedLanguage = "default";  // Default language
    public static Properties properties = new Properties();

    // Load the appropriate language resource bundle
    public static void loadLanguage() {
        // Clear previous properties to ensure we load a fresh set
        properties.clear();

        // Load German translations if selected language is "de"
        if ("de".equals(selectedLanguage)) {
            loadProperties("messages_de.properties");
        } else {
            // When "default" (English) is selected, no translations are loaded
            System.out.println("English selected - No translation loaded.");
        }
    }

    // Helper method to load a properties file with UTF-8 encoding
    private static void loadProperties(String fileName) {
        try (InputStream inputStream = LanguageManager.class.getClassLoader().getResourceAsStream(fileName);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            if (inputStream != null) {
                properties.load(reader);  // Load the translations with UTF-8 encoding
            } else {
                System.err.println("Could not find language file: " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Retrieve the translated string from the properties file
    public static String getString(String key) {
        // Return the translation if available, else fallback to the key (or default text)
        return properties.getProperty(key, key);  // Returns the key itself if translation is not found
    }
}
