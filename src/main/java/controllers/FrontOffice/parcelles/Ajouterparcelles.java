package controllers.FrontOffice.parcelles;

import controllers.FrontOffice.BaseFrontController;
import controllers.FrontOffice.contrats.Ajoutercontrat;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Models.ParcelleProprietes;
import services.ParcelleProprietesService;
import netscape.javascript.JSObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;

public class Ajouterparcelles {

    @FXML private TextField titreField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> statutCombo;
    @FXML private ComboBox<String> typeTerrain;
    @FXML private TextField tailleField;
    @FXML private DatePicker dateCreation;
    @FXML private DatePicker dateMaj;
    @FXML private TextField emplacementField;
    @FXML private TextField nomProprio;
    @FXML private TextField contactProprio;
    @FXML private TextField emailField;
    @FXML private CheckBox disponibleCheckBox;
    @FXML private Label fileNameLabel;
    @FXML private Button afficherButton;
    @FXML private Button ajouterContratButton;

    // Map display elements
    @FXML private WebView mapWebView;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private Label latitudeLabel;
    @FXML private Label longitudeLabel;
    @FXML private Label locationInfoLabel;

    private File selectedFile;
    private WebEngine webEngine;
    private boolean mapLoaded = false;

    // Java-JavaScript communication bridge
    public class JavaBridge {
        public void updateLocation(double lat, double lng, String address) {
            // Called from JavaScript
            Platform.runLater(() -> {
                latitudeField.setText(String.valueOf(lat));
                longitudeField.setText(String.valueOf(lng));
                emplacementField.setText(address);

                // Update display labels
                latitudeLabel.setText(String.format("%.6f", lat));
                longitudeLabel.setText(String.format("%.6f", lng));
                locationInfoLabel.setText("Selected location: " + address);
            });
        }

        public void mapLoaded() {
            // Called when map finishes loading
            Platform.runLater(() -> {
                mapLoaded = true;
                System.out.println("Map loaded successfully");
            });
        }
    }

