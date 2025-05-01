package controllers.FrontOffice.GestionCommande;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import kong.unirest.json.JSONObject;
import Models.gestionCommande.Commande;
import Models.gestionCommande.Livraison;
import Models.gestionCommande.Paiement;
import services.gestionCommande.LivraisonService;
import services.gestionCommande.PaiementService;
import services.gestionCommande.PayPalService;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

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
import java.util.concurrent.atomic.AtomicBoolean;

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

    @FXML private Label numeroCarteError;
    @FXML private Label dateExpirationError;
    @FXML private Label cvvError;
    @FXML private Label nomTitulaireError;

    private ToggleGroup toggleGroup;
    private Commande commande;
    private static final String APP_TOKEN = "cb172228-ba54-4fce-83a5-c17ad365c9a6";
    private static final String APP_SECRET = "57e46146-f0ea-4130-961c-99885f30a209";

    // PayPal variables
    private PayPalService paypalService;
    private AtomicBoolean paypalPaiementEffectue = new AtomicBoolean(false);
    private String paypalPaymentId;
    private String paypalPayerId;

    public void setCommande(Commande commande) {
        this.commande = commande;
    }
    private File imageRecto;
    private File imageVerso;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation du service PayPal
        paypalService = new PayPalService();

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
                initPayPalPayment();
            } else if (newVal == virementRadio) {
                virementDetails.setVisible(true);
                virementDetails.setManaged(true);
                initFlouciPayment();
            }
        });

        // Validation en temps réel
        setupFieldValidators();
    }

    private void initPayPalPayment() {
        // Nettoyer le contenu précédent
        paypalDetails.getChildren().clear();

        // Ajouter un bouton pour procéder au paiement PayPal
        Button payerPaypalButton = new Button("Payer avec PayPal");
        Label paypalInfoLabel = new Label("Cliquez sur le bouton pour être redirigé vers PayPal");
        paypalInfoLabel.setStyle("-fx-text-fill: #3b7bbf;");

        paypalDetails.getChildren().addAll(paypalInfoLabel, payerPaypalButton);

        payerPaypalButton.setOnAction(event -> {
            try {
                lancerPaiementPayPal();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de paiement PayPal");
                alert.setContentText("Une erreur est survenue lors du traitement du paiement: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        });
    }

    private void lancerPaiementPayPal() {
        try {
            // Créer un serveur HTTP local pour gérer les redirections
            int port = 9998; // Port différent de Flouci

            // URLs de redirection pour PayPal
            String returnUrl = "http://localhost:" + port + "/success";
            String cancelUrl = "http://localhost:" + port + "/cancel";

            // Démarrer le serveur avant de créer le paiement
            startPayPalServer(port);

            // Créer le paiement PayPal
            String approvalUrl = paypalService.createPayment(commande, returnUrl, cancelUrl);

            // Ouvrir le navigateur avec l'URL d'approbation
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(approvalUrl));
            } else {
                System.out.println("Desktop non supporté, veuillez visiter: " + approvalUrl);
            }

        } catch (PayPalRESTException | IOException | URISyntaxException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur PayPal");
            alert.setContentText("Erreur lors de l'initialisation du paiement PayPal: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void startPayPalServer(int port) {
        new Thread(() -> {
            try {
                com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                        new java.net.InetSocketAddress(port), 0);

                // Gestionnaire pour les paiements réussis
                server.createContext("/success", httpExchange -> {
                    // Extraire les paramètres de l'URL
                    String query = httpExchange.getRequestURI().getQuery();
                    String paymentId = extractParameter(query, "paymentId");
                    String payerId = extractParameter(query, "PayerID");

                    if (paymentId != null && payerId != null) {
                        try {
                            // Exécuter le paiement
                            Payment executedPayment = paypalService.executePayment(paymentId, payerId);

                            // Enregistrer les identifiants pour l'utilisation ultérieure
                            paypalPaymentId = paymentId;
                            paypalPayerId = payerId;
                            paypalPaiementEffectue.set(true);

                            // Mettre à jour l'interface utilisateur
                            Platform.runLater(() -> {
                                labelConfirmation.setText("✅ Paiement PayPal effectué avec succès !");
                                labelConfirmation.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            });

                            // Réponse HTML avec redirection automatique
                            String responseSuccess = "<html><body>" +
                                    "<h2>Paiement effectué avec succès</h2>" +
                                    "<p>Vous pouvez fermer cette fenêtre et retourner à l'application.</p>" +
                                    "<script>setTimeout(() => window.close(), 3000);</script>" +
                                    "</body></html>";

                            httpExchange.getResponseHeaders().set("Content-Type", "text/html");
                            httpExchange.sendResponseHeaders(200, responseSuccess.length());
                            httpExchange.getResponseBody().write(responseSuccess.getBytes());

                        } catch (PayPalRESTException e) {
                            String errorMessage = "Erreur lors de l'exécution du paiement: " + e.getMessage();
                            httpExchange.sendResponseHeaders(500, errorMessage.length());
                            httpExchange.getResponseBody().write(errorMessage.getBytes());
                            e.printStackTrace();
                        }
                    } else {
                        String errorMessage = "Paramètres de retour incomplets";
                        httpExchange.sendResponseHeaders(400, errorMessage.length());
                        httpExchange.getResponseBody().write(errorMessage.getBytes());
                    }

                    httpExchange.close();
                });

                // Gestionnaire pour les paiements annulés
                server.createContext("/cancel", httpExchange -> {
                    String responseCanceled = "<html><body>" +
                            "<h2>Paiement annulé</h2>" +
                            "<p>Vous avez annulé le paiement. Vous pouvez fermer cette fenêtre et retourner à l'application.</p>" +
                            "<script>setTimeout(() => window.close(), 3000);</script>" +
                            "</body></html>";

                    httpExchange.getResponseHeaders().set("Content-Type", "text/html");
                    httpExchange.sendResponseHeaders(200, responseCanceled.length());
                    httpExchange.getResponseBody().write(responseCanceled.getBytes());
                    httpExchange.close();

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Paiement annulé");
                        alert.setContentText("Le paiement PayPal a été annulé.");
                        alert.showAndWait();
                    });
                });

                server.setExecutor(null);
                server.start();
                System.out.println("Serveur PayPal démarré sur le port " + port);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String extractParameter(String query, String paramName) {
        if (query == null) return null;

        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        return null;
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
    private boolean paiementEffectue = false;

    private void lancerPaiementFlouci() throws IOException {
        JSONObject jsonBody = null;
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();

            jsonBody = new JSONObject();
            jsonBody.put("app_token", APP_TOKEN);
            jsonBody.put("app_secret", APP_SECRET);
            jsonBody.put("accept_card", true);
            jsonBody.put("amount", montantFinal);
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
                            labelConfirmation.setText("✅ Paiement effectué avec succès !");
                            labelConfirmation.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            paiementEffectue = true;
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
        imageVerso = fileChooser.showOpenDialog(null);
        if (imageVerso != null) {
            versoLabel.setText(imageVerso.getName());
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
        imageRecto = fileChooser.showOpenDialog(null);
        if (imageRecto != null) {
            rectoLabel.setText(imageRecto.getName());
        }
    }

    private float montantFinal;

    public void setMontantFinal(float montantFinal) {
        this.montantFinal = montantFinal;
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


        // Date d'expiration - format MM/YY
        dateExpirationField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,2}/?(\\d{0,2})?")) {
                dateExpirationField.setText(oldVal);
            } else if (newVal.length() == 2 && oldVal.length() == 1 && !newVal.contains("/")) {
                dateExpirationField.setText(newVal + "/");
            }
        });

        // CVV - seulement 3 chiffres
        cvvField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,3}")) {
                cvvField.setText(oldVal);
            }
        });

        // Nom du titulaire - lettres et espaces uniquement
        nomTitulaireField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-ZÀ-ÿ\\s]*")) {
                nomTitulaireField.setText(oldVal);
            }
        });

    }
    private boolean validateCardFields() {
        boolean isValid = true;

        // Réinitialiser les messages d'erreur
        numeroCarteError.setText("");
        dateExpirationError.setText("");
        cvvError.setText("");
        nomTitulaireError.setText("");

        // Validation numéro de carte
        if (numeroCarteField.getText().trim().isEmpty()) {
            numeroCarteError.setText("Ce champ est obligatoire");
            isValid = false;
        } else if (numeroCarteField.getText().trim().length() < 16) {
            numeroCarteError.setText("Doit contenir 16 chiffres");
            isValid = false;
        }
        // Validation date d'expiration
        if (dateExpirationField.getText().trim().isEmpty()) {
            dateExpirationError.setText("Obligatoire");
            isValid = false;
        } else if (!dateExpirationField.getText().matches("\\d{2}/\\d{2}")) {
            dateExpirationError.setText("Format MM/YY");
            isValid = false;
        } else {
            try {
                String[] parts = dateExpirationField.getText().split("/");
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);
                if (month < 1 || month > 12) {
                    dateExpirationError.setText("Mois invalide");
                    isValid = false;
                }


            } catch (Exception e) {
                dateExpirationError.setText("Format invalide");
                isValid = false;
            }
        }

        // Validation CVV
        if (cvvField.getText().trim().isEmpty()) {
            cvvError.setText("Obligatoire");
            isValid = false;
        } else if (cvvField.getText().trim().length() != 3) {
            cvvError.setText("3 chiffres");
            isValid = false;
        }

        // Validation nom du titulaire
        if (nomTitulaireField.getText().trim().isEmpty()) {
            nomTitulaireError.setText("Ce champ est obligatoire");
            isValid = false;
        } else if (nomTitulaireField.getText().trim().length() < 3) {
            nomTitulaireError.setText("Nom trop court");
            isValid = false;
        }

        return isValid;
    }
    private boolean validateFields() {
        boolean isValid = true;

        // Réinitialiser les messages d'erreur
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
    private Label messageErreur;

    // Méthode qui vérifie si une date est un jour férié
    private boolean isJourFerie(Date date) {
        Set<Date> joursFeries = new HashSet<>();

        // Exemple de jours fériés (à personnaliser selon les jours fériés de ton pays)
        // Ajouter manuellement les jours fériés pour l'année 2025 (par exemple)
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JANUARY, 1);  // 1er janvier
        joursFeries.add(calendar.getTime());

        calendar.set(2025, Calendar.MAY, 1);  // 1er mai
        joursFeries.add(calendar.getTime());

        calendar.set(2025, Calendar.JULY, 14);  // 14 juillet
        joursFeries.add(calendar.getTime());

        // Ajouter d'autres jours fériés ici...

        // Vérifier si la date est dans la liste des jours fériés
        for (Date jourFerie : joursFeries) {
            if (isSameDay(date, jourFerie)) {
                return true;
            }
        }
        return false;
    }

    // Méthode qui vérifie si deux dates sont le même jour
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    @FXML
    private void validerPaiement() {
        if (!validateFields()) return;

        RadioButton selectedRadio = (RadioButton) toggleGroup.getSelectedToggle();
        if (selectedRadio == null) {
            new Alert(Alert.AlertType.ERROR, "Choisissez une méthode de paiement !").show();
            return;
        }

        // Valider les champs selon la méthode de paiement sélectionnée
        if (selectedRadio == carteRadio) {
            if (!validateCardFields()) return;
        } else if (selectedRadio == virementRadio && !paiementEffectue) {
            new Alert(Alert.AlertType.WARNING, "Veuillez effectuer le paiement via Flouci avant de continuer.").show();
            return;
        } else if (selectedRadio == paypalRadio && !paypalPaiementEffectue.get()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez effectuer le paiement via PayPal avant de continuer.").show();
            return;
        }

        // Création du paiement
        Paiement paiement = new Paiement();
        paiement.setCommande(commande);
        paiement.setDatePaiement(new Date());

        // Définir la méthode de paiement avec plus de détails si nécessaire
        if (selectedRadio == paypalRadio && paypalPaymentId != null) {
            paiement.setMethodePaiement("PayPal (ID: " + paypalPaymentId + ")");
        } else {
            paiement.setMethodePaiement(selectedRadio.getText());
        }

        PaiementService paiementService = new PaiementService();
        paiementService.ajouter(paiement);

        // Construction adresse complète
        String adresse = numeroRueField.getText() + ", " + villeField.getText() + ", " +
                codePostalField.getText() + ", " + paysField.getText();

        // Création de la livraison
        Livraison livraison = new Livraison();
        livraison.setCommande(commande);
        livraison.setAdresse(adresse);

        // Calculer la date de livraison (3 jours ouvrables)
        Calendar calendar = Calendar.getInstance();
        int joursOuvrables = 0;
        int joursAAjouter = 0;

        // Boucle jusqu'à atteindre 3 jours ouvrables
        while (joursOuvrables < 3) {
            joursAAjouter++;
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            // Vérifier si c'est un jour ouvrable (pas weekend et pas férié)
            int jourDeSemaine = calendar.get(Calendar.DAY_OF_WEEK);
            boolean estWeekend = (jourDeSemaine == Calendar.SATURDAY || jourDeSemaine == Calendar.SUNDAY);
            boolean estFerie = isJourFerie(calendar.getTime());

            // Si ce n'est pas un weekend ni un jour férié, on incrémente le compteur
            if (!estWeekend && !estFerie) {
                joursOuvrables++;
            }

            // Pour debug
            System.out.println("Jour " + joursAAjouter + ": " + calendar.getTime() +
                    " (ouvrable: " + (!estWeekend && !estFerie) +
                    ", compteur: " + joursOuvrables + ")");
        }

        System.out.println("Date finale de livraison: " + calendar.getTime());
        livraison.setDateLiv(calendar.getTime());
        livraison.setStatus("Validation en cours");

        LivraisonService livraisonService = new LivraisonService();
        livraisonService.ajouter(livraison);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/GestionCommande/SuiviLivraison.fxml"));
            Parent root = loader.load();

            SuiviLivraisonController controller = loader.getController();
            controller.setCommandeId(commande.getId());

            // Créer une nouvelle scène avec le root chargé
            Scene scene = new Scene(root);

            // Appliquer le CSS à la scène
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            // Récupérer le stage actuel
            Stage stage = (Stage) numeroRueField.getScene().getWindow();
            stage.setScene(scene);  // Remplacer la scène de l'application
        } catch (IOException e) {
            e.printStackTrace();
        }
    }}