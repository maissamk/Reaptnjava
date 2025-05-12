package controllers.FrontOffice;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import Models.Product;
import Models.ProductType;
import service.ProductService;
import service.ProductTypeService;
import utils.QRCodeGenerator;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;

import java.io.File;

import components.WeatherWidget;
import utils.ImageUtils;
import javafx.stage.Popup;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;

public class FrontOfficeController implements Initializable {
    @FXML private TextField searchField;
    @FXML private Label cartItemCount;
    @FXML private HBox featuredProducts;
    @FXML private VBox categoryFilters;
    @FXML private Slider priceSlider;
    @FXML private Label minPrice;
    @FXML private Label maxPrice;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private FlowPane productsGrid;
    @FXML private Pagination productPagination;
    @FXML private ImageView qrCodeImageView;
    @FXML private Label qrCodeLabel;
    @FXML private Button saveQrCodeButton;
    @FXML private StackPane qrCodeContainer;
    @FXML private WeatherWidget weatherWidget;
    @FXML private Label productCount;
    @FXML private Button filterResetButton;

    private final ProductService productService;
    private final ProductTypeService productTypeService;
    private final ObservableList<Product> allProducts;
    private final ObservableList<Product> filteredProducts;
    private final Map<Integer, Integer> cartItems;
    private double maxProductPrice = 100.0;
    private static final int PRODUCTS_PER_PAGE = 12;
    private Product selectedProduct;

    public FrontOfficeController() {
        productService = new ProductService();
        productTypeService = new ProductTypeService();
        allProducts = FXCollections.observableArrayList();
        filteredProducts = FXCollections.observableArrayList();
        cartItems = new HashMap<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProducts();
        setupSearchField();
        setupCategoryFilters();
        setupPriceSlider();
        setupSortComboBox();
        setupQRCodeSection();
        setupPagination();
        
        // Initialize minPrice to 0
        minPrice.setText("0.00 DT");
        
        // Display initial products
        int initialProductCount = Math.min(PRODUCTS_PER_PAGE, filteredProducts.size());
        productCount.setText(String.format("Showing %d of %d products", initialProductCount, filteredProducts.size()));
        displayProducts(0);
        displayFeaturedProducts();
    }

