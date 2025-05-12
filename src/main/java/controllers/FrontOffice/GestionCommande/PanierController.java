package controllers.FrontOffice.GestionCommande;

import controllers.FrontOffice.Home;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import Models.gestionCommande.Commande;
import Models.gestionCommande.PanierItem;
import javafx.stage.Stage;
import services.gestionCommande.CommandeService;
import services.gestionCommande.SmsService;
import utils.gestionCommande.PanierSession;

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
    @FXML private Label messageErreurLabel;
    @FXML private Button obtenirCodePromoButton;
    @FXML private Button appliquerCodeButton;
    @FXML private TextField codePromoField;
    @FXML
    public void initialize() {
        afficherPanier();
        obtenirCodePromoButton.setOnAction(e -> obtenirCodePromo());
        appliquerCodeButton.setOnAction(e -> appliquerCodePromo());
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

            // Nom
            Label nomLabel = new Label(item.getProduit().getNom());
            nomLabel.setStyle("-fx-font-weight: bold;");
            nomLabel.setPrefWidth(100);

            // Prix unitaire
            Label prixLabel = new Label(item.getProduit().getPrix() + " DT");
            prixLabel.setPrefWidth(80);

            // Quantité avec boutons
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

            // Total de l'article
            Label totalItemLabel = new Label(item.getTotal() + " DT");
            totalItemLabel.setStyle("-fx-font-weight: bold;");
            totalItemLabel.setPrefWidth(80);

            // Bouton suppression
            Button deleteBtn = new Button("✗");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
            deleteBtn.setPrefWidth(60);

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

        if (reductionAppliquee) {
            float reduction = total * 0.3f;
            reductionLabel.setText(String.format("Réduction de 30%% : -%.2f DT", reduction));
            float totalRemise = total - reduction;
            totalRemiseLabel.setText(String.format("%.2f DT", totalRemise));
        } else {
            reductionLabel.setText("Aucune réduction appliquée");
            totalRemiseLabel.setText(String.format("%.2f DT", total));
        }
    }

    // Variable pour stocker le code promo envoyé
    private String codePromoEnvoye;

    private void obtenirCodePromo() {
        // Remplacez par le numéro de téléphone réel
        String numeroTelephone = "+21651084072"; // Remplacez avec votre numéro

        // Envoie le code promo par SMS et stocke le code
        codePromoEnvoye = SmsService.envoyerCodePromo(numeroTelephone);

        // Affiche le code dans le champ de texte pour l'utilisateur
        codePromoField.setText(codePromoEnvoye); // Affiche le code dans le champ (si nécessaire)
        System.out.println("Code promo envoyé par SMS: " + codePromoEnvoye);
    }
    private boolean reductionAppliquee = false;

    private void appliquerCodePromo() {
        String codePromo = codePromoField.getText();

        if (reductionAppliquee) {
            messageErreurLabel.setText("Le code promo a déjà été appliqué.");
            return;
        }

        if (codePromoEnvoye != null && codePromoEnvoye.equals(codePromo)) {
            reductionAppliquee = true;
            messageErreurLabel.setText("Code promo appliqué avec succès !");
            afficherPanier(); // Réaffiche le panier avec réduction appliquée
        } else {
            messageErreurLabel.setText("Code promo invalide.");
        }
    }

    private boolean reductionActive = false;


    @FXML
    private void payerCommande() {
        if (PanierSession.getPanier().isEmpty()) {
            messageErreurLabel.setText("Votre panier est vide !");
            return;
        }

        float total = 0f;
        int quantiteTotal = 0;

        for (PanierItem item : PanierSession.getPanier()) {
            total += item.getTotal();
            quantiteTotal += item.getQuantite();
        }

        // Calcul des totaux avec réduction
        float tva = total * 0.19f;
        float taxeFixe = 5.0f;
        float montantAvecTaxes = total + tva + taxeFixe;
        float totalFinal = reductionActive ? montantAvecTaxes * 0.7f : montantAvecTaxes;

        float reduction = montantAvecTaxes * 0.3f;

        Commande commande = new Commande(quantiteTotal, new Date(), totalFinal);
        CommandeService service = new CommandeService();
        service.add(commande);

        try {
            // 1. Load Home.fxml (which contains the navbar)
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent homeRoot = homeLoader.load();
            Home homeController = homeLoader.getController();

            // 2. Load the payment content
            FXMLLoader paymentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/GestionCommande/Paiement.fxml"));
            Parent paymentContent = paymentLoader.load();

            // 3. Configure the payment controller
            PaiementController paymentController = paymentLoader.getController();
            paymentController.setCommande(commande);
            paymentController.setMontantFinal(totalFinal);

            // 4. Set the payment content in Home's mainContentPane
            homeController.getMainContentPane().getChildren().setAll(paymentContent);

            // 5. Update the stage
            Stage stage = (Stage) itemsContainer.getScene().getWindow();
            stage.setScene(new Scene(homeRoot));
            stage.show();

            // 6. Clear the cart
            PanierSession.viderPanier();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