    @FXML
    private void initialize() {
        // Configure combo boxes
        statutCombo.getItems().addAll("Louer", "Vendre", "En location");
        typeTerrain.getItems().addAll("Agricole", "Résidentiel", "Commercial", "Mixte");

        // Set default dates
        dateCreation.setValue(LocalDate.now());
        dateMaj.setValue(LocalDate.now());

        // Restrict size field to numeric input
        tailleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                tailleField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        // Restrict price field to numeric/decimal input
        prixField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                prixField.setText(oldVal);
            }
        });

        // Restrict contact field to 8 digits
        contactProprio.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                contactProprio.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (newVal.length() > 8) {
                contactProprio.setText(newVal.substring(0, 8));
            }
        });

        // Real-time email validation
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
                emailField.setStyle("-fx-border-color: red;");
            } else {
                emailField.setStyle("");
            }
        });

        // Initialize Leaflet map
        initializeMap();
    }

    private void initializeMap() {
        webEngine = mapWebView.getEngine();

        // Load Leaflet map HTML
        try {
            URL mapUrl = getClass().getResource("/FrontOffice/parcelles/map.html");
            if (mapUrl != null) {
                webEngine.load(mapUrl.toExternalForm());
                System.out.println("Loading map from: " + mapUrl);
            } else {
                // Fallback HTML content
                System.out.println("Map file not found, loading embedded HTML");
                String htmlContent = getLeafletHtmlContent();
                webEngine.loadContent(htmlContent);
            }

            // Set up JavaScript bridge
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaConnector", new JavaBridge());
                    System.out.println("JavaScript-Java bridge initialized");
                }
            });
        } catch (Exception e) {
            showAlert("Erreur Carte", "Impossible de charger la carte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Generate Leaflet HTML content
    private String getLeafletHtmlContent() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Leaflet Map</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" />\n" +
                "    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n" +
                "    <style>\n" +
                "        html, body {\n" +
                "            height: 100%;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        #map {\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "        }\n" +
                "        .info-box {\n" +
                "            padding: 6px 8px;\n" +
                "            font: 14px/16px Arial, Helvetica, sans-serif;\n" +
                "            background: white;\n" +
                "            background: rgba(255,255,255,0.8);\n" +
                "            box-shadow: 0 0 15px rgba(0,0,0,0.2);\n" +
                "            border-radius: 5px;\n" +
                "        }\n" +
                "        .info-box h4 {\n" +
                "            margin: 0 0 5px;\n" +
                "            color: #777;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div id=\"map\"></div>\n" +
                "<script>\n" +
                "    // Initialize map\n" +
                "    const map = L.map('map').setView([36.8065, 10.1815], 7); // Default coordinates: Tunis\n" +
                "    \n" +
                "    // Add OpenStreetMap base layer\n" +
                "    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                "        attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors',\n" +
                "        maxZoom: 19\n" +
                "    }).addTo(map);\n" +
                "    \n" +
                "    let marker = null;\n" +
                "    let infoBox = null;\n" +
                "    \n" +
                "    // Create info display box\n" +
                "    function createInfoBox() {\n" +
                "        const infoDiv = L.DomUtil.create('div', 'info-box');\n" +
                "        infoDiv.style.display = 'none';\n" +
                "        infoBox = L.control({position: 'bottomleft'});\n" +
                "        \n" +
                "        infoBox.onAdd = function() {\n" +
                "            return infoDiv;\n" +
                "        };\n" +
                "        \n" +
                "        infoBox.addTo(map);\n" +
                "        return infoDiv;\n" +
                "    }\n" +
                "    \n" +
                "    const infoDiv = createInfoBox();\n" +
                "    \n" +
                "    // Update info box with location data\n" +
                "    function updateInfoBox(lat, lng, address) {\n" +
                "        infoDiv.innerHTML = `\n" +
                "            <h4>Location Information</h4>\n" +
                "            <b>Address:</b> ${address}<br>\n" +
                "            <b>Latitude:</b> ${lat.toFixed(6)}<br>\n" +
                "            <b>Longitude:</b> ${lng.toFixed(6)}\n" +
                "        `;\n" +
                "        infoDiv.style.display = 'block';\n" +
                "    }\n" +
                "    \n" +
                "    // Map click handler\n" +
                "    map.on('click', async function(e) {\n" +
                "        const lat = e.latlng.lat;\n" +
                "        const lng = e.latlng.lng;\n" +
                "        \n" +
                "        // Remove existing marker\n" +
                "        if (marker) {\n" +
                "            map.removeLayer(marker);\n" +
                "        }\n" +
                "        \n" +
                "        // Add new marker\n" +
                "        marker = L.marker([lat, lng]).addTo(map);\n" +
                "        \n" +
                "        // Show loading message\n" +
                "        updateInfoBox(lat, lng, \"Loading address...\");\n" +
                "        \n" +
                "        // Reverse geocoding using Nominatim\n" +
                "        try {\n" +
                "            const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`);\n" +
                "            \n" +
                "            if (response.ok) {\n" +
                "                const data = await response.json();\n" +
                "                let formattedAddress = \"\";\n" +
                "                \n" +
                "                if (data.address) {\n" +
                "                    const addr = data.address;\n" +
                "                    const components = [];\n" +
                "                    \n" +
                "                    if (addr.road) components.push(addr.road);\n" +
                "                    if (addr.house_number) components.push(addr.house_number);\n" +
                "                    if (addr.suburb) components.push(addr.suburb);\n" +
                "                    if (addr.city || addr.town || addr.village) {\n" +
                "                        components.push(addr.city || addr.town || addr.village);\n" +
                "                    }\n" +
                "                    if (addr.state || addr.county) {\n" +
                "                        components.push(addr.state || addr.county);\n" +
                "                    }\n" +
                "                    if (addr.country) components.push(addr.country);\n" +
                "                    \n" +
                "                    formattedAddress = components.join(', ');\n" +
                "                }\n" +
                "                \n" +
                "                const address = formattedAddress || data.display_name || `${lat.toFixed(4)}, ${lng.toFixed(4)}`;\n" +
                "                \n" +
                "                // Update info display\n" +
                "                updateInfoBox(lat, lng, address);\n" +
                "                \n" +
                "                // Communicate with Java\n" +
                "                if (window.javaConnector) {\n" +
                "                    window.javaConnector.updateLocation(lat, lng, address);\n" +
                "                }\n" +
                "            } else {\n" +
                "                throw new Error(`HTTP Error: ${response.status}`);\n" +
                "            }\n" +
                "        } catch (error) {\n" +
                "            console.error('Reverse geocoding error:', error);\n" +
                "            const fallbackAddress = `${lat.toFixed(4)}, ${lng.toFixed(4)}`;\n" +
                "            updateInfoBox(lat, lng, `Location: ${fallbackAddress}`);\n" +
                "            \n" +
                "            if (window.javaConnector) {\n" +
                "                window.javaConnector.updateLocation(lat, lng, fallbackAddress);\n" +
                "            }\n" +
                "        }\n" +
                "    });\n" +
                "    \n" +
                "    // Zoom to specific location\n" +
                "    window.zoomToLocation = function(lat, lng) {\n" +
                "        map.setView([lat, lng], 15);\n" +
                "        if (marker) {\n" +
                "            map.removeLayer(marker);\n" +
                "        }\n" +
                "        marker = L.marker([lat, lng]).addTo(map);\n" +
                "    };\n" +
                "    \n" +
                "    // Handle map container resize\n" +
                "    window.addEventListener('resize', function() {\n" +
                "        map.invalidateSize();\n" +
                "    });\n" +
                "    \n" +
                "    // Notify Java when map is loaded\n" +
                "    window.onload = function() {\n" +
                "        // Invalidate map size to ensure proper rendering\n" +
                "        setTimeout(function() {\n" +
                "            map.invalidateSize();\n" +
                "            if (window.javaConnector) {\n" +
                "                window.javaConnector.mapLoaded();\n" +
                "            }\n" +
                "        }, 1000); // Longer timeout to ensure map is fully rendered\n" +
                "    };\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }

    @FXML
    public void handleAjouterContrat() {
        try {
            // Load base layout
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            // Load contract form
            Parent content = FXMLLoader.load(getClass().getResource("/FrontOffice/contrats/Ajoutercontrat.fxml"));

            // Inject into content pane
            baseController.getContentPane().getChildren().setAll(content);

            // Update current window
            Stage stage = (Stage) ajouterContratButton.getScene().getWindow();
            stage.setScene(new Scene(baseRoot));

        } catch (IOException e) {
            showAlert("Erreur Navigation",
                    "Impossible d'ouvrir l'interface :\n"
                            + "1. Vérifiez que Ajoutercontrat.fxml existe\n"
                            + "2. Vérifiez le chemin: /FrontOffice/contrats/\n"
                            + "Erreur technique: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFileSelect() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
        } else {
            fileNameLabel.setText("Aucun fichier choisi");
        }
    }

    @FXML
    private void handleAfficherListe() {
        try {
            // Load base layout
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            // Load parcel list
            Parent content = FXMLLoader.load(getClass().getResource("/FrontOffice/parcelles/Afficherparcelles.fxml"));

            // Inject into content pane
            baseController.getContentPane().getChildren().setAll(content);

            // Update current window
            Stage stage = (Stage) afficherButton.getScene().getWindow();
            stage.setScene(new Scene(baseRoot));

        } catch (IOException e) {
            showAlert("Erreur Navigation",
                    "Impossible de charger l'interface :\n"
                            + "1. Vérifiez que baseFront.fxml existe\n"
                            + "2. Vérifiez les chemins FXML\n"
                            + "Erreur technique : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEnregistrer() {
        // Validate all fields first
        if (!validateFields()) {
            return;
        }

        // Get all form values
        String titre = titreField.getText();
        String prix = prixField.getText();
        String statut = statutCombo.getValue();
        String type = typeTerrain.getValue();
        String taille = tailleField.getText();
        LocalDate dateCreationVal = dateCreation.getValue();
        LocalDate dateMajVal = dateMaj.getValue();
        String emplacement = emplacementField.getText();
        String nom = nomProprio.getText();
        String contact = contactProprio.getText();
        String email = emailField.getText();
        boolean estDisponible = disponibleCheckBox.isSelected();

        // Improved image handling
        String imagePath = "/images/default.png"; // Default image path

        if (selectedFile != null) {
            try {
                // Generate unique filename
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();

                // Ensure directory exists
                File destDir = new File("src/main/resources/images");
                if (!destDir.exists()) destDir.mkdirs();

                // Path for the destination file
                File destFile = new File(destDir, fileName);

                // Copy the file
                try (FileInputStream fis = new FileInputStream(selectedFile);
                     FileOutputStream fos = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }

                // Store path with leading slash to ensure it works with resource loading
                imagePath = "/images/" + fileName;

            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors de la copie de l'image : " + e.getMessage());
                return;
            }
        }

        // Create and populate the parcelle object
        ParcelleProprietes parcelle = new ParcelleProprietes();
        parcelle.setTitre(titre);
        parcelle.setDescription("");
        parcelle.setPrix(Double.parseDouble(prix));
        parcelle.setStatus(statut);
        parcelle.setType_terrain(type);
        parcelle.setTaille(Double.parseDouble(taille));
        parcelle.setDate_creation_annonce(Timestamp.valueOf(dateCreationVal.atStartOfDay()));
        parcelle.setDate_misajour_annonce(Timestamp.valueOf(dateMajVal.atStartOfDay()));
        parcelle.setEmplacement(emplacement);
        parcelle.setNom_proprietaire(nom);
        parcelle.setContact_proprietaire(contact);
        parcelle.setEmail(email);
        parcelle.setEst_disponible(estDisponible);
        parcelle.setImage(imagePath);

        // Make sure latitude and longitude are set correctly
        if (!latitudeField.getText().isEmpty() && !longitudeField.getText().isEmpty()) {
            parcelle.setLatitude(latitudeField.getText());
            parcelle.setLongitude(longitudeField.getText());
        } else {
            parcelle.setLatitude("0.0");
            parcelle.setLongitude("0.0");
        }

        // Save the parcelle
        ParcelleProprietesService service = new ParcelleProprietesService();
        int parcelleId = service.addWithId(parcelle);

        if (parcelleId > 0) {
            showSuccess("Parcelle ajoutée avec succès !");

            // Demander si l'utilisateur veut créer un contrat associé
            boolean createContract = showConfirmationDialog(
                    "Création de contrat",
                    "Voulez-vous créer un contrat associé à cette parcelle ?");

            if (createContract) {
                // Ouvrir l'interface de création de contrat avec les informations de la parcelle
                openContractFormWithParcelInfo(parcelleId, titre);
            } else {
                // Auto-refresh the list if no contract is created
                clearFields();
                handleAfficherListe();
            }
        } else {
            showAlert("Erreur", "Erreur lors de l'ajout de la parcelle");
        }
    }




    /**
     * Affiche une boîte de dialogue de confirmation
     * @param title Titre de la boîte de dialogue
     * @param message Message à afficher
     * @return true si l'utilisateur a cliqué sur OK, false sinon
     */
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Personnaliser les boutons
        ButtonType buttonTypeYes = new ButtonType("Oui");
        ButtonType buttonTypeNo = new ButtonType("Non");

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        // Afficher et attendre la réponse
        return alert.showAndWait().orElse(buttonTypeNo) == buttonTypeYes;
    }

    /**
     * Ouvre le formulaire de contrat avec les informations de la parcelle préchargées
     * @param parcelleId ID de la parcelle
     * @param parcelleTitle Titre de la parcelle
     */
    private void openContractFormWithParcelInfo(int parcelleId, String parcelleTitle) {
        try {
            // Charger le layout de base
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            // Charger le formulaire de contrat
            FXMLLoader contractLoader = new FXMLLoader(getClass().getResource("/FrontOffice/contrats/Ajoutercontrat.fxml"));
            Parent contractContent = contractLoader.load();

            // Récupérer le contrôleur du formulaire de contrat
            Ajoutercontrat contractController = contractLoader.getController();

            // Définir les informations de la parcelle dans le formulaire de contrat
            contractController.initWithParcelleInfo(parcelleId, parcelleTitle);

            // Injecter dans le panneau de contenu
            baseController.getContentPane().getChildren().setAll(contractContent);

            // Mettre à jour la fenêtre actuelle
            Stage stage = (Stage) ajouterContratButton.getScene().getWindow();
            stage.setScene(new Scene(baseRoot));

        } catch (IOException e) {
            showAlert("Erreur Navigation",
                    "Impossible d'ouvrir l'interface de contrat :\n"
                            + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, retourner à la liste des parcelles
            clearFields();
            handleAfficherListe();
        }
    }

    /**
     * Validate all form fields
     * @return true if all valid, false otherwise
     */
    private boolean validateFields() {
        StringBuilder errorMsg = new StringBuilder();

        // Required fields check
        if (titreField.getText().isEmpty()) {
            errorMsg.append("- Le titre est obligatoire\n");
        }

        if (prixField.getText().isEmpty()) {
            errorMsg.append("- Le prix est obligatoire\n");
        } else {
            try {
                double prix = Double.parseDouble(prixField.getText());
                if (prix <= 0) {
                    errorMsg.append("- Le prix doit être supérieur à 0\n");
                }
            } catch (NumberFormatException e) {
                errorMsg.append("- Le prix doit être un nombre valide\n");
            }
        }

        if (tailleField.getText().isEmpty()) {
            errorMsg.append("- La taille est obligatoire\n");
        } else {
            try {
                double taille = Double.parseDouble(tailleField.getText());
                if (taille <= 0) {
                    errorMsg.append("- La taille doit être supérieure à 0\n");
                }
            } catch (NumberFormatException e) {
                errorMsg.append("- La taille doit être un nombre valide\n");
            }
        }

        if (statutCombo.getValue() == null) {
            errorMsg.append("- Le statut est obligatoire\n");
        }

        if (typeTerrain.getValue() == null) {
            errorMsg.append("- Le type de terrain est obligatoire\n");
        }

        if (dateCreation.getValue() == null) {
            errorMsg.append("- La date de création est obligatoire\n");
        }

        if (dateMaj.getValue() == null) {
            errorMsg.append("- La date de mise à jour est obligatoire\n");
        } else if (dateCreation.getValue() != null && dateMaj.getValue().isBefore(dateCreation.getValue())) {
            errorMsg.append("- La date de mise à jour ne peut pas être antérieure à la date de création\n");
        }

        if (emplacementField.getText().isEmpty()) {
            errorMsg.append("- L'emplacement est obligatoire, sélectionnez un point sur la carte\n");
        }

        if (nomProprio.getText().isEmpty()) {
            errorMsg.append("- Le nom du propriétaire est obligatoire\n");
        }

        if (contactProprio.getText().isEmpty()) {
            errorMsg.append("- Le contact du propriétaire est obligatoire\n");
        } else if (contactProprio.getText().length() < 8) {
            errorMsg.append("- Le contact du propriétaire doit contenir 8 chiffres\n");
        }

        if (emailField.getText().isEmpty()) {
            errorMsg.append("- L'email est obligatoire\n");
        } else if (!emailField.getText().matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            errorMsg.append("- Format d'email invalide\n");
        }

        if (selectedFile == null) {
            errorMsg.append("- Une image est obligatoire\n");
        }

        if (latitudeField.getText().isEmpty() || longitudeField.getText().isEmpty()) {
            errorMsg.append("- Veuillez sélectionner un emplacement sur la carte\n");
        }

        if (errorMsg.length() > 0) {
            showAlert("Validation", "Veuillez corriger les erreurs suivantes :\n" + errorMsg.toString());
            return false;
        }

        return true;
    }

    private void clearFields() {
        titreField.clear();
        prixField.clear();
        statutCombo.getSelectionModel().clearSelection();
        typeTerrain.getSelectionModel().clearSelection();
        tailleField.clear();
        dateCreation.setValue(LocalDate.now());
        dateMaj.setValue(LocalDate.now());
        emplacementField.clear();
        nomProprio.clear();
        contactProprio.clear();
        emailField.clear();
        disponibleCheckBox.setSelected(false);
        fileNameLabel.setText("Aucun fichier choisi");
        selectedFile = null;

        // Reset map fields
        latitudeField.clear();
        longitudeField.clear();
        latitudeLabel.setText("---");
        longitudeLabel.setText("---");
        locationInfoLabel.setText("Cliquez sur un point de la carte pour sélectionner une localisation");

        // Reset map view
        if (webEngine != null && mapLoaded) {
            try {
                webEngine.executeScript(
                        "if (map) { " +
                                "   map.setView([36.8065, 10.1815], 7); " +
                                "   if (marker) { " +
                                "       map.removeLayer(marker); " +
                                "       marker = null; " +
                                "   } " +
                                "}"
                );
            } catch (Exception e) {
                System.err.println("Erreur lors de la réinitialisation de la carte: " + e.getMessage());
            }
        }
    }

    /**
     * Center map on specific coordinates
     * @param lat Latitude
     * @param lng Longitude
     */
    public void centerMapOn(double lat, double lng) {
        if (webEngine != null && mapLoaded) {
            try {
                webEngine.executeScript("zoomToLocation(" + lat + ", " + lng + ")");
            } catch (Exception e) {
                System.err.println("Erreur lors du centrage de la carte: " + e.getMessage());
            }
        }
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}