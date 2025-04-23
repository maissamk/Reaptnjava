package controllers.FrontOffice.GestionCommande;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import kong.unirest.json.JSONObject;
import Models.gestionCommande.Commande;
import Models.gestionCommande.Livraison;
import Models.gestionCommande.Paiement;
import services.gestionCommande.LivraisonService;
import services.gestionCommande.PaiementService;
import kong.unirest.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.UUID;


public class PaiementController implements Initializable {


    // Champs d'adresse
    @FXML private TextField numeroRueField;
    @FXML private TextField villeField;
    @FXML private TextField paysField;
    @FXML private TextField codePostalField;
    @FXML private TextField telephoneField;
    @FXML private TextArea notesCommandeArea;
    @FXML
    private Label labelConfirmation;
    // Labels d'erreur
    @FXML private Label numeroRueError;
    @FXML private Label villeError;
    @FXML private Label paysError;
    @FXML private Label codePostalError;
    @FXML private Label telephoneError;

    // Méthodes de paiement
    @FXML private RadioButton carteRadio;
    @FXML private RadioButton paypalRadio;
    @FXML private RadioButton virementRadio;
    @FXML private VBox carteDetails;
    @FXML private VBox paypalDetails;
    @FXML private VBox virementDetails;
    @FXML private TextField numeroCarteField;
    @FXML private TextField dateExpirationField;
    @FXML private TextField cvvField;
    @FXML private TextField nomTitulaireField;
    @FXML private Label rectoLabel;
    @FXML private Label versoLabel;
    @FXML private Button extraireDetailsButton;

    private ToggleGroup toggleGroup;
    private Commande commande;
    private static final String APP_TOKEN = "cb172228-ba54-4fce-83a5-c17ad365c9a6";
    private static final String APP_SECRET = "57e46146-f0ea-4130-961c-99885f30a209";

    public void setCommande(Commande commande) {
        this.commande = commande;
    }
    private File imageRecto;
    private File imageVerso;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des méthodes de paiement
        toggleGroup = new ToggleGroup();
        carteRadio.setToggleGroup(toggleGroup);
        paypalRadio.setToggleGroup(toggleGroup);
        virementRadio.setToggleGroup(toggleGroup);

        toggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            carteDetails.setVisible(false);
            carteDetails.setManaged(false);
            paypalDetails.setVisible(false);
            paypalDetails.setManaged(false);
            virementDetails.setVisible(false);
            virementDetails.setManaged(false);

