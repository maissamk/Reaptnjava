package controllers.FrontOffice;

import Models.user;
import controllers.FrontOffice.material.client.ShowMaterielLocationController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import services.UserServices;
import utils.SessionManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class BaseFrontController {

    public ImageView userAvatar;
    public Label userNameLabel;
    public Label userRoleLabel;
    public Button loginButton;
    public Button profileButton;
    @FXML
    private AnchorPane contentPane;



    public AnchorPane getContentPane() {
        return contentPane;
    }
}