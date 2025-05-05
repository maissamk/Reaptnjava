package components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import utils.WeatherService;
import utils.WeatherService.WeatherData;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Weather widget component for displaying current weather information
 */
public class WeatherWidget extends VBox {
    private static final Logger LOGGER = Logger.getLogger(WeatherWidget.class.getName());
    
    private final Label temperatureLabel = new Label("--°C");
    private final Label cityLabel = new Label("City, Country");
    private final Label descriptionLabel = new Label("Weather description");
    private final Label detailedWeatherLabel = new Label("Detailed weather information will appear here");
    private final Label feelsLikeLabel = new Label("Feels like: --°C");
    private final Label humidityLabel = new Label("Humidity: --%");
    private final Label windSpeedLabel = new Label("Wind: -- m/s");
    private final Label sunriseLabel = new Label("Sunrise: --:--");
    private final Label sunsetLabel = new Label("Sunset: --:--");
    private final Label todayWeatherLabel = new Label("How's the weather today?");
    private final ImageView weatherIconView = new ImageView();
    private final TextField cityInputField = new TextField();
    private final Button searchButton = new Button("Search");
    private final Button refreshButton = new Button("⟳");
    private final Label errorLabel = new Label();
    private final Label statusLabel = new Label("Ready");
    
    private String currentCity = "Paris";
    private boolean isLoading = false;
    
    public WeatherWidget() {
        setupUI();
        // Load default city
        loadWeatherData(currentCity);
        
        // Prevent scroll propagation
        preventScrollPropagation();
    }
    
    private void setupUI() {
        this.getStyleClass().add("weather-widget");
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setMaxWidth(350);
        
        // Search Box with Refresh Button
        HBox searchBox = new HBox(5);
        searchBox.setAlignment(Pos.CENTER);
        cityInputField.setPromptText("Enter city");
        cityInputField.getStyleClass().add("weather-search-field");
        searchButton.getStyleClass().add("weather-search-button");
        refreshButton.getStyleClass().add("weather-refresh-button");
        refreshButton.setTooltip(new javafx.scene.control.Tooltip("Refresh current weather"));
        
        // Set action on button and enter key with event consumption to prevent scrolling
        searchButton.setOnAction(e -> {
            e.consume(); // Prevent event propagation
            handleSearch();
        });
        
        cityInputField.setOnAction(e -> {
            e.consume(); // Prevent event propagation
            handleSearch();
        });
        
        refreshButton.setOnAction(e -> {
            e.consume(); // Prevent event propagation
            handleRefresh();
        });
        
        HBox.setHgrow(cityInputField, Priority.ALWAYS);
        searchBox.getChildren().addAll(cityInputField, searchButton, refreshButton);
        
        // Status label for feedback
        statusLabel.getStyleClass().add("weather-status-label");
        statusLabel.setVisible(true);
        
        // Weather Header (Icon, Temperature)
        HBox weatherHeader = new HBox(20);
        weatherHeader.setAlignment(Pos.CENTER);
        
        weatherIconView.setFitWidth(80);
        weatherIconView.setFitHeight(80);
        weatherIconView.getStyleClass().add("weather-icon");
        
        temperatureLabel.getStyleClass().add("temperature-label");
        
        VBox tempBox = new VBox(5);
        tempBox.setAlignment(Pos.CENTER_LEFT);
        tempBox.getChildren().addAll(temperatureLabel, feelsLikeLabel);
        
        weatherHeader.getChildren().addAll(weatherIconView, tempBox);
        
        // Location and Description
        cityLabel.getStyleClass().add("city-label");
        descriptionLabel.getStyleClass().add("description-label");
        
        // Detailed weather description
        detailedWeatherLabel.getStyleClass().add("detailed-weather-label");
        detailedWeatherLabel.setAlignment(Pos.CENTER);
        detailedWeatherLabel.setWrapText(true);
        detailedWeatherLabel.setStyle("-fx-font-size: 14px; -fx-padding: 5 0; -fx-font-weight: normal;");
        
        // Today's weather message
        todayWeatherLabel.getStyleClass().add("today-weather-label");
        todayWeatherLabel.setAlignment(Pos.CENTER);
        todayWeatherLabel.setStyle("-fx-font-style: italic; -fx-font-size: 14px; -fx-padding: 5 0;");
        
        // Weather Details
        HBox weatherDetails = new HBox(30);
        weatherDetails.setAlignment(Pos.CENTER);
        
        VBox detailsLeft = new VBox(8);
        detailsLeft.getChildren().addAll(humidityLabel, windSpeedLabel);
        
        VBox detailsRight = new VBox(8);
        detailsRight.getChildren().addAll(sunriseLabel, sunsetLabel);
        
        weatherDetails.getChildren().addAll(detailsLeft, detailsRight);
        
        // Error label
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        // Add all to main container
        this.getChildren().addAll(
            searchBox, 
            statusLabel,
            new Region() {{ setMinHeight(10); }},
            weatherHeader,
            cityLabel,
            descriptionLabel,
            detailedWeatherLabel,
            todayWeatherLabel,
            new Region() {{ setMinHeight(5); }},
            weatherDetails,
            errorLabel
        );
    }
    
