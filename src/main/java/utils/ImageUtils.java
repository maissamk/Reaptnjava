package utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for image operations across the application
 */
public class ImageUtils {

    private static final String PROJECT_ROOT = "workshopjdbc3a";
    private static final String IMAGES_DIR = "src/main/resources/images";
    private static final String PRODUCTS_DIR = "products";

    /**
     * Loads an image from various possible locations, with detailed logging
     * @param imagePath The database-stored path of the image
     * @param width The desired width
     * @param height The desired height
     * @return The loaded Image, or null if not found
     */
    public static Image loadProductImage(String imagePath, double width, double height) {
        if (imagePath == null || imagePath.isEmpty()) {
            System.out.println("Image path is null or empty");
            return null;
        }

        System.out.println("Loading image: " + imagePath);
        String resourcePath = null;
        
        try {
            // 1. Try direct resource lookup
            URL imageUrl = ImageUtils.class.getResource("/images/" + imagePath);
            if (imageUrl != null) {
                resourcePath = imageUrl.toExternalForm();
                System.out.println("Found as resource: " + resourcePath);
                return new Image(resourcePath, width, height, true, true);
            }
            
            // 2. If not found and starts with 'products/', try without that prefix
            if (imagePath.startsWith(PRODUCTS_DIR + "/")) {
                String imageName = imagePath.substring((PRODUCTS_DIR + "/").length());
                imageUrl = ImageUtils.class.getResource("/images/" + PRODUCTS_DIR + "/" + imageName);
                if (imageUrl != null) {
                    resourcePath = imageUrl.toExternalForm();
                    System.out.println("Found as resource with modified path: " + resourcePath);
                    return new Image(resourcePath, width, height, true, true);
                }
            }
            
            // 3. Try with absolute project path
            Path currentPath = Paths.get(System.getProperty("user.dir"));
            Path projectRoot = findProjectRoot(currentPath, PROJECT_ROOT);
            
            if (projectRoot != null) {
                // Try full path with products directory
                Path imageDirPath = projectRoot.resolve(IMAGES_DIR);
                Path fullImagePath = imageDirPath.resolve(imagePath);
                
                if (Files.exists(fullImagePath)) {
                    resourcePath = fullImagePath.toUri().toString();
                    System.out.println("Found with absolute path: " + resourcePath);
                    return new Image(resourcePath, width, height, true, true);
                }
                
                // Try separate lookup in products directory
                Path productsPath = imageDirPath.resolve(PRODUCTS_DIR);
                if (imagePath.contains("/")) {
                    String fileName = imagePath.substring(imagePath.lastIndexOf('/') + 1);
                    Path productImagePath = productsPath.resolve(fileName);
                    
                    if (Files.exists(productImagePath)) {
                        resourcePath = productImagePath.toUri().toString();
                        System.out.println("Found in products directory: " + resourcePath);
                        return new Image(resourcePath, width, height, true, true);
                    }
                }
            }
            
            // 4. Try as direct file path
            File directFile = new File(imagePath);
            if (directFile.exists()) {
                resourcePath = directFile.toURI().toString();
                System.out.println("Found as direct file: " + resourcePath);
                return new Image(resourcePath, width, height, true, true);
            }
            
            // 5. Try as direct URL
            if (imagePath.startsWith("file:") || imagePath.startsWith("http")) {
                System.out.println("Using as direct URL: " + imagePath);
                return new Image(imagePath, width, height, true, true);
            }
            
            System.out.println("Failed to find image at any location: " + imagePath);
            return null;
            
        } catch (Exception e) {
            System.err.println("Error loading image " + imagePath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Walks up directory tree to find project root
     */
    private static Path findProjectRoot(Path current, String projectDirName) {
        if (current == null) {
            return null;
        }
        
        // Check if current path contains the project directory
        if (current.getFileName() != null && 
            current.getFileName().toString().equals(projectDirName)) {
            return current;
        }
        
        // Check if project directory is a child of current path
        Path potential = current.resolve(projectDirName);
        if (Files.exists(potential)) {
            return potential;
        }
        
        // Go up one level
        return findProjectRoot(current.getParent(), projectDirName);
    }
    
    /**
     * Creates a properly sized and configured ImageView
     */
    public static ImageView createProductImageView(double width, double height) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }
} 