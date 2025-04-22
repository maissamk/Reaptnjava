package controllers.FrontOffice.parcelles;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.ParcelleProprietes;

public class DetailParcelle {

    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label detailsLabel;
    @FXML private ImageView imageView;

    public void initData(ParcelleProprietes parcelle) {
        titleLabel.setText(parcelle.getTitre());
        priceLabel.setText(String.format("%.2f DT", parcelle.getPrix()));

        String details = String.format(
                "Type: %s\nStatut: %s\nTaille: %.2f m²\nPropriétaire: %s\nContact: %s",
                parcelle.getType_terrain(),
                parcelle.getStatus(),
                parcelle.getTaille(),
                parcelle.getNom_proprietaire(),
                parcelle.getContact_proprietaire()
        );
        detailsLabel.setText(details);

        try {
            Image image = new Image(getClass().getResourceAsStream("/" + parcelle.getImage()));
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.png")));
        }
    }
}