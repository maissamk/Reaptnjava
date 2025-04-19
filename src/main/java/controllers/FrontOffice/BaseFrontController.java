package controllers.FrontOffice;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class BaseFrontController {

    @FXML
    private AnchorPane contentPane;

    public AnchorPane getContentPane() {
        return contentPane;
    }
}