            if (newVal == carteRadio) {
                carteDetails.setVisible(true);
                carteDetails.setManaged(true);
            } else if (newVal == paypalRadio) {
                paypalDetails.setVisible(true);
                paypalDetails.setManaged(true);
            } else if (newVal == virementRadio) {
                virementDetails.setVisible(true);
                virementDetails.setManaged(true);
                initFlouciPayment();
            }
        });

        // Validation en temps réel
        setupFieldValidators();
    }

    private void initFlouciPayment() {
        Button payerButton = new Button("Procéder au paiement");
        virementDetails.getChildren().add(payerButton);

        payerButton.setOnAction(event -> {
            try {
                lancerPaiementFlouci();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de paiement");
                alert.setContentText("Une erreur est survenue lors du traitement du paiement.");
                alert.showAndWait();
            }
        });
    }
    private void lancerPaiementFlouci() throws IOException {
        JSONObject jsonBody = null;
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();

            jsonBody = new JSONObject();
            jsonBody.put("app_token", APP_TOKEN);
            jsonBody.put("app_secret", APP_SECRET);
            jsonBody.put("accept_card", true);
            jsonBody.put("amount", 100); // Montant à ajuster selon la commande
            jsonBody.put("success_link", "http://localhost:9999/success"); // Lien local
            jsonBody.put("fail_link", "https://example.website.com/fail");
            jsonBody.put("session_timeout_secs", 1200);
            jsonBody.put("developer_tracking_id", UUID.randomUUID().toString());

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonBody.toString());

            Request request = new Request.Builder()
                    .url("https://developers.flouci.com/api/generate_payment")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                System.err.println("Erreur " + response.code() + ": " + responseBody);
                throw new IOException("Erreur " + response.code() + ": " + responseBody);
            }

            String responseBody = response.body().string();
            System.out.println("Réponse brute : " + responseBody); // Pour debug

            JSONObject jsonResponse = new JSONObject(responseBody);
            String paymentUrl = jsonResponse.getJSONObject("result").getString("link");

            System.out.println("URL du paiement : " + paymentUrl);

            // Lancer le serveur local pour écouter le retour de Flouci
            new Thread(() -> {
                try {
                    com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(9999), 0);
                    server.createContext("/success", httpExchange -> {
                        String responseSuccess = "Paiement effectué avec succès.";
                        httpExchange.sendResponseHeaders(200, responseSuccess.length());
                        httpExchange.getResponseBody().write(responseSuccess.getBytes());
                        httpExchange.close();

                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Paiement Confirmé");
                            alert.setHeaderText(null);
                            alert.setContentText("Le paiement a été effectué avec succès !");
                            alert.showAndWait();
                        });

                        server.stop(1); // Arrête le serveur après le traitement
                    });
                    server.setExecutor(null); // Utilise un exécuteur par défaut
                    server.start();
                    System.out.println("Serveur local démarré sur le port 9999");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Ouvrir le lien dans le navigateur
            Platform.runLater(() -> {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(paymentUrl));
                    } else {
                        System.out.println("Desktop non supporté.");
                    }
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de navigation");
                    alert.setContentText("Impossible d'ouvrir le lien de paiement: " + e.getMessage());
                    alert.showAndWait();
                }
            });

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de paiement");
            alert.setContentText("Une erreur est survenue lors du traitement du paiement: " + e.getMessage());
            alert.showAndWait();
            System.err.println("Erreur détaillée: " + e.getMessage());
        }

        System.out.println("Corps de la requête: " + jsonBody.toString(4));
    }


    @FXML
    private void choisirImageVerso() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            versoLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    private void extraireDetailsCarte() {
        if (imageRecto == null || imageVerso == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner les images du recto et du verso.").show();
            return;
        }

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/api/extract-card-details")
                .field("front_image", imageRecto)
                .field("back_image", imageVerso)
                .asJson();

        if (response.getStatus() == 200) {
            JSONObject data = response.getBody().getObject().getJSONObject("data");
            numeroCarteField.setText(data.getString("Numéro de carte"));
            dateExpirationField.setText(data.getString("Date d'expiration"));
            cvvField.setText(data.getString("CVV"));
            nomTitulaireField.setText(data.getString("Titulaire"));
        } else {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'extraction : " + response.getBody().toString()).show();
        }
    }

    @FXML
    private void choisirImageRecto() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            rectoLabel.setText(selectedFile.getName());
        }
    }

    private void setupFieldValidators() {
        // Ville - seulement des lettres
        villeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-ZÀ-ÿ\\s]*")) {
                villeField.setText(oldVal);
            }
        });

        // Pays - seulement des lettres
        paysField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-ZÀ-ÿ\\s]*")) {
                paysField.setText(oldVal);
            }
        });

        // Code postal - 5 chiffres max
        codePostalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,5}")) {
                codePostalField.setText(oldVal);
            }
        });

        // Téléphone - 10 chiffres max
        telephoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,10}")) {
                telephoneField.setText(oldVal);
            }
        });
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Réinitialiser les messages d'erreur
        numeroRueError.setText("");
        villeError.setText("");
        paysError.setText("");
        codePostalError.setText("");
        telephoneError.setText("");

        // Validation numéro et rue
        if (numeroRueField.getText().trim().isEmpty()) {
            numeroRueError.setText("Ce champ est obligatoire");
            isValid = false;
        }

        // Validation ville
        if (villeField.getText().trim().isEmpty()) {
            villeError.setText("Ce champ est obligatoire");
            isValid = false;
        } else if (villeField.getText().trim().length() < 2) {
            villeError.setText("Minimum 2 caractères");
            isValid = false;
        }

        // Validation pays
        if (paysField.getText().trim().isEmpty()) {
            paysError.setText("Ce champ est obligatoire");
            isValid = false;
        } else if (paysField.getText().trim().length() < 2) {
            paysError.setText("Minimum 2 caractères");
            isValid = false;
        }
// Quand le paiement est réussi, afficher le message de confirmation
        Platform.runLater(() -> {
            labelConfirmation.setText("✅ Paiement effectué avec succès !");
        });
        // Validation code postal
        if (codePostalField.getText().trim().isEmpty()) {
            codePostalError.setText("Ce champ est obligatoire");
            isValid = false;
        } else if (codePostalField.getText().trim().length() != 4) {
            codePostalError.setText("Doit contenir 4 chiffres");
            isValid = false;
        }

        // Validation téléphone
        if (telephoneField.getText().trim().isEmpty()) {
            telephoneError.setText("Ce champ est obligatoire");
            isValid = false;
        } else if (telephoneField.getText().trim().length() != 8) {
            telephoneError.setText("Doit contenir 8 chiffres");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void validerPaiement() {
        if (!validateFields()) return;

        RadioButton selectedRadio = (RadioButton) toggleGroup.getSelectedToggle();
        if (selectedRadio == null) {
            new Alert(Alert.AlertType.ERROR, "Choisissez une méthode de paiement !").show();
            return;
        }

        Paiement paiement = new Paiement();
        paiement.setCommande(commande);
        paiement.setDatePaiement(new Date());
        paiement.setMethodePaiement(selectedRadio.getText());

        PaiementService paiementService = new PaiementService();
        paiementService.ajouter(paiement);

        // Construction adresse complète
        String adresse = numeroRueField.getText() + ", " + villeField.getText() + ", " +
                codePostalField.getText() + ", " + paysField.getText();

        // Création de la livraison
        Livraison livraison = new Livraison();
        livraison.setCommande(commande);
        livraison.setAdresse(adresse);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        livraison.setDateLiv(calendar.getTime());
        livraison.setStatus("Validation en cours");

        LivraisonService livraisonService = new LivraisonService();
        livraisonService.ajouter(livraison);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/GestionCommande/SuiviLivraison.fxml"));
            Parent root = loader.load();

            SuiviLivraisonController controller = loader.getController();
            controller.setCommandeId(commande.getId());

            // Affiche la nouvelle scène
            numeroRueField.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}