    private void handleSearch() {
        String city = cityInputField.getText().trim();
        if (!city.isEmpty()) {
            currentCity = city;
            loadWeatherData(city);
            
            // Set focus to the widget itself to blur the input field
            this.requestFocus();
        }
    }
    
    private void handleRefresh() {
        if (!isLoading && currentCity != null && !currentCity.isEmpty()) {
            LOGGER.info("Refreshing weather data for: " + currentCity);
            loadWeatherData(currentCity);
            
            // Set focus to the widget itself to blur the input field
            this.requestFocus();
        }
    }
    
    private void loadWeatherData(String city) {
        if (isLoading) {
            LOGGER.info("Weather data is already being loaded, ignoring request");
            return;
        }
        
        // Hide any previous error
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        // Show loading state
        isLoading = true;
        statusLabel.setText("Loading weather data...");
        temperatureLabel.setText("Loading...");
        descriptionLabel.setText("Fetching weather data");
        detailedWeatherLabel.setText("Loading weather information...");
        cityLabel.setText(city);
        
        // Disable inputs while loading
        setControlsEnabled(false);
        
        // Clear focus and return focus to widget to prevent scrolling
        Platform.runLater(() -> {
            this.requestFocus();
            // Make sure the city input field loses focus by setting focus elsewhere
            cityInputField.getParent().requestFocus();
        });
        
        LOGGER.info("Loading weather data for city: " + city);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return WeatherService.getWeatherForCity(city);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error getting weather data: " + e.getMessage(), e);
                return null;
            }
        }).thenAccept(weatherData -> {
            Platform.runLater(() -> {
                isLoading = false;
                setControlsEnabled(true);
                statusLabel.setText("Last updated: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
                
                if (weatherData != null) {
                    updateWeatherUI(weatherData);
                } else {
                    showError("Could not fetch weather for: " + city);
                }
                
                // Make sure we don't affect scroll position
                this.requestFocus();
            });
        });
    }
    
    private void setControlsEnabled(boolean enabled) {
        searchButton.setDisable(!enabled);
        refreshButton.setDisable(!enabled);
        cityInputField.setDisable(!enabled);
    }
    
    private void updateWeatherUI(WeatherData data) {
        try {
            temperatureLabel.setText(data.getFormattedTemperature());
            cityLabel.setText(data.getCityName() + ", " + data.getCountry());
            descriptionLabel.setText(data.getCapitalizedDescription());
            
            // Update the detailed weather description
            updateDetailedWeatherDescription(data);
            
            feelsLikeLabel.setText("Feels like: " + data.getFormattedFeelsLike());
            humidityLabel.setText("Humidity: " + data.getHumidity() + "%");
            windSpeedLabel.setText("Wind: " + String.format("%.1f", data.getWindSpeed()) + " m/s");
            sunriseLabel.setText("Sunrise: " + data.getFormattedSunrise());
            sunsetLabel.setText("Sunset: " + data.getFormattedSunset());
            
            // Set today's weather message based on current conditions
            updateTodayWeatherMessage(data);
            
            // Check if this is mock data
            if (data.getCountry().equals("Demo")) {
                descriptionLabel.setText(data.getCapitalizedDescription() + " (Demo data)");
                statusLabel.setText("Using demo data (API unavailable)");
            }
            
            weatherIconView.setImage(data.getWeatherIcon());
            
            LOGGER.info("Successfully updated weather UI for: " + data.getCityName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating weather UI: " + e.getMessage(), e);
            showError("Error displaying weather data");
        }
    }
    
    private void updateDetailedWeatherDescription(WeatherData data) {
        String description = data.getWeatherDescription().toLowerCase();
        int temp = (int) Math.round(data.getTemperature());
        int humidity = data.getHumidity();
        double windSpeed = data.getWindSpeed();
        
        StringBuilder details = new StringBuilder("Current weather: ");
        
        // Add temperature description
        if (temp < 0) {
            details.append("Freezing ");
        } else if (temp < 10) {
            details.append("Cold ");
        } else if (temp < 20) {
            details.append("Cool ");
        } else if (temp < 25) {
            details.append("Mild ");
        } else if (temp < 30) {
            details.append("Warm ");
        } else {
            details.append("Hot ");
        }
        
        // Add main weather type
        if (description.contains("rain") || description.contains("drizzle")) {
            details.append("and rainy. ");
        } else if (description.contains("snow")) {
            details.append("with snowfall. ");
        } else if (description.contains("cloud")) {
            details.append("and cloudy. ");
        } else if (description.contains("clear") || description.contains("sun")) {
            details.append("with clear skies. ");
        } else if (description.contains("fog") || description.contains("mist")) {
            details.append("with reduced visibility. ");
        } else if (description.contains("thunder") || description.contains("storm")) {
            details.append("with thunderstorms. ");
        } else {
            details.append("with " + data.getWeatherDescription() + ". ");
        }
        
        // Add humidity information
        if (humidity > 80) {
            details.append("Very humid ");
        } else if (humidity > 60) {
            details.append("Humid ");
        } else if (humidity < 30) {
            details.append("Dry ");
        } else {
            details.append("Moderate humidity ");
        }
        
        // Add wind information
        if (windSpeed > 10) {
            details.append("and windy conditions.");
        } else if (windSpeed > 5) {
            details.append("with moderate breeze.");
        } else {
            details.append("with light winds.");
        }
        
        detailedWeatherLabel.setText(details.toString());
    }
    
    private void updateTodayWeatherMessage(WeatherData data) {
        int temp = (int) Math.round(data.getTemperature());
        String description = data.getWeatherDescription().toLowerCase();
        
        if (description.contains("rain") || description.contains("drizzle")) {
            todayWeatherLabel.setText("Today's weather: Don't forget your umbrella!");
        } else if (description.contains("snow")) {
            todayWeatherLabel.setText("Today's weather: Bundle up, it's snowing!");
        } else if (description.contains("clear") && temp > 25) {
            todayWeatherLabel.setText("Today's weather: Perfect day for the beach!");
        } else if (description.contains("clear") || description.contains("sun")) {
            todayWeatherLabel.setText("Today's weather: Enjoy the sunshine today!");
        } else if (description.contains("cloud")) {
            todayWeatherLabel.setText("Today's weather: A bit cloudy, but still nice!");
        } else if (description.contains("fog") || description.contains("mist")) {
            todayWeatherLabel.setText("Today's weather: Be careful driving in low visibility!");
        } else if (temp < 5) {
            todayWeatherLabel.setText("Today's weather: It's freezing cold outside!");
        } else if (temp > 30) {
            todayWeatherLabel.setText("Today's weather: Stay hydrated, it's hot today!");
        } else {
            todayWeatherLabel.setText("Today's weather: " + data.getCapitalizedDescription() + "!");
        }
    }
    
    private void showError(String message) {
        LOGGER.warning("Weather widget error: " + message);
        temperatureLabel.setText("--°C");
        descriptionLabel.setText("Weather unavailable");
        detailedWeatherLabel.setText("Weather details unavailable");
        todayWeatherLabel.setText("How's the weather today?");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        weatherIconView.setImage(null);
        statusLabel.setText("Error occurred");
    }
    
    /**
     * Prevents the widget from affecting the parent scroll behavior
     */
    private void preventScrollPropagation() {
        // Consume scroll events to prevent them from bubbling up to parent containers
        this.setOnScroll(event -> {
            event.consume();
        });
        
        // Prevent focus events from affecting scroll position
        this.setOnMouseClicked(event -> {
            event.consume();
            this.requestFocus();
        });
    }
} 