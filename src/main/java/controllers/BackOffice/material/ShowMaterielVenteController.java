package controllers.BackOffice.material;

import Models.MaterielVente;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.MaterielService;
import java.io.File;


public class ShowMaterielVenteController {
    @FXML private ImageView imageView;
    @FXML private Label nomLabel;
    @FXML private Label prixLabel;
    @FXML private TextArea descriptionLabel;
    @FXML private Label disponibiliteLabel;
    @FXML private Label categorieLabel;
    @FXML private Label createdAtLabel;
    MaterielService service = new MaterielService();
    public void setMateriel(MaterielVente materiel) {
        File imageFile = new File("C:/Users/romdh/Downloads/pi2025/pi2025/public/uploads/images/", materiel.getImage());
        if (imageFile.exists()) {
            imageView.setImage(new Image(imageFile.toURI().toString()));
        } else {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.png")));
        }
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(false);

        nomLabel.setText(materiel.getNom());
        prixLabel.setText(String.format("%.2f TND", materiel.getPrix()));
        descriptionLabel.setText(materiel.getDescription());
        disponibiliteLabel.setText(materiel.isDisponibilite() ? "Disponible" : "Non disponible");
        disponibiliteLabel.setStyle("-fx-text-fill: " + (materiel.isDisponibilite() ? "#4CAF50" : "#F44336") + ";");
        categorieLabel.setText("Catégorie: " + materiel.getCategorie());
        createdAtLabel.setText("Ajouté le: " + materiel.getCreatedAt().toLocalDate());
    }


}