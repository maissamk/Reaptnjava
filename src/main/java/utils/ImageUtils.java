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

    // Base directory where uploaded images are stored
    private static final String UPLOADS_BASE_DIR = "C:/Users/romdh/Downloads/pi2025/pi2025/public/uploads/images/";
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
            // 1. First try the new uploads directory
            if (imagePath.startsWith("uploads/images/")) {
                String fileName = imagePath.replace("uploads/images/", "");
                Path uploadsPath = Paths.get(UPLOADS_BASE_DIR, fileName);
                if (Files.exists(uploadsPath)) {
                    resourcePath = uploadsPath.toUri().toString();
                    System.out.println("Found in uploads directory: " + resourcePath);
                    return new Image(resourcePath, width, height, true, true);
                }
            }

            // 2. Try direct resource lookup (for legacy images)
            URL imageUrl = ImageUtils.class.getResource("/images/" + imagePath);
            if (imageUrl != null) {
                resourcePath = imageUrl.toExternalForm();
                System.out.println("Found as resource: " + resourcePath);
                return new Image(resourcePath, width, height, true, true);
            }

            // 3. If not found and starts with 'products/', try without that prefix
            if (imagePath.startsWith(PRODUCTS_DIR + "/")) {
                String imageName = imagePath.substring((PRODUCTS_DIR + "/").length());
                imageUrl = ImageUtils.class.getResource("/images/" + PRODUCTS_DIR + "/" + imageName);
                if (imageUrl != null) {
                    resourcePath = imageUrl.toExternalForm();
                    System.out.println("Found as resource with modified path: " + resourcePath);
                    return new Image(resourcePath, width, height, true, true);
                }
            }

            // 4. Try with absolute project path (for legacy images)
            Path currentPath = Paths.get(System.getProperty("user.dir"));
            Path imageDirPath = currentPath.resolve(IMAGES_DIR);
            Path fullImagePath = imageDirPath.resolve(imagePath);

            if (Files.exists(fullImagePath)) {
                resourcePath = fullImagePath.toUri().toString();
                System.out.println("Found with absolute path: " + resourcePath);
                return new Image(resourcePath, width, height, true, true);
            }

            // 5. Try separate lookup in products directory (for legacy images)
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

            // 6. Try as direct file path
            File directFile = new File(imagePath);
            if (directFile.exists()) {
                resourcePath = directFile.toURI().toString();
                System.out.println("Found as direct file: " + resourcePath);
                return new Image(resourcePath, width, height, true, true);
            }

            // 7. Try as direct URL
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