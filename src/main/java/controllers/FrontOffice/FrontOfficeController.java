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
            maxPrice.setText(String.format("$%.2f", maxProductPrice));
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
            maxPrice.setText(String.format("$%.2f", newValue.doubleValue()));
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
        productPagination.setPageCount((int) Math.ceil((double) filteredProducts.size() / PRODUCTS_PER_PAGE));
        productPagination.setCurrentPageIndex(0);
        productPagination.setPageFactory(this::displayProducts);
    }

    private Node displayProducts(int pageIndex) {
        int fromIndex = pageIndex * PRODUCTS_PER_PAGE;
        int toIndex = Math.min(fromIndex + PRODUCTS_PER_PAGE, filteredProducts.size());
        
        productsGrid.getChildren().clear();
        
        for (int i = fromIndex; i < toIndex; i++) {
            Product product = filteredProducts.get(i);
            productsGrid.getChildren().add(createProductCard(product));
        }
        
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
                    featuredProducts.getChildren().add(card);
                });
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        
        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        
        String imagePath = product.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                // Convert the database path to a resource path
                String resourcePath;
                if (imagePath.startsWith("file:") || imagePath.startsWith("http")) {
                    resourcePath = imagePath;
                } else {
                    // Assuming images are stored in resources/images directory
                    resourcePath = getClass().getResource("/images/" + imagePath).toExternalForm();
                }
                
                Image image = new Image(resourcePath, 180, 180, true, true);
                if (!image.isError()) {
                    imageView.setImage(image);
                    // Add the image to the card
                    StackPane imageContainer = new StackPane(imageView);
                    imageContainer.getStyleClass().add("product-image-container");
                    card.getChildren().add(imageContainer);
                } else {
                    addPlaceholder(card, product);
                }
            } catch (Exception e) {
                addPlaceholder(card, product);
            }
        } else {
            addPlaceholder(card, product);
        }
        
        Label titleLabel = new Label(product.getCategory());
        titleLabel.getStyleClass().add("product-title");
        
        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");
        
        // Button Container
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        
        // Add to Cart Button
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("add-to-cart-button");
        addToCartBtn.setOnAction(e -> addToCart(product));
        
        // View QR Code Button
        Button viewQrBtn = new Button("View QR");
        viewQrBtn.getStyleClass().add("view-qr-button");
        viewQrBtn.setOnAction(e -> displayQRCode(product));
        
        buttonContainer.getChildren().addAll(addToCartBtn, viewQrBtn);
        
        card.getChildren().addAll(titleLabel, priceLabel, buttonContainer);
        card.setAlignment(Pos.CENTER);
        
        return card;
    }

    private void addPlaceholder(VBox card, Product product) {
        Region placeholder = new Region();
        placeholder.setMinSize(180, 180);
        placeholder.setMaxSize(180, 180);
        placeholder.setStyle("-fx-background-color: #dcfce7; -fx-background-radius: 8;");
        
        String categoryInitial = !product.getCategory().isEmpty() ? 
            product.getCategory().substring(0, 1).toUpperCase() : "P";
        Label placeholderText = new Label(categoryInitial);
        placeholderText.setStyle("-fx-font-size: 48px; -fx-text-fill: #064e3b; -fx-font-weight: bold;");
        
        StackPane placeholderStack = new StackPane(placeholder, placeholderText);
        placeholderStack.getStyleClass().add("product-image-container");
        
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
        productPagination.setPageCount((int) Math.ceil((double) filteredProducts.size() / PRODUCTS_PER_PAGE));
        displayProducts(productPagination.getCurrentPageIndex());
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

    private void displayQRCode(Product product) {
        this.selectedProduct = product;
        
        // Generate QR code image
        Image qrImage = QRCodeGenerator.generateQRCodeForProduct(product, 180);
        qrCodeImageView.setImage(qrImage);
        
        // Update label
        qrCodeLabel.setText("QR Code for: " + product.getCategory());
        
        // Enable save button
        saveQrCodeButton.setDisable(false);
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