    private void loadProducts() {
        try {
            List<Product> products = productService.getAll();
            allProducts.addAll(products);
            filteredProducts.addAll(products);
            
            // Find max price for slider
            maxProductPrice = products.stream()
                    .mapToDouble(Product::getPrice)
                    .max()
                    .orElse(100.0);
            
            priceSlider.setMax(maxProductPrice);
            maxPrice.setText(String.format("%.2f DT", maxProductPrice));
        } catch (Exception e) {
            showError("Error loading products", e.getMessage());
        }
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts();
        });
    }

    private void setupCategoryFilters() {
        try {
            List<ProductType> types = productTypeService.getAll();
            Set<String> categories = new HashSet<>();
            
            for (Product product : allProducts) {
                categories.add(product.getCategory());
            }

            categories.forEach(category -> {
                CheckBox checkBox = new CheckBox(category);
                checkBox.setSelected(true);
                checkBox.selectedProperty().addListener((obs, old, newValue) -> filterProducts());
                categoryFilters.getChildren().add(checkBox);
            });
        } catch (Exception e) {
            showError("Error setting up filters", e.getMessage());
        }
    }

    private void setupPriceSlider() {
        priceSlider.valueProperty().addListener((obs, old, newValue) -> {
            maxPrice.setText(String.format("%.2f DT", newValue.doubleValue()));
            filterProducts();
        });
    }

    private void setupSortComboBox() {
        sortComboBox.getItems().addAll(
            "Price: Low to High",
            "Price: High to Low",
            "Name: A to Z",
            "Name: Z to A"
        );
        sortComboBox.setValue("Price: Low to High");
        sortComboBox.setOnAction(e -> filterProducts());
    }

    private void setupQRCodeSection() {
        saveQrCodeButton.setOnAction(e -> saveQRCodeToFile());
        saveQrCodeButton.setDisable(true);
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) filteredProducts.size() / PRODUCTS_PER_PAGE);
        pageCount = Math.max(1, pageCount); // Ensure at least one page
        
        productPagination.setPageCount(pageCount);
        productPagination.setCurrentPageIndex(0);
        productPagination.setPageFactory(this::displayProducts);
        
        // Add page change listener to update product count
        productPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            displayProducts(newIndex.intValue());
        });
    }

    private Node displayProducts(int pageIndex) {
        int fromIndex = pageIndex * PRODUCTS_PER_PAGE;
        int toIndex = Math.min(fromIndex + PRODUCTS_PER_PAGE, filteredProducts.size());
        
        productsGrid.getChildren().clear();
        
        for (int i = fromIndex; i < toIndex; i++) {
            Product product = filteredProducts.get(i);
            productsGrid.getChildren().add(createProductCard(product));
        }
        
        // Update product count text
        int productsShown = toIndex - fromIndex;
        int totalProducts = filteredProducts.size();
        productCount.setText(String.format("Showing %d of %d products", productsShown, totalProducts));
        
        return productsGrid;
    }

    private void displayFeaturedProducts() {
        featuredProducts.getChildren().clear();
        
        // Display top 5 products as featured
        allProducts.stream()
                .limit(5)
                .forEach(product -> {
                    VBox card = createProductCard(product);
                    card.setPrefWidth(280);
                    card.setMinWidth(280);
                    card.setMaxWidth(280);
                    card.setPrefHeight(350);
                    card.setMinHeight(350);
                    featuredProducts.getChildren().add(card);
                });
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(220);
        card.setMinWidth(220);
        card.setMaxWidth(220);
        card.setPrefHeight(320);
        card.setMinHeight(320);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 0); -fx-border-color: #e2efe2; -fx-border-radius: 8;");
        
        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        
        String imagePath = product.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                // Use ImageUtils to load the product image
                Image image = ImageUtils.loadProductImage(imagePath, 180, 180);
                
                if (image != null && !image.isError()) {
                    imageView.setImage(image);
                    // Add the image to the card
                    StackPane imageContainer = new StackPane(imageView);
                    imageContainer.getStyleClass().add("product-image-container");
                    imageContainer.setStyle("-fx-padding: 10;");
                    card.getChildren().add(imageContainer);
                } else {
                    addPlaceholder(card, product);
                }
            } catch (Exception e) {
                System.err.println("Error loading image for product " + product.getCategory() + ": " + e.getMessage());
                addPlaceholder(card, product);
            }
        } else {
            addPlaceholder(card, product);
        }
        
        // Product details in a separate VBox to control spacing
        VBox detailsBox = new VBox(8);
        detailsBox.setAlignment(Pos.CENTER);
        detailsBox.setPadding(new javafx.geometry.Insets(10, 5, 10, 5));
        
        Label titleLabel = new Label(product.getCategory());
        titleLabel.getStyleClass().add("product-title");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setStyle("-fx-text-fill: #386e40; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label priceLabel = new Label(String.format("%.2f DT", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");
        priceLabel.setStyle("-fx-text-fill: #488251; -fx-font-size: 14px;");
        
        detailsBox.getChildren().addAll(titleLabel, priceLabel);
        
        // Button Container
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new javafx.geometry.Insets(0, 0, 10, 0));
        
        // Add to Cart Button
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("add-to-cart-button");
        addToCartBtn.setStyle("-fx-background-color: #ffb006; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 4;");
        addToCartBtn.setOnAction(e -> addToCart(product));
        
        // View QR Code Button
        Button viewQrBtn = new Button("View QR");
        viewQrBtn.getStyleClass().add("view-qr-button");
        viewQrBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #386e40; -fx-text-fill: #386e40; -fx-background-radius: 4; -fx-border-radius: 4;");
        viewQrBtn.setOnAction(e -> displayQRCode(product, viewQrBtn));
        
        buttonContainer.getChildren().addAll(addToCartBtn, viewQrBtn);
        
        card.getChildren().addAll(detailsBox, buttonContainer);
        card.setAlignment(Pos.CENTER);
        
        return card;
    }

    private void addPlaceholder(VBox card, Product product) {
        Region placeholder = new Region();
        placeholder.setMinSize(180, 180);
        placeholder.setMaxSize(180, 180);
        placeholder.setStyle("-fx-background-color: #f6fbf7; -fx-background-radius: 8;");
        
        String categoryInitial = !product.getCategory().isEmpty() ? 
            product.getCategory().substring(0, 1).toUpperCase() : "P";
        Label placeholderText = new Label(categoryInitial);
        placeholderText.setStyle("-fx-font-size: 48px; -fx-text-fill: #386e40; -fx-font-weight: bold;");
        
        StackPane placeholderStack = new StackPane(placeholder, placeholderText);
        placeholderStack.getStyleClass().add("product-image-container");
        placeholderStack.setStyle("-fx-padding: 10;");
        
        card.getChildren().add(placeholderStack);
    }

    private void filterProducts() {
        filteredProducts.clear();
        
        // Get selected categories
        List<String> selectedCategories = categoryFilters.getChildren().stream()
                .filter(node -> node instanceof CheckBox)
                .map(node -> (CheckBox) node)
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());
        
        // Filter products
        allProducts.stream()
                .filter(product -> 
                    selectedCategories.contains(product.getCategory()) &&
                    product.getPrice() <= priceSlider.getValue() &&
                    (searchField.getText().isEmpty() ||
                     product.getCategory().toLowerCase().contains(searchField.getText().toLowerCase()))
                )
                .forEach(filteredProducts::add);
        
        // Sort products
        switch (sortComboBox.getValue()) {
            case "Price: Low to High":
                filteredProducts.sort(Comparator.comparing(Product::getPrice));
                break;
            case "Price: High to Low":
                filteredProducts.sort(Comparator.comparing(Product::getPrice).reversed());
                break;
            case "Name: A to Z":
                filteredProducts.sort(Comparator.comparing(Product::getCategory));
                break;
            case "Name: Z to A":
                filteredProducts.sort(Comparator.comparing(Product::getCategory).reversed());
                break;
        }
        
        // Update pagination
        int pageCount = (int) Math.ceil((double) filteredProducts.size() / PRODUCTS_PER_PAGE);
        pageCount = Math.max(1, pageCount); // Ensure at least one page
        productPagination.setPageCount(pageCount);
        
        // Reset to first page when filter changes
        productPagination.setCurrentPageIndex(0);
        displayProducts(0);
    }

    private void addToCart(Product product) {
        cartItems.merge(product.getId(), 1, Integer::sum);
        updateCartCount();
        showInfo("Added to Cart", product.getCategory() + " has been added to your cart.");
    }

    private void updateCartCount() {
        int totalItems = cartItems.values().stream().mapToInt(Integer::intValue).sum();
        cartItemCount.setText(String.valueOf(totalItems));
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void displayQRCode(Product product, Button sourceButton) {
        this.selectedProduct = product;
        
        // Generate QR code image
        Image qrImage = QRCodeGenerator.generateQRCodeForProduct(product, 180);
        qrCodeImageView.setImage(qrImage);
        
        // Update label
        qrCodeLabel.setText("QR Code for: " + product.getCategory());
        
        // Enable save button
        saveQrCodeButton.setDisable(false);
        
        // Show popup with QR code
        showQRCodePopup(product, qrImage, sourceButton);
    }
    
    private void showQRCodePopup(Product product, Image qrImage, Button sourceButton) {
        // Create popup
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        
        // Create a container for the QR code
        StackPane popupContent = new StackPane();
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0); -fx-background-radius: 5;");
        
        // Create QR code ImageView
        ImageView popupQrView = new ImageView(qrImage);
        popupQrView.setFitWidth(200);
        popupQrView.setFitHeight(200);
        popupQrView.setPreserveRatio(true);
        
        // Create a title label
        Label titleLabel = new Label(product.getCategory() + " - " + String.format("%.2f DT", product.getPrice()));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 0 10 0;");
        
        // Close on click
        popupContent.setOnMouseClicked(e -> popup.hide());
        
        // Create a VBox to hold the title and image
        VBox popupVBox = new VBox(10);
        popupVBox.setAlignment(Pos.CENTER);
        popupVBox.getChildren().addAll(titleLabel, popupQrView);
        
        popupContent.getChildren().add(popupVBox);
        popup.getContent().add(popupContent);
        
        // Show popup near the button
        javafx.geometry.Bounds buttonBounds = sourceButton.localToScreen(sourceButton.getBoundsInLocal());
        double centerX = buttonBounds.getMinX() + buttonBounds.getWidth() / 2;
        double centerY = buttonBounds.getMinY() + buttonBounds.getHeight() / 2;
        
        // Position popup above the button with some offset
        popup.show(sourceButton, centerX - 100, centerY - 240);
    }
    
    private void saveQRCodeToFile() {
        if (selectedProduct == null || qrCodeImageView.getImage() == null) {
            showInfo("No QR Code Available", "Please select a product first to generate a QR code.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save QR Code");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );
        fileChooser.setInitialFileName(selectedProduct.getCategory().replace(" ", "_") + "_QR.png");
        
        File file = fileChooser.showSaveDialog(qrCodeContainer.getScene().getWindow());
        if (file != null) {
            try {
                // Convert JavaFX image to BufferedImage and save using ImageIO
                java.awt.image.BufferedImage bufferedImage = SwingFXUtils.fromFXImage(qrCodeImageView.getImage(), null);
                ImageIO.write(bufferedImage, "png", file);
                
                showInfo("QR Code Saved", "QR code has been saved successfully to: " + file.getAbsolutePath());
            } catch (Exception e) {
                showError("Error Saving QR Code", "Failed to save QR code: " + e.getMessage());
            }
        }
    }
} 