package controllers.FrontOffice.GestionCommande;

import Models.gestionCommande.DiagnosticModel;
import Models.gestionCommande.PlantSuggestion;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.gestionCommande.ImageAnalysisService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class AnalyzeImageController {

    @FXML private ImageView imageView;
    @FXML private Button btnChoose;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label lblError;
    @FXML private ListView<PlantSuggestion> listViewSuggestions;
    @FXML private ListView<PlantSuggestion> listViewHealth;

    @FXML
    public void initialize() {
        progressIndicator.setVisible(false);
        lblError.setText("");
        listViewHealth.getItems().clear();

        // Onglet Identification
        listViewSuggestions.setCellFactory(lv -> new IdentificationCell());
        // Onglet Diagnostic de Santé
        listViewHealth.setCellFactory(lv -> new HealthCell());

        btnChoose.setOnAction(e -> onChooseImage());
    }

    private void onChooseImage() {
        lblError.setText("");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sélectionner une image de plante");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images JPG/PNG","*.jpg","*.jpeg","*.png")
        );
        File file = chooser.showOpenDialog(btnChoose.getScene().getWindow());
        if (file == null) return;

        imageView.setImage(new Image(file.toURI().toString()));

        Task<DiagnosticModel> task = new Task<>() {
            @Override
            protected DiagnosticModel call() throws Exception {
                DiagnosticModel model = ImageAnalysisService.analyzePlantImage(file.toPath());
                if (model.getSuggestions() == null) {
                    model.setSuggestions(Collections.emptyList());
                }
                return model;
            }
        };

        progressIndicator.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(evt -> {
            List<PlantSuggestion> suggestions = task.getValue().getSuggestions();
            listViewSuggestions.getItems().setAll(suggestions);
            listViewHealth.getItems().setAll(suggestions);
        });

        task.setOnFailed(evt -> {
            lblError.setText("Erreur : " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // Nouvelle méthode pour la navigation entre écrans
    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            lblError.setText("Erreur de navigation: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour ouvrir un navigateur sans dépendre de Main
    private void openInBrowser(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            lblError.setText("Erreur lors de l'ouverture du navigateur: " + e.getMessage());
        }
    }

    private class IdentificationCell extends ListCell<PlantSuggestion> {
        private final Label lblName = new Label();
        private final Label lblProb = new Label();
        private final Label lblRisk = new Label();

        @Override
        protected void updateItem(PlantSuggestion s, boolean empty) {
            super.updateItem(s, empty);
            if (empty || s == null) {
                setGraphic(null);
            } else {
                lblName.setText(s.getPlantName());
                double p = s.getProbability();
                lblProb.setText(String.format("%.2f%%", p * 100));
                if (p >= 0.7)      lblRisk.setStyle("-fx-background-color:red;-fx-text-fill:white;");
                else if (p >= 0.4) lblRisk.setStyle("-fx-background-color:orange;-fx-text-fill:white;");
                else               lblRisk.setStyle("-fx-background-color:green;-fx-text-fill:white;");
                lblRisk.setText(p >= 0.7 ? "Haut risque" : p >= 0.4 ? "Risque moyen" : "Faible risque");

                HBox h = new HBox(10, lblName, lblProb, lblRisk);
                h.setStyle("-fx-padding:8; -fx-alignment:center-left;");
                setGraphic(h);
            }
        }
    }

    private class HealthCell extends ListCell<PlantSuggestion> {
        private final Label lblName   = new Label();
        private final Button btnLink  = new Button("→");
        private final Label lblPrompt = new Label("Pour consulter plus de détails");

        public HealthCell() {
            btnLink.setOnAction(e -> {
                PlantSuggestion s = getItem();
                if (s != null) {
                    String query = URLEncoder
                            .encode(s.getPlantName() + " plant disease", StandardCharsets.UTF_8);
                    // Remplacer l'appel à Main par la méthode locale
                    openInBrowser("https://www.google.com/search?q=" + query);
                }
            });
        }

        @Override
        protected void updateItem(PlantSuggestion s, boolean empty) {
            super.updateItem(s, empty);
            if (empty || s == null) {
                setGraphic(null);
            } else {
                lblName.setText(s.getPlantName());
                HBox line = new HBox(10, lblName, btnLink);
                VBox box = new VBox(2, line, lblPrompt);
                box.setStyle("-fx-padding:8; -fx-alignment:TOP_LEFT;");
                setGraphic(box);
            }
        }
    }

    @FXML
    private void handleCommande(ActionEvent event) {
        navigateTo("/FrontOffice/GestionCommande/analyze_image.fxml", event);
    }
}