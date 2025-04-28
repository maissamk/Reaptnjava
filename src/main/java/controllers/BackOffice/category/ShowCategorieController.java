package controllers.BackOffice.category;

import models.Categorie;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ShowCategorieController {
    @FXML private Label idLabel;
    @FXML private Label nomLabel;
    @FXML private TextArea descLabel;

    public void setCategorie(Categorie categorie) {
        idLabel.setText(String.valueOf(categorie.getId()));
        nomLabel.setText(categorie.getNom());
        descLabel.setText(categorie.getDescription());
    }
}