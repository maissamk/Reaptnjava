package controllers.FrontOffice.material.client;

import Models.MaterielVente;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import services.MaterielService;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
 import services.MaterielService;
import services.SimpleImageAnalysisService;
import utils.SessionManager;
import utils.gestionCommande.PanierSession;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ShowMaterielVenteController implements Initializable {

    @FXML private ImageView imageView;
    @FXML private Label nomLabel;
    @FXML private Label prixLabel;
    @FXML private TextArea descriptionLabel;
    @FXML private Label disponibiliteLabel;
    @FXML private Label categorieLabel;
    @FXML private Label createdAtLabel;
    @FXML private Button acheter;
    @FXML private ImageView qrCodeImageView;
    @FXML private Button generateQRButton;
    @FXML private Button analyzeImageButton;
    @FXML private ProgressIndicator analyzeProgress;
    @FXML private Button ajouterPanierButton;
    @FXML private Button voirPanierButton;

    private final MaterielService service = new MaterielService();
    private MaterielVente currentMateriel;
    private int currentUserId;
    private SimpleImageAnalysisService imageAnalysisService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imageAnalysisService = new SimpleImageAnalysisService("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"); // Replace with your real key

        generateQRButton.setOnAction(e -> generateQRCode());
        analyzeImageButton.setOnAction(e -> analyzeImage());

        analyzeProgress.setVisible(false);

        if (SessionManager.getInstance().getCurrentUser() == null) {
            acheter.setDisable(true);
            acheter.setText("vous devez conncter pour louer");
        }
    }

    public void setMateriel(MaterielVente materiel) {
        this.currentMateriel = materiel;
        updateUI();
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    private void updateUI() {
        if (currentMateriel == null) return;

        // Load image with fallback
        try {
            String imagePath = "file:src/main/resources/images_materiels/" + currentMateriel.getImage();
            imageView.setImage(new Image(imagePath));
        } catch (Exception e) {
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.png")));
            } catch (Exception ex) {
                System.err.println("Could not load default image: " + ex.getMessage());
            }
        }

        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        nomLabel.setText(currentMateriel.getNom());
        prixLabel.setText(String.format("%.2f TND", currentMateriel.getPrix()));
        descriptionLabel.setText(currentMateriel.getDescription());

        boolean isAvailable = currentMateriel.isDisponibilite();
        disponibiliteLabel.setText(isAvailable ? "Disponible" : "Non disponible");
        disponibiliteLabel.setStyle("-fx-text-fill: " + (isAvailable ? "#4CAF50" : "#F44336"));

        categorieLabel.setText("Catégorie: " + (currentMateriel.getCategorieId() != null ?
                currentMateriel.getCategorieId().toString() : "N/A"));
        createdAtLabel.setText("Ajouté le: " + (currentMateriel.getCreatedAt() != null ?
                currentMateriel.getCreatedAt().toLocalDate().toString() : "N/A"));

        acheter.setDisable(!isAvailable);
    }

    @FXML
    public void Acheter(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUser() != null) {
            if (currentMateriel == null) {
                showAlert("Erreur", "Aucun matériel sélectionné", Alert.AlertType.ERROR);
                return;
            }

            if (!currentMateriel.isDisponibilite()) {
                showAlert("Erreur", "Ce matériel n'est plus disponible", Alert.AlertType.ERROR);
                return;
            }

            try {
                service.acheterMateriel(currentMateriel.getId(), SessionManager.getInstance().getCurrentUser().getId());

                // Update model
                currentMateriel.setDisponibilite(false);
                currentMateriel.setUserIdMaterielVenteId(SessionManager.getInstance().getCurrentUser().getId());

                // Update UI
                updateUI();
                showAlert("Succès", "Matériel acheté avec succès!", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de l'achat: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void generateQRCode() {
        if (currentMateriel == null) return;

        String qrText = "Matériel Agricole: " + currentMateriel.getNom() + "\n" +
                "Prix: " + currentMateriel.getPrix() + " TND\n" +
                "Description: " + currentMateriel.getDescription().substring(0, Math.min(100, currentMateriel.getDescription().length()));

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, 200, 200);

            Image qrImage = SwingFXUtils.toFXImage(MatrixToImageWriter.toBufferedImage(bitMatrix), null);
            qrCodeImageView.setImage(qrImage);

            Tooltip.install(qrCodeImageView, new Tooltip(qrText));
        } catch (WriterException e) {
            showAlert("Erreur", "Impossible de générer le QR Code", Alert.AlertType.ERROR);
        }
    }

    private void analyzeImage() {
        if (currentMateriel == null) return;

        File imageFile = new File("src/main/resources/images_materiels/" + currentMateriel.getImage());

        analyzeProgress.setVisible(true);

        new Thread(() -> {
            try {
                String jsonResponse = imageAnalysisService.classifyImage(imageFile);

                // Parse the JSON to get the top label
                String topLabel = parseLabelFromJson(jsonResponse);

                Platform.runLater(() -> {
                    showAlert("Image Classification", "Detected: " + topLabel, Alert.AlertType.INFORMATION);
                    analyzeProgress.setVisible(false);
                });

            } catch (IOException e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to analyze: " + e.getMessage(), Alert.AlertType.ERROR);
                    analyzeProgress.setVisible(false);
                });
            }
        }).start();
    }

    private String parseLabelFromJson(String json) {
        // Very simple JSON parsing - just get the first label
        int labelIndex = json.indexOf("\"label\":\"");
        if (labelIndex == -1) return "Unknown";

        int start = labelIndex + 9;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }



private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void ajouterAuPanier() {
        if (currentMateriel != null) {
            PanierSession.ajouterProduit(currentMateriel, 1);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ajout au panier");
            alert.setHeaderText(null);
            alert.setContentText("Produit ajouté au panier !");
            alert.showAndWait();
        } else {
            showAlert("Erreur", "Aucun matériel sélectionné", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void voirPanier() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FrontOffice/GestionCommande/PanierView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Mon Panier");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture du panier: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}