package controllers.FrontOffice.GestionCommande;

import controllers.FrontOffice.Home;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import Models.gestionCommande.Livraison;
import javafx.stage.Stage;
import services.gestionCommande.LivraisonService;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SuiviLivraisonController {

    @FXML
    private Label adresseLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox step1Box, step2Box, step3Box, step4Box;

    private LivraisonService livraisonService = new LivraisonService();
    private Livraison currentLivraison;
    private Timer refreshTimer = new Timer();

    public void initialize() {
        // Initialisation automatique par JavaFX
    }

    public void setCommandeId(int commandeId) {
        currentLivraison = livraisonService.findByCommandeId(commandeId);
        if (currentLivraison != null) {
            setLivraison(currentLivraison);
            startAutoRefresh(commandeId);
        }
    }

    public void setLivraison(Livraison livraison) {
        adresseLabel.setText("Adresse de livraison : " + livraison.getAdresse());
        dateLabel.setText("Date estimée : " + livraison.getDateLiv());
        statusLabel.setText("Statut : " + livraison.getStatus());

        updateProgressSteps(livraison.getStatus());
    }

    private void updateProgressSteps(String status) {
        resetStepStyles();

        switch (status) {
            case "Validation en cours" -> highlightStep(step1Box);
            case "En préparation" -> {
                highlightStep(step1Box);
                highlightStep(step2Box);
            }
            case "Expédiée" -> {
                highlightStep(step1Box);
                highlightStep(step2Box);
                highlightStep(step3Box);
            }
            case "Livrée" -> {
                highlightStep(step1Box);
                highlightStep(step2Box);
                highlightStep(step3Box);
                highlightStep(step4Box);
            }
        }
    }

    private void resetStepStyles() {
        VBox[] steps = {step1Box, step2Box, step3Box, step4Box};
        for (VBox box : steps) {
            for (var node : box.getChildren()) {
                if (node instanceof StackPane stackPane) {
                    stackPane.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 2px; -fx-border-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40;");
                } else if (node instanceof Label label) {
                    // Texte toujours visible, en gris clair
                    label.setTextFill(Color.GRAY);
                    label.setFont(new Font(12));
                }
            }
        }
    }

    private void highlightStep(VBox box) {
        String color = switch (box.getId()) {
            case "step1Box" -> "#4CAF50"; // Vert
            case "step2Box" -> "#4CAF50"; // Orange
            case "step3Box" -> "#4CAF50"; // Bleu
            case "step4Box" -> "#4CAF50"; // Rose
            default -> "#4CAF50";
        };

        for (var node : box.getChildren()) {
            if (node instanceof StackPane stackPane) {
                stackPane.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40;");
            } else if (node instanceof Label label) {
                label.setTextFill(Color.BLACK); // Texte actif en noir
                label.setFont(new Font(13));
            }
        }
    }

    @FXML
    private void suivreAutreCommande() {
        try {
            // 1. Load Home.fxml (which contains the navbar)
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent homeRoot = homeLoader.load();
            Home homeController = homeLoader.getController();

            // 2. Load the history content
            FXMLLoader historyLoader = new FXMLLoader(getClass().getResource("/FrontOffice/GestionCommande/Historique.fxml"));
            Parent historyContent = historyLoader.load();

            // 3. Set the history content in Home's mainContentPane
            homeController.getMainContentPane().getChildren().setAll(historyContent);

            // 4. Update the stage
            Stage stage = (Stage) adresseLabel.getScene().getWindow();
            Scene scene = new Scene(homeRoot);

            // Apply CSS if needed
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startAutoRefresh(int commandeId) {
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Livraison updated = livraisonService.findByCommandeId(commandeId);
                if (updated != null && !updated.getStatus().equals(currentLivraison.getStatus())) {
                    currentLivraison = updated;
                    Platform.runLater(() -> setLivraison(currentLivraison));
                }
            }
        }, 0, 10000); // toutes les 10 secondes
    }
// event listener
    public void stopAutoRefresh() {
        refreshTimer.cancel();
    }
}
