package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import models.Livraison;
import services.LivraisonService;

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
        // Appelé automatiquement par JavaFX si besoin
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
                if (node instanceof Label label) {
                    label.setTextFill(Color.GRAY);
                    label.setFont(new Font(14));
                }
            }
        }
    }

    private void highlightStep(VBox box) {
        for (var node : box.getChildren()) {
            if (node instanceof Label label) {
                label.setTextFill(Color.GREEN);
                label.setFont(new Font(16));
            }
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

    public void stopAutoRefresh() {
        refreshTimer.cancel();
    }
}
