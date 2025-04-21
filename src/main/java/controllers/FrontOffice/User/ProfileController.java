package controllers.FrontOffice.User;

import Models.user;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import utils.SessionManager;
import utils.NavigationUtil;

public class ProfileController {
    @FXML private Label welcomeLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private ImageView avatarImage;

    @FXML
    public void initialize() {
        loadUserProfile();
    }

    private void loadUserProfile() {
        user currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getPrenom() + "!");
            fullNameLabel.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            emailLabel.setText(currentUser.getEmail());
            phoneLabel.setText(currentUser.getTelephone());
            roleLabel.setText(currentUser.getRoles());
            statusLabel.setText(currentUser.getStatus() != null ? currentUser.getStatus() : "Active");

            try {
                String avatarPath = currentUser.getAvatar();
                if (avatarPath == null || avatarPath.isEmpty()) {
                    avatarPath = "/images/defaultavatar.png";
                } else {
                    // Ensure path starts with /images/
                    if (!avatarPath.startsWith("/images/avatars/")) {
                        avatarPath = "/images/avatars/" + avatarPath;
                    }
                }

                System.out.println("Loading avatar from: " + avatarPath); // Debug
                Image avatar = new Image(getClass().getResourceAsStream(avatarPath));
                avatarImage.setImage(avatar);
            } catch (Exception e) {
                System.err.println("Error loading avatar: " + e.getMessage());
                avatarImage.setImage(new Image(getClass().getResourceAsStream("/images/defaultavatar.png")));
            }
        }
    }

    @FXML
    private void handleEditProfile() {
        NavigationUtil.navigateTo("/FrontOffice/user/EditProfile.fxml", welcomeLabel);
    }

    @FXML
    private void handleBack() {
        NavigationUtil.navigateTo("/FrontOffice/Home.fxml", welcomeLabel);
    }

    public void handleUserList(ActionEvent actionEvent) {
        NavigationUtil.navigateTo("/BackOffice/UserList.fxml", welcomeLabel);
    }
}