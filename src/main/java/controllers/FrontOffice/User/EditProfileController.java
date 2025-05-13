package controllers.FrontOffice.User;

import Models.user;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import services.UserServices;
import utils.SessionManager;
import utils.PasswordUtils;
import utils.NavigationUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class EditProfileController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ImageView avatarImage;

    private final UserServices userService = new UserServices();
    private user currentUser;
    private File selectedAvatarFile;
    private final String AVATAR_BASE_PATH = "C:/Users/romdh/Downloads/pi2025/pi2025/public/uploads/avatars/";

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadUserData();
        } else {
            NavigationUtil.navigateTo("/FrontOffice/user/login.fxml", (Node) firstNameField.getScene().getRoot());
        }
    }

    private void loadUserData() {
        firstNameField.setText(currentUser.getPrenom());
        lastNameField.setText(currentUser.getNom());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getTelephone());

        try {
            if (currentUser.getAvatar() == null || currentUser.getAvatar().isEmpty()) {
                // Load default avatar from resources if no avatar is set
                avatarImage.setImage(new Image(getClass().getResourceAsStream("/images/defaultavatar.png")));
                return;
            }

            String avatarPath = AVATAR_BASE_PATH + currentUser.getAvatar();
            System.out.println("Loading avatar from: " + avatarPath);

            File file = new File(avatarPath);
            if (file.exists()) {
                Image avatar = new Image(file.toURI().toString());
                avatarImage.setImage(avatar);
            } else {
                System.out.println("Avatar file not found, using default");
                avatarImage.setImage(new Image(getClass().getResourceAsStream("/images/defaultavatar.png")));
            }
        } catch (Exception e) {
            System.out.println("Error loading avatar: " + e.getMessage());
            avatarImage.setImage(new Image(getClass().getResourceAsStream("/images/defaultavatar.png")));
        }
    }

    @FXML
    private void handleChangeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        selectedAvatarFile = fileChooser.showOpenDialog(avatarImage.getScene().getWindow());
        if (selectedAvatarFile != null) {
            Image image = new Image(selectedAvatarFile.toURI().toString());
            avatarImage.setImage(image);
        }
    }

    @FXML
    private void handleSaveChanges() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Update user data
            currentUser.setPrenom(firstNameField.getText());
            currentUser.setNom(lastNameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setTelephone(phoneField.getText());

            // Handle password change if needed
            if (!newPasswordField.getText().isEmpty()) {
                if (!PasswordUtils.checkSymfonyPassword(currentPasswordField.getText(), currentUser.getPassword())) {
                    showAlert("Error", "Invalid Password", "Current password is incorrect");
                    return;
                }
                currentUser.setPassword(PasswordUtils.hashForSymfony(newPasswordField.getText()));
            }

            // Handle avatar change if needed
            if (selectedAvatarFile != null) {
                String avatarPath = saveAvatarToUploads(selectedAvatarFile);
                currentUser.setAvatar(avatarPath);
            }

            // Save changes
            userService.update(currentUser);
            SessionManager.getInstance().startSession(currentUser);
            showAlertAndNavigate("Success", "Profile Updated",
                    "Your changes have been saved successfully",
                    "/FrontOffice/user/profile.fxml");

        } catch (Exception e) {
            showAlert("Error", "Update Failed", "Failed to update profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String saveAvatarToUploads(File avatarFile) throws IOException {
        // Create directory if it doesn't exist
        File targetDir = new File(AVATAR_BASE_PATH);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // Generate unique filename (using user ID and timestamp)
        String fileExtension = avatarFile.getName().substring(avatarFile.getName().lastIndexOf("."));
        String fileName = currentUser.getId() + "_" + System.currentTimeMillis() + fileExtension;
        File destFile = new File(targetDir, fileName);

        // Copy the file
        Files.copy(avatarFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Return just the filename (without path)
        return fileName;
    }

    private boolean validateInputs() {
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                emailField.getText().isEmpty() || phoneField.getText().isEmpty()) {
            showAlert("Error", "Missing Fields", "Please fill all required fields");
            return false;
        }

        if (!newPasswordField.getText().isEmpty()) {
            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showAlert("Error", "Password Mismatch", "New passwords do not match");
                return false;
            }

            if (newPasswordField.getText().length() < 8) {
                showAlert("Error", "Weak Password", "Password must be at least 8 characters");
                return false;
            }
        }

        return true;
    }

    private void showAlertAndNavigate(String title, String header, String content, String fxmlPath) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.setOnHidden(event -> {
            NavigationUtil.navigateTo(fxmlPath, firstNameField);
        });
        alert.show();
    }

    @FXML
    private void handleCancel() {
        NavigationUtil.navigateTo("/FrontOffice/user/profile.fxml", firstNameField);
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}