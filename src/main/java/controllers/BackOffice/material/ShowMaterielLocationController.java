package controllers.BackOffice.material;

import Models.MaterielLocation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ShowMaterielLocationController {
    @FXML private ImageView imageView;
    @FXML private Label nomLabel;
    @FXML private Label prixLabel;
    @FXML private TextArea descriptionLabel;
    @FXML private Label disponibiliteLabel;
    @FXML private Label createdAtLabel;

    public void setMateriel(MaterielLocation materiel) {
        try {
            imageView.setImage(new Image("file:C:/Users/romdh/Downloads/pi2025/pi2025/public/uploads/images/" + materiel.getImage()));
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.png")));
        }
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(false);

        nomLabel.setText(materiel.getNom());
        prixLabel.setText(String.format("%.2f TND/jour", materiel.getPrix()));
        descriptionLabel.setText(materiel.getDescription());
        disponibiliteLabel.setText(materiel.isDisponibilite() ? "Disponible" : "Non disponible");
        disponibiliteLabel.setStyle("-fx-text-fill: " + (materiel.isDisponibilite() ? "#4CAF50" : "#F44336") + ";");
     }

   
}