package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.Product;
import models.ProductType;
import service.ProductService;
import service.ProductTypeService;
import utils.ImageUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

public class ProductManagementController {

    @FXML
    private TableView<Product> productTable;
    
    @FXML
    private TableColumn<Product, Integer> idColumn;
    
    @FXML
    private TableColumn<Product, String> categoryColumn;
    
    @FXML
    private TableColumn<Product, ProductType> typeColumn;
    
    @FXML
    private TableColumn<Product, Integer> quantityColumn;
    
    @FXML
    private TableColumn<Product, Float> weightColumn;
    
    @FXML
    private TableColumn<Product, Double> priceColumn;
    
    @FXML
    private TextField categoryField;
    
    @FXML
    private ComboBox<ProductType> productTypeComboBox;
    
    @FXML
    private TextField quantityField;
    
    @FXML
    private TextField weightField;
    
    @FXML
    private TextField priceField;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ImageView productImageView;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button updateButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Button uploadImageButton;
    
    private ProductService productService;
    private ProductTypeService productTypeService;
    private ObservableList<Product> productList;
    private String currentImagePath;
    private Product selectedProduct;
    
    @FXML
    private void initialize() {
        productService = new ProductService();
        productTypeService = new ProductTypeService();
        productList = FXCollections.observableArrayList();
        
        setupTableColumns();
        loadAllProducts();
        loadProductTypes();
        setupSearchFieldListener();
        setupButtonHandlers();
        setupTableSelectionListener();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        // Custom cell factory for product type to display in a cleaner format
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setCellFactory(column -> new TableCell<Product, ProductType>() {
            @Override
            protected void updateItem(ProductType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getSeason() + " - " + item.getProductionMethod());
                }
            }
        });
        
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
    }
    
    private void loadAllProducts() {
        try {
            List<Product> products = productService.getAll();
            productList.clear();
            productList.addAll(products);
            productTable.setItems(productList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading products: " + e.getMessage());
        }
    }
    
    private void loadProductTypes() {
        try {
            List<ProductType> productTypes = productTypeService.getAll();
            productTypeComboBox.setItems(FXCollections.observableArrayList(productTypes));
            
            // Add a StringConverter to display a cleaner representation of ProductType
            productTypeComboBox.setConverter(new javafx.util.StringConverter<ProductType>() {
                @Override
                public String toString(ProductType productType) {
                    if (productType == null) {
                        return "";
                    }
                    return productType.getSeason() + " - " + productType.getProductionMethod();
                }
                
                @Override
                public ProductType fromString(String string) {
                    // This method is needed for the converter but not used in this context
                    return null;
                }
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading product types: " + e.getMessage());
        }
    }
    
    private void setupSearchFieldListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                loadAllProducts();
            } else {
                filterProducts(newValue);
            }
        });
    }
    
    private void filterProducts(String searchText) {
        try {
            List<Product> filteredProducts = productService.getByCategoryOrType(searchText, -1);
            productList.clear();
            productList.addAll(filteredProducts);
            productTable.setItems(productList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error filtering products: " + e.getMessage());
        }
    }
    
    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAddProduct());
        updateButton.setOnAction(event -> handleUpdateProduct());
        deleteButton.setOnAction(event -> handleDeleteProduct());
        clearButton.setOnAction(event -> clearFields());
        uploadImageButton.setOnAction(event -> handleImageUpload());
    }
    
    private void setupTableSelectionListener() {
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProduct = newSelection;
                populateFields(newSelection);
            }
        });
    }
    
    private void populateFields(Product product) {
        categoryField.setText(product.getCategory());
        productTypeComboBox.setValue(product.getType());
        quantityField.setText(String.valueOf(product.getQuantity()));
        weightField.setText(String.valueOf(product.getWeight()));
        priceField.setText(String.valueOf(product.getPrice()));
        currentImagePath = product.getImage();
        
        // Display product image using ImageUtils
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            Image productImage = ImageUtils.loadProductImage(currentImagePath, 160, 160);
            if (productImage != null && !productImage.isError()) {
                productImageView.setImage(productImage);
            } else {
                productImageView.setImage(null);
                System.err.println("Failed to load image: " + currentImagePath);
            }
        } else {
            productImageView.setImage(null);
        }
    }
    
    private void clearFields() {
        categoryField.clear();
        productTypeComboBox.setValue(null);
        quantityField.clear();
        weightField.clear();
        priceField.clear();
        productImageView.setImage(null);
        currentImagePath = null;
        selectedProduct = null;
        productTable.getSelectionModel().clearSelection();
    }
    
    private void handleAddProduct() {
        if (!validateFields()) {
            return;
        }
        
        try {
            Product product = new Product(
                    categoryField.getText(),
                    productTypeComboBox.getValue(),
                    Integer.parseInt(quantityField.getText()),
                    Float.parseFloat(weightField.getText()),
                    Double.parseDouble(priceField.getText()),
                    currentImagePath
            );
            
            productService.add(product);
            loadAllProducts();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
        } catch (SQLException | NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error adding product: " + e.getMessage());
        }
    }
    
    private void handleUpdateProduct() {
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a product to update.");
            return;
        }
        
        if (!validateFields()) {
            return;
        }
        
        try {
            selectedProduct.setCategory(categoryField.getText());
            selectedProduct.setType(productTypeComboBox.getValue());
            selectedProduct.setQuantity(Integer.parseInt(quantityField.getText()));
            selectedProduct.setWeight(Float.parseFloat(weightField.getText()));
            selectedProduct.setPrice(Double.parseDouble(priceField.getText()));
            selectedProduct.setImage(currentImagePath);
            
            productService.update(selectedProduct);
            loadAllProducts();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
        } catch (SQLException | NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error updating product: " + e.getMessage());
        }
    }
    
    private void handleDeleteProduct() {
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a product to delete.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete this product?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productService.delete(selectedProduct.getId());
                    loadAllProducts();
                    clearFields();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Error deleting product: " + e.getMessage());
                }
            }
        });
    }
    
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        
        if (selectedFile != null) {
            try {
                // Create directory if it doesn't exist
                Path rootDir = Paths.get(System.getProperty("user.dir"));
                Path resourcesDir = rootDir.resolve("workshopjdbc3a/src/main/resources");
                Path imagesDir = resourcesDir.resolve("images");
                
                // Create product images subdirectory if needed
                Path productImagesDir = imagesDir.resolve("products");
                if (!Files.exists(productImagesDir)) {
                    Files.createDirectories(productImagesDir);
                } else if (!Files.exists(imagesDir)) {
                    Files.createDirectories(imagesDir);
                }
                
                // Generate a unique filename to avoid duplicates
                String fileExtension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));
                String fileName = "product_" + System.currentTimeMillis() + fileExtension;
                
                // Store in products directory
                Path targetPath = productImagesDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Store relative path in database (as it is used in FrontOfficeController)
                currentImagePath = "products/" + fileName;
                
                System.out.println("Image saved to: " + targetPath);
                System.out.println("Path stored in database: " + currentImagePath);
                
                // Display the uploaded image
                Image image = new Image(targetPath.toUri().toString());
                productImageView.setImage(image);
                productImageView.setFitWidth(160);
                productImageView.setFitHeight(160);
                productImageView.setPreserveRatio(true);
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Image uploaded successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Image Upload Error", "Error uploading image: " + e.getMessage());
            }
        }
    }
    
    private boolean validateFields() {
        if (categoryField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a category.");
            return false;
        }
        
        if (productTypeComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a product type.");
            return false;
        }
        
        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Quantity must be a positive number.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid quantity.");
            return false;
        }
        
        try {
            float weight = Float.parseFloat(weightField.getText());
            if (weight <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Weight must be greater than zero.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid weight.");
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceField.getText());
            if (price <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Price must be greater than zero.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid price.");
            return false;
        }
        
        return true;
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 