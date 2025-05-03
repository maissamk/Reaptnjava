package controllers.FrontOffice.parcelles;

import controllers.FrontOffice.BaseFrontController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import Models.ParcelleRegressionModel.EstimationResultat;
import services.ParcelleEstimationService;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EstimationParcelleController {

    @FXML private ComboBox<String> cbTypeBien;
    @FXML private TextField tfLieu;
    @FXML private ListView<String> lvSuggestions;
    @FXML private TextField tfSurface;
    @FXML private ComboBox<String> cbUnite;
    @FXML private CheckBox chkAccesEau;
    @FXML private CheckBox chkCheminAcces;
    @FXML private CheckBox chkCloture;
    @FXML private CheckBox chkConstruisible;
    @FXML private Button btnEstimer;
    @FXML private Button btnRetour;
    @FXML private VBox resultatSection;
    @FXML private Label lblValeurEstimee;
    @FXML private Label lblPrixMoyen;
    @FXML private TextArea taDetails;
    @FXML private VBox suggestionsContainer;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    private final ParcelleEstimationService estimationService = ParcelleEstimationService.getInstance();

    // Délai pour limiter la fréquence des recherches de suggestions
    private static final int SUGGESTION_DELAY_MS = 300;
    private long lastSuggestionTime = 0;

    @FXML
    public void initialize() {
        // Initialisation du formatteur de devise
        currencyFormat.setMaximumFractionDigits(0);

        // Configuration des types de parcelles
        setupTypeParcelles();

        // Configuration de l'unité de mesure
        setupUnites();

        // Configuration du champ de recherche de lieu
        setupLieuSearch();

        // S'assurer que la section résultat est initialement cachée
        if (resultatSection != null) {
            resultatSection.setVisible(false);
        }
    }

    private void setupTypeParcelles() {
        if (cbTypeBien != null) {
            ObservableList<String> types = FXCollections.observableArrayList(
                    "Terres et prairies",
                    "Forêt",
                    "Terrain agricole",
                    "Terrain résidentiel",
                    "Terrain commercial"
            );
            cbTypeBien.setItems(types);
        }
    }

    private void setupUnites() {
        if (cbUnite != null) {
            ObservableList<String> unites = FXCollections.observableArrayList("ha", "m²");
            cbUnite.setItems(unites);
            cbUnite.setValue("ha"); // Valeur par défaut

            // Convertisseur pour assurer que le texte affiché correspond à la valeur
            cbUnite.setConverter(new StringConverter<String>() {
                @Override
                public String toString(String unite) {
                    return unite;
                }

                @Override
                public String fromString(String string) {
                    return string;
                }
            });
        }
    }

    private void setupLieuSearch() {
        if (tfLieu != null && lvSuggestions != null && suggestionsContainer != null) {
            // Service d'estimation pour les suggestions
            ParcelleEstimationService estimationService = ParcelleEstimationService.getInstance();

            // Initialement, masquer les suggestions et les retirer du flux de layout
            suggestionsContainer.setVisible(false);
            suggestionsContainer.setManaged(false);

            // S'assurer que le conteneur a un z-index supérieur
            suggestionsContainer.setViewOrder(-1.0);

            // Configurer la liste des suggestions
            lvSuggestions.setPrefHeight(150);

            // Style pour les éléments de la liste des suggestions
            lvSuggestions.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-padding: 5 10 5 10;");
                    }
                }
            });

            // Gestion des sélections dans la liste
            lvSuggestions.setOnMouseClicked(event -> {
                String selected = lvSuggestions.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    tfLieu.setText(selected);
                    suggestionsContainer.setVisible(false);
                    suggestionsContainer.setManaged(false);
                }
            });

            // Gestion des touches clavier
            tfLieu.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.DOWN && suggestionsContainer.isVisible()) {
                    lvSuggestions.requestFocus();
                    lvSuggestions.getSelectionModel().selectFirst();
                }
            });

            lvSuggestions.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    String selected = lvSuggestions.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        tfLieu.setText(selected);
                        suggestionsContainer.setVisible(false);
                        suggestionsContainer.setManaged(false);
                        tfLieu.requestFocus();
                    }
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    suggestionsContainer.setVisible(false);
                    suggestionsContainer.setManaged(false);
                    tfLieu.requestFocus();
                }
            });

            // Écouter les changements de texte dans le champ lieu
            tfLieu.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    hideSuggestions();
                    return;
                }

                String trimmed = newValue.trim();

                // Si le texte est vide, montrer les localités populaires
                if (trimmed.isEmpty()) {
                    Platform.runLater(() -> {
                        List<String> popularLocations = estimationService.getSuggestions("");
                        if (popularLocations != null && !popularLocations.isEmpty()) {
                            lvSuggestions.setItems(FXCollections.observableArrayList(popularLocations));
                            showSuggestions();
                        } else {
                            hideSuggestions();
                        }
                    });
                    return;
                }

                // Vérifier le délai
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSuggestionTime <= SUGGESTION_DELAY_MS) {
                    return;
                }

                // Rechercher les suggestions de manière asynchrone
                lastSuggestionTime = currentTime;
                new Thread(() -> {
                    // Utilisez le service pour obtenir les suggestions
                    List<String> suggestions = estimationService.getSuggestions(trimmed);

                    Platform.runLater(() -> {
                        if (suggestions != null && !suggestions.isEmpty()) {
                            lvSuggestions.setItems(FXCollections.observableArrayList(suggestions));
                            showSuggestions();
                        } else {
                            hideSuggestions();
                        }
                    });
                }).start();
            });

            // Montrer les suggestions populaires lorsque le champ obtient le focus
            tfLieu.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) { // Si le champ obtient le focus
                    // Afficher les suggestions populaires immédiatement
                    Platform.runLater(() -> {
                        List<String> popularLocations = estimationService.getSuggestions("");
                        if (popularLocations != null && !popularLocations.isEmpty()) {
                            lvSuggestions.setItems(FXCollections.observableArrayList(popularLocations));
                            showSuggestions();
                        }
                    });
                } else {
                    // Délai pour permettre de cliquer sur une suggestion
                    new Thread(() -> {
                        try {
                            Thread.sleep(200);
                            Platform.runLater(() -> {
                                if (!lvSuggestions.isFocused()) {
                                    hideSuggestions();
                                }
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }
            });
        }
    }

    // Méthodes auxiliaires pour gérer la visibilité des suggestions
    private void showSuggestions() {
        if (suggestionsContainer != null) {
            suggestionsContainer.setVisible(true);
            suggestionsContainer.setManaged(true);
        }
    }

    private void hideSuggestions() {
        if (suggestionsContainer != null) {
            suggestionsContainer.setVisible(false);
            suggestionsContainer.setManaged(false);
        }
    }

    @FXML
    private void handleEstimation() {
        // Validation des champs
        if (cbTypeBien.getValue() == null || tfLieu.getText().isEmpty() || tfSurface.getText().isEmpty()) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Récupération des valeurs
        String typeBien = cbTypeBien.getValue();
        String lieu = tfLieu.getText();
        double surface;

        try {
            surface = Double.parseDouble(tfSurface.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "La surface doit être un nombre valide.");
            return;
        }

        // Conversion en m² si nécessaire
        if (cbUnite.getValue().equals("ha")) {
            surface = surface * 10000; // 1 hectare = 10000 m²
        }

        // Caractéristiques du terrain
        boolean accesEau = chkAccesEau != null && chkAccesEau.isSelected();
        boolean cheminAcces = chkCheminAcces != null && chkCheminAcces.isSelected();
        boolean cloture = chkCloture != null && chkCloture.isSelected();
        boolean construisible = chkConstruisible != null && chkConstruisible.isSelected();

        // Utilisation du service d'estimation pour estimer la valeur
        EstimationResultat resultat = estimationService.estimerParcelle(
                typeBien, lieu, surface, accesEau, cheminAcces, cloture, construisible
        );

        // Affichage des résultats
        displayResults(resultat, typeBien, lieu, surface, accesEau, cheminAcces, cloture, construisible);
    }

    private void displayResults(EstimationResultat resultat, String typeBien, String lieu,
                                double surface, boolean accesEau, boolean cheminAcces,
                                boolean cloture, boolean construisible) {
        // Formatage des valeurs monétaires
        lblValeurEstimee.setText(currencyFormat.format(resultat.getValeurTotale()).replace("€", "TND"));
        lblPrixMoyen.setText(currencyFormat.format(resultat.getPrixAuM2()).replace("€", "TND") + "/m²");

        // Construction des détails
        StringBuilder details = new StringBuilder();
        details.append("Type de bien: ").append(typeBien).append("\n");
        details.append("Localisation: ").append(lieu).append("\n");

        // Formatage de la surface avec séparateur de milliers
        String surfaceFormatted = NumberFormat.getNumberInstance(Locale.FRANCE).format(surface);
        details.append("Surface: ").append(surfaceFormatted).append(" m²").append("\n\n");

        details.append("Facteurs impactant l'estimation:\n");
        if (accesEau) details.append("- Accès à l'eau (+15%)\n");
        if (cheminAcces) details.append("- Chemin d'accès (+10%)\n");
        if (cloture) details.append("- Terrain clôturé (+5%)\n");
        if (construisible) details.append("- Terrain constructible (+50%)\n");
        details.append("\n* Estimation réalisée par modèle d'IA basée sur les données du marché tunisien");

        taDetails.setText(details.toString());

        // Afficher la section résultat
        resultatSection.setVisible(true);
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/Afficherparcelles.fxml"));
            Parent content = contentLoader.load();

            baseController.getContentPane().getChildren().setAll(content);

            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(baseRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de revenir à la page précédente : " + e.getMessage());
        }
    }

    @FXML
    private void handleGenererRapport() {
        // Cette méthode pourrait être implémentée pour générer un rapport PDF détaillé
        // Afficher un message temporaire
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité en cours de développement");
        alert.setHeaderText(null);
        alert.setContentText("La génération de rapport détaillé sera disponible dans une prochaine version.");
        alert.showAndWait();
    }

    @FXML
    private void handleGuidePrice() {
        // Méthode pour afficher le guide des prix
        showInfoDialog("Guide des prix",
                "Guide des prix moyens par région et type de terrain\n\n" +
                        "Terres agricoles:\n" +
                        "- Tunis/Grand Tunis: 30-40 TND/m²\n" +
                        "- Sousse/Monastir: 25-35 TND/m²\n" +
                        "- Sfax: 20-30 TND/m²\n" +
                        "- Nabeul/Cap Bon: 20-30 TND/m²\n" +
                        "- Autres régions: 10-25 TND/m²\n\n" +
                        "Terrains résidentiels:\n" +
                        "- Tunis/Grand Tunis: 1000-1500 TND/m²\n" +
                        "- Sousse/Hammamet: 800-1200 TND/m²\n" +
                        "- Sfax: 600-900 TND/m²\n" +
                        "- Nabeul/Cap Bon: 700-1100 TND/m²\n" +
                        "- Autres régions: 400-800 TND/m²");
    }

    @FXML
    private void handleConseilsPratiques() {
        // Méthode pour afficher les conseils pratiques
        showInfoDialog("Conseils pratiques pour l'achat de terrain",
                "Avant d'acheter un terrain:\n\n" +
                        "1. Vérifiez le statut juridique et le titre de propriété\n" +
                        "2. Consultez le plan d'aménagement de la zone\n" +
                        "3. Faites analyser la qualité du sol si c'est un terrain agricole\n" +
                        "4. Vérifiez les accès aux utilités (eau, électricité)\n" +
                        "5. Renseignez-vous sur les projets d'infrastructure à proximité\n" +
                        "6. Consultez un expert immobilier pour valider l'estimation\n" +
                        "7. Examinez les contraintes environnementales potentielles\n\n" +
                        "Pour maximiser votre investissement, privilégiez les terrains:\n" +
                        "- Bien desservis par les transports\n" +
                        "- Proches des commodités\n" +
                        "- Dans des zones en développement");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Configuration pour permettre le texte plus long et le formatage
        alert.getDialogPane().setMinHeight(400);
        alert.getDialogPane().setMinWidth(500);

        // Utiliser un TextArea pour permettre le défilement
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(300);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    // Méthode de débogage pour vérifier l'état des suggestions
    public void debugSuggestions() {
        System.out.println("Suggestions container visible: " + suggestionsContainer.isVisible());
        System.out.println("Suggestions container managed: " + suggestionsContainer.isManaged());
        System.out.println("Champ de recherche focused: " + tfLieu.isFocused());
        System.out.println("Nombre d'éléments dans la liste: " +
                (lvSuggestions.getItems() != null ? lvSuggestions.getItems().size() : 0));
    }
}