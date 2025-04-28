package controllers.FrontOffice.User;

import models.user;
import javafx.fxml.FXML;
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

    @FXML
    public void initialize() {
        System.out.println("Initializing EditProfileController...");
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            System.out.println("Loading user: " + currentUser.getEmail());
            loadUserData();
        } else {
            System.out.println("No user logged in!");
        }
    }

    private void loadUserData() {
        System.out.println("firstNameField: " + firstNameField);
        System.out.println("lastNameField: " + lastNameField);

        firstNameField.setText(currentUser.getPrenom());
        lastNameField.setText(currentUser.getNom());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getTelephone());

        try {
            String avatarPath = "/images/avatars/" + currentUser.getAvatar();
            System.out.println("Loading avatar from: " + avatarPath);
            Image avatar = new Image(getClass().getResourceAsStream(avatarPath));
            avatarImage.setImage(avatar);
        } catch (Exception e) {
            System.out.println("Error loading avatar, using default");
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
            currentUser.setPrenom(firstNameField.getText());
            currentUser.setNom(lastNameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setTelephone(phoneField.getText());

            if (!newPasswordField.getText().isEmpty()) {
                if (!PasswordUtils.checkSymfonyPassword(currentPasswordField.getText(), currentUser.getPassword())) {
                    showAlert("Error", "Invalid Password", "Current password is incorrect");
                    return;
                }
                currentUser.setPassword(PasswordUtils.hashForSymfony(newPasswordField.getText()));
            }

            if (selectedAvatarFile != null) {
                String avatarPath = saveAvatarToResources(selectedAvatarFile);
                currentUser.setAvatar(avatarPath);
            }

            userService.update(currentUser);

            SessionManager.getInstance().startSession(currentUser);

            showAlert("Success", "Profile Updated", "Your changes have been saved successfully");
            NavigationUtil.navigateTo("/FrontOffice/user/profile.fxml", firstNameField);

        } catch (Exception e) {
            showAlert("Error", "Update Failed", "Failed to update profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String saveAvatarToResources(File avatarFile) throws IOException {
        String resourcesPath = "src/main/resources/images/avatars/";
        String fileName = currentUser.getId() + "_" + avatarFile.getName();
        File destFile = new File(resourcesPath + fileName);

       new File(resourcesPath).mkdirs();

        Files.copy(avatarFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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