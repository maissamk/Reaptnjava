package utils;

import javafx.scene.image.Image;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service for fetching weather information from OpenWeatherMap API
 */
public class WeatherService {
    private static final Logger LOGGER = Logger.getLogger(WeatherService.class.getName());
    
    // IMPORTANT: Replace this with your own API key from OpenWeatherMap
    private static final String API_KEY = "7f18bdc50cbb4e1f238139c1d6312582"; 
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String ICON_URL = "https://openweathermap.org/img/wn/";
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 10000; // 10 seconds
    
    /**
     * Weather data model class
     */
    public static class WeatherData {
        private String cityName;
        private String country;
        private String weatherMain;
        private String weatherDescription;
        private double temperature;
        private double feelsLike;
        private int humidity;
        private double windSpeed;
        private LocalDateTime sunrise;
        private LocalDateTime sunset;
        private String iconCode;
        
        public String getCityName() { return cityName; }
        public String getCountry() { return country; }
        public String getWeatherMain() { return weatherMain; }
        public String getWeatherDescription() { return weatherDescription; }
        public double getTemperature() { return temperature; }
        public double getFeelsLike() { return feelsLike; }
        public int getHumidity() { return humidity; }
        public double getWindSpeed() { return windSpeed; }
        public LocalDateTime getSunrise() { return sunrise; }
        public LocalDateTime getSunset() { return sunset; }
        public String getIconCode() { return iconCode; }
        
        public String getFormattedTemperature() {
            return String.format("%.1f°C", temperature);
        }
        
        public String getFormattedFeelsLike() {
            return String.format("%.1f°C", feelsLike);
        }
        
        public String getFormattedSunrise() {
            return sunrise.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        
        public String getFormattedSunset() {
            return sunset.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        
        public String getCapitalizedDescription() {
            if (weatherDescription == null || weatherDescription.isEmpty()) {
                return "";
            }
            return weatherDescription.substring(0, 1).toUpperCase() + weatherDescription.substring(1);
        }
        
        public Image getWeatherIcon() {
            try {
                String iconUrl = ICON_URL + iconCode + "@2x.png";
                LOGGER.fine("Loading weather icon from URL: " + iconUrl);
                return new Image(iconUrl, true); // true enables background loading
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error loading weather icon: " + e.getMessage(), e);
                return null;
            }
        }
    }
    
    /**
     * Get weather data for a specified city
     * 
     * @param city The city name to get weather for
     * @return WeatherData object containing weather information
     * @throws Exception If an error occurs during API call
     */
    public static WeatherData getWeatherForCity(String city) throws Exception {
        if (city == null || city.trim().isEmpty()) {
            LOGGER.warning("Empty city name provided");
            throw new IllegalArgumentException("City name cannot be empty");
        }
        
        LOGGER.info("Fetching weather data for city: " + city);
        HttpURLConnection connection = null;
        
        try {
            // URL encode the city name to handle spaces and special characters
            String encodedCity = java.net.URLEncoder.encode(city.trim(), "UTF-8");
            String urlString = BASE_URL + "?q=" + encodedCity + "&appid=" + API_KEY + "&units=metric";
            
            LOGGER.fine("Weather API URL: " + urlString);
            
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Set a user agent to avoid potential blocks
            connection.setRequestProperty("User-Agent", "Fruitables Weather Widget");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            
            int responseCode = connection.getResponseCode();
            LOGGER.info("Weather API Response Code: " + responseCode);
            
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                    try (InputStream inputStream = connection.getInputStream();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        
                        String response = reader.lines().collect(Collectors.joining());
                        LOGGER.fine("Received API response: " + response);
                        return parseWeatherData(response);
                    }
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    LOGGER.warning("API Key authentication failed (401). The API key may be invalid or not activated yet.");
                    LOGGER.info("Using mock data as fallback...");
                    return getMockWeatherData(city);
                case HttpURLConnection.HTTP_NOT_FOUND:
                    LOGGER.warning("City not found (404): " + city);
                    throw new Exception("City not found: " + city);
                case 429: // Too Many Requests
                    LOGGER.warning("Rate limit exceeded (429). Using mock data as fallback.");
                    return getMockWeatherData(city);
                default:
                    LOGGER.warning("Failed to get weather data. Response code: " + responseCode);
                    throw new Exception("Failed to get weather data. Response code: " + responseCode);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error fetching weather: " + e.getMessage(), e);
            
            // Check if this is a network connectivity issue
            if (e instanceof java.net.SocketTimeoutException || 
                e instanceof java.net.UnknownHostException ||
                e instanceof java.net.ConnectException) {
                LOGGER.info("Network connection issue. Using mock data as fallback...");
                return getMockWeatherData(city);
            }
            
            if (e.getMessage() != null && e.getMessage().contains("City not found")) {
                throw e; // Rethrow specific errors that shouldn't be handled with mock data
            }
            
            LOGGER.info("Using mock data as fallback...");
            return getMockWeatherData(city);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Parse JSON response into WeatherData object
     */
    private static WeatherData parseWeatherData(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            WeatherData data = new WeatherData();
            
            data.cityName = json.getString("name");
            
            JSONObject sys = json.getJSONObject("sys");
            data.country = sys.getString("country");
            
            JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
            data.weatherMain = weather.getString("main");
            data.weatherDescription = weather.getString("description");
            data.iconCode = weather.getString("icon");
            
            JSONObject main = json.getJSONObject("main");
            data.temperature = main.getDouble("temp");
            data.feelsLike = main.getDouble("feels_like");
            data.humidity = main.getInt("humidity");
            
            JSONObject wind = json.getJSONObject("wind");
            data.windSpeed = wind.getDouble("speed");
            
            // Convert Unix timestamps to LocalDateTime
            data.sunrise = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(sys.getLong("sunrise")),
                ZoneId.systemDefault()
            );
            
            data.sunset = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(sys.getLong("sunset")),
                ZoneId.systemDefault()
            );
            
            return data;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error parsing weather data: " + e.getMessage(), e);
            throw new RuntimeException("Error parsing weather data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create mock weather data for testing when API is unavailable
     * 
     * @param city City name
     * @return Mock WeatherData object
     */
    private static WeatherData getMockWeatherData(String city) {
        LOGGER.info("Generating mock weather data for: " + city);
        
        WeatherData mockData = new WeatherData();
        mockData.cityName = city;
        mockData.country = "Demo";
        mockData.weatherMain = "Clear";
        mockData.weatherDescription = "clear sky";
        mockData.temperature = 22.5;
        mockData.feelsLike = 23.0;
        mockData.humidity = 65;
        mockData.windSpeed = 4.2;
        mockData.iconCode = "01d"; // Clear sky icon
        
        // Set sunrise and sunset times to current time +/- 6 hours
        LocalDateTime now = LocalDateTime.now();
        mockData.sunrise = now.minusHours(6);
        mockData.sunset = now.plusHours(6);
        
        return mockData;
    }
} 