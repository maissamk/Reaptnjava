package tn.esprit.controllers.category;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.entities.Categorie;

public class ShowCategorieController {
    @FXML private Label idLabel;
    @FXML private Label nomLabel;
    @FXML private Label descLabel;

    public void setCategorie(Categorie categorie) {
        idLabel.setText(String.valueOf(categorie.getId()));
        nomLabel.setText(categorie.getNom());
        descLabel.setText(categorie.getDescription());
    }
}