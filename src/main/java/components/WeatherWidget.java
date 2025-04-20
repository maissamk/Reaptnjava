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
    private final Label feelsLikeLabel = new Label("Feels like: --°C");
    private final Label humidityLabel = new Label("Humidity: --%");
    private final Label windSpeedLabel = new Label("Wind: -- m/s");
    private final Label sunriseLabel = new Label("Sunrise: --:--");
    private final Label sunsetLabel = new Label("Sunset: --:--");
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
            feelsLikeLabel.setText("Feels like: " + data.getFormattedFeelsLike());
            humidityLabel.setText("Humidity: " + data.getHumidity() + "%");
            windSpeedLabel.setText("Wind: " + String.format("%.1f", data.getWindSpeed()) + " m/s");
            sunriseLabel.setText("Sunrise: " + data.getFormattedSunrise());
            sunsetLabel.setText("Sunset: " + data.getFormattedSunset());
            
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
    
    private void showError(String message) {
        LOGGER.warning("Weather widget error: " + message);
        temperatureLabel.setText("--°C");
        descriptionLabel.setText("Weather unavailable");
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