package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.ProduitTestN;
import utils.PanierSession;

import java.io.IOException;
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
    private void openPanier() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/PanierView.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Mon Panier");
        stage.show();
    }
}
