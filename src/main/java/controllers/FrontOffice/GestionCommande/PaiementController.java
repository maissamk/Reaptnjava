package controllers.FrontOffice.GestionCommande;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import kong.unirest.json.JSONObject;
import Models.gestionCommande.Commande;
import Models.gestionCommande.Livraison;
import Models.gestionCommande.Paiement;
import services.gestionCommande.LivraisonService;
import services.gestionCommande.PaiementService;
import kong.unirest.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

public class PaiementController implements Initializable {

    // Champs d'adresse
    @FXML private TextField numeroRueField;
    @FXML private TextField villeField;
    @FXML private TextField paysField;
    @FXML private TextField codePostalField;
    @FXML private TextField telephoneField;
    @FXML private TextArea notesCommandeArea;

    // Labels d'erreur
    @FXML private Label numeroRueError;
    @FXML private Label villeError;
    @FXML private Label paysError;
    @FXML private Label codePostalError;
    @FXML private Label telephoneError;

    // Méthodes de paiement (existant)
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

    public void setCommande(Commande commande) {
        this.commande = commande;
    }
    private File imageRecto;
    private File imageVerso;

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





    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des méthodes de paiement (existant)
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
            }
        });

        // Validation en temps réel
        setupFieldValidators();
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
        String adresse = numeroRueField.getText() + ", " + villeField.getText() + ", " + codePostalField.getText() + ", " + paysField.getText();

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