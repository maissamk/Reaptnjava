package controllers.FrontOffice.GestionCommande;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Models.gestionCommande.ProduitTestN;
import utils.gestionCommande.PanierSession;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ProduitTestNController {

    @FXML
    private VBox produitsContainer;

    @FXML
    public void initialize() {
        List<ProduitTestN> produits = List.of(
                new ProduitTestN("Produit A", 10.5f, "Description A"),
                new ProduitTestN("Produit B", 25.0f, "Description B"),
                new ProduitTestN("Produit C", 40.0f, "Description C")
        );

        for (ProduitTestN p : produits) {
            HBox box = new HBox(10);
            Label nom = new Label(p.getNom());
            Label prix = new Label(p.getPrix() + " DT");
            Button ajouterBtn = new Button("Ajouter au panier");

            ajouterBtn.setOnAction(e -> {
                PanierSession.ajouterProduit(p, 1);
            });

            box.getChildren().addAll(nom, prix, ajouterBtn);
            produitsContainer.getChildren().add(box);
        }
    }

    @FXML
    private void openPanier() {
        try {
            URL fxmlLocation = getClass().getResource("/FrontOffice/GestionCommande/PanierView.fxml");
            if (fxmlLocation == null) {
                System.out.println("Le fichier FXML est introuvable !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());

            // Ajout explicite de la feuille de style
            scene.getStylesheets().add(getClass().getResource("/css/panier.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Mon Panier");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
