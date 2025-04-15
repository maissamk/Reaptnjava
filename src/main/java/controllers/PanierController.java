package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.Stage;
import models.Commande;
import models.PanierItem;
import services.CommandeService;
import utils.PanierSession;

import java.io.IOException;
import java.util.Date;

public class PanierController {

    @FXML private VBox itemsContainer;
    @FXML private Label sousTotalLabel;
    @FXML private Label tvaLabel;
    @FXML private Label taxeFixeLabel;
    @FXML private Label totalLabel;
    @FXML private Label reductionLabel;
    @FXML private Label totalRemiseLabel;

    @FXML
    public void initialize() {
        afficherPanier();
    }

    private void afficherPanier() {
        itemsContainer.getChildren().clear();

        float sousTotal = 0;

        for (PanierItem item : PanierSession.getPanier()) {
            HBox itemBox = new HBox(15);
            itemBox.setAlignment(Pos.CENTER_LEFT);
            itemBox.setStyle("-fx-padding: 10px; -fx-border-color: #eee; -fx-border-width: 0 0 1px 0;");

            // Placeholder pour l'image
            Pane imagePlaceholder = new Pane();
            imagePlaceholder.setPrefSize(80, 80);
            imagePlaceholder.setStyle("-fx-background-color: #f0f0f0;");

            // Nom (colonne)
            Label nomLabel = new Label(item.getProduit().getNom());
            nomLabel.setStyle("-fx-font-weight: bold;");
            nomLabel.setPrefWidth(100);

            // Prix (colonne)
            Label prixLabel = new Label(item.getProduit().getPrix() + " DT");
            prixLabel.setPrefWidth(80);

            // Contrôle de quantité
            HBox quantityBox = new HBox(5);
            quantityBox.setAlignment(Pos.CENTER);
            Button decrementBtn = new Button("-");
            decrementBtn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ddd;");
            Label quantityLabel = new Label(String.valueOf(item.getQuantite()));
            quantityLabel.setStyle("-fx-padding: 0 10px;");
            Button incrementBtn = new Button("+");
            incrementBtn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ddd;");
            quantityBox.getChildren().addAll(decrementBtn, quantityLabel, incrementBtn);
            quantityBox.setPrefWidth(100);

            // Total pour l'article
            Label totalItemLabel = new Label(item.getTotal() + " DT");
            totalItemLabel.setStyle("-fx-font-weight: bold;");
            totalItemLabel.setPrefWidth(80);

            // Bouton de suppression
            Button deleteBtn = new Button("✗");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
            deleteBtn.setPrefWidth(60);

            // Ajouter tous les éléments dans le bon ordre (colonne par colonne)
            itemBox.getChildren().addAll(
                    imagePlaceholder,
                    nomLabel,
                    prixLabel,
                    quantityBox,
                    totalItemLabel,
                    deleteBtn
            );
            itemsContainer.getChildren().add(itemBox);

            sousTotal += item.getTotal();

            // Gestion des événements
            decrementBtn.setOnAction(e -> {
                PanierSession.retirerProduit(item.getProduit());
                afficherPanier();
            });

            incrementBtn.setOnAction(e -> {
                PanierSession.ajouterProduit(item.getProduit(), 1);
                afficherPanier();
            });

            deleteBtn.setOnAction(e -> {
                PanierSession.supprimerProduit(item.getProduit());
                afficherPanier();
            });
        }


        // Mise à jour des totaux
        updateTotals(sousTotal);
    }

    private void updateTotals(float sousTotal) {
        sousTotalLabel.setText(String.format("%.2f DT", sousTotal));

        float tva = sousTotal * 0.19f;
        tvaLabel.setText(String.format("%.2f DT", tva));

        float taxeFixe = 5.00f;
        taxeFixeLabel.setText(String.format("%.2f DT", taxeFixe));

        float total = sousTotal + tva + taxeFixe;
        totalLabel.setText(String.format("%.2f DT", total));

        float reduction = total * 0.3f;
        reductionLabel.setText(String.format("Réduction de 30%% : -%.2f DT", reduction));

        float totalRemise = total - reduction;
        totalRemiseLabel.setText(String.format("%.2f DT", totalRemise));
    }
    @FXML private Label messageErreurLabel;

    @FXML
    private void payerCommande() {
        if (PanierSession.getPanier().isEmpty()) {
            messageErreurLabel.setText(" Votre panier est vide !");
            return;
        }

        float total = 0f;
        int quantiteTotal = 0;

        for (PanierItem item : PanierSession.getPanier()) {
            total += item.getTotal();
            quantiteTotal += item.getQuantite();
        }

        Commande commande = new Commande(quantiteTotal, new Date(), total);
        CommandeService service = new CommandeService();
        service.add(commande); // Ajoute dans la base

        // Ouvrir interface de paiement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Paiement.fxml"));
            Parent root = loader.load();

            PaiementController controller = loader.getController();
            controller.setCommande(commande);

            // Remplacer la scène actuelle (dans la même fenêtre)
            messageErreurLabel.getScene().setRoot(root);

            PanierSession.viderPanier();
            afficherPanier();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}