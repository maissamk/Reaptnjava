package controllers.FrontOffice.contrats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import netscape.javascript.JSObject;
import org.json.JSONObject;

import Models.Contrat;
import services.ContratService;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

public class SignatureContratController implements Initializable {

    @FXML
    private Button btnChoisirPDF;

    @FXML
    private Button btnSigner;

    @FXML
    private Label lblStatus;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private VBox containerSign;

    @FXML
    private WebView webViewSignature;

    private WebEngine webEngine;
    private File selectedPDFFile;
    private String fileId;
    private String buyerName;
    private String signatureBase64;
    private final String API_BASE_URL = "http://localhost:5000/api";

    // Flag pour indiquer si on utilise l'API ou la méthode locale pour signature
    private final boolean USE_LOCAL_SIGNING = true; // Changer à false pour utiliser l'API

    // Ajout des variables pour le contrat
    private Contrat contrat;
    private ContratService contratService = new ContratService();
    private boolean useExistingPDF = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = webViewSignature.getEngine();

        // Initialiser les composants UI
        progressIndicator.setVisible(false);
        btnSigner.setDisable(true);
        containerSign.setVisible(false);

        // Configurer le WebView pour la signature
        setupJavaScriptBridge();

        // Injecter le code HTML et JavaScript nécessaire si le serveur est hors ligne
        if (USE_LOCAL_SIGNING) {
            loadLocalSignaturePad();
        }
    }

    // Chargement d'un pad de signature local pour éviter la dépendance au serveur
    private void loadLocalSignaturePad() {
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Signature Pad</title>\n" +
                "    <style>\n" +
                "        body { margin: 0; padding: 20px; font-family: Arial, sans-serif; }\n" +
                "        #signature-pad { border: 1px solid #ccc; border-radius: 4px; }\n" +
                "        .button-container { margin-top: 10px; }\n" +
                "        button { padding: 8px 15px; margin-right: 10px; cursor: pointer; }\n" +
                "        .save { background-color: #27ae60; color: white; border: none; border-radius: 4px; }\n" +
                "        .clear { background-color: #e74c3c; color: white; border: none; border-radius: 4px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h3>Signez ci-dessous:</h3>\n" +
                "    <canvas id=\"signature-pad\" width=\"600\" height=\"200\"></canvas>\n" +
                "    <div class=\"button-container\">\n" +
                "        <button class=\"save\" id=\"save-signature\">Appliquer la signature</button>\n" +
                "        <button class=\"clear\" id=\"clear-signature\">Effacer</button>\n" +
                "    </div>\n" +
                "\n" +
                "    <script>\n" +
                "        document.addEventListener('DOMContentLoaded', function() {\n" +
                "            const canvas = document.getElementById('signature-pad');\n" +
                "            const ctx = canvas.getContext('2d');\n" +
                "            let isDrawing = false;\n" +
                "            \n" +
                "            // Style de la signature\n" +
                "            ctx.lineWidth = 2;\n" +
                "            ctx.lineJoin = 'round';\n" +
                "            ctx.lineCap = 'round';\n" +
                "            ctx.strokeStyle = '#000';\n" +
                "            \n" +
                "            // Fond blanc\n" +
                "            ctx.fillStyle = '#fff';\n" +
                "            ctx.fillRect(0, 0, canvas.width, canvas.height);\n" +
                "            \n" +
                "            // Événements de dessin\n" +
                "            canvas.addEventListener('mousedown', startDrawing);\n" +
                "            canvas.addEventListener('mousemove', draw);\n" +
                "            canvas.addEventListener('mouseup', stopDrawing);\n" +
                "            canvas.addEventListener('mouseout', stopDrawing);\n" +
                "            \n" +
                "            // Support tactile\n" +
                "            canvas.addEventListener('touchstart', function(e) {\n" +
                "                e.preventDefault();\n" +
                "                startDrawing(e.touches[0]);\n" +
                "            });\n" +
                "            canvas.addEventListener('touchmove', function(e) {\n" +
                "                e.preventDefault();\n" +
                "                draw(e.touches[0]);\n" +
                "            });\n" +
                "            canvas.addEventListener('touchend', stopDrawing);\n" +
                "            \n" +
                "            function startDrawing(e) {\n" +
                "                isDrawing = true;\n" +
                "                ctx.beginPath();\n" +
                "                ctx.moveTo(getX(e), getY(e));\n" +
                "            }\n" +
                "            \n" +
                "            function draw(e) {\n" +
                "                if (!isDrawing) return;\n" +
                "                ctx.lineTo(getX(e), getY(e));\n" +
                "                ctx.stroke();\n" +
                "            }\n" +
                "            \n" +
                "            function stopDrawing() {\n" +
                "                isDrawing = false;\n" +
                "            }\n" +
                "            \n" +
                "            function getX(e) {\n" +
                "                return e.clientX - canvas.getBoundingClientRect().left;\n" +
                "            }\n" +
                "            \n" +
                "            function getY(e) {\n" +
                "                return e.clientY - canvas.getBoundingClientRect().top;\n" +
                "            }\n" +
                "            \n" +
                "            // Bouton pour effacer\n" +
                "            document.getElementById('clear-signature').addEventListener('click', function() {\n" +
                "                ctx.fillStyle = '#fff';\n" +
                "                ctx.fillRect(0, 0, canvas.width, canvas.height);\n" +
                "            });\n" +
                "            \n" +
                "            // Bouton pour sauvegarder\n" +
                "            document.getElementById('save-signature').addEventListener('click', function() {\n" +
                "                const signatureData = canvas.toDataURL('image/png');\n" +
                "                // Envoyer la signature à Java\n" +
                "                if (window.javaConnector) {\n" +
                "                    window.javaConnector.receiveSignature(signatureData);\n" +
                "                } else {\n" +
                "                    alert('Connexion avec Java non disponible');\n" +
                "                }\n" +
                "            });\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        webEngine.loadContent(html);
    }

    /**
     * Méthode pour initialiser le contrôleur avec un contrat existant
     */
    public void initWithContrat(Contrat contrat) {
        this.contrat = contrat;
        this.buyerName = contrat.getNom_acheteur();

        // Mise à jour de l'UI
        lblStatus.setText("Contrat N°" + contrat.getId() + " - " + contrat.getNom_acheteur() + " / " + contrat.getNom_vendeur());

        // Génération automatique du PDF du contrat
        generateContractPDF();
    }

    /**
     * Génère le PDF du contrat pour la signature
     */
    private void generateContractPDF() {
        progressIndicator.setVisible(true);
        lblStatus.setText("Génération du PDF du contrat...");

        CompletableFuture.supplyAsync(() -> {
            try {
                // Utiliser la logique existante pour générer le PDF
                String fileName = "Contrat_" + contrat.getId() + ".pdf";

                // Créer un fichier PDF temporaire pour le contrat avec un design amélioré
                Document document = new Document(PageSize.A4);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
                document.open();

                // Définir les polices et les couleurs
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(44, 62, 80));
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(52, 152, 219));
                com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL);
                com.itextpdf.text.Font smallFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.ITALIC);

                // Ajouter un en-tête avec une ligne colorée
                PdfContentByte cb = writer.getDirectContent();
                cb.setColorStroke(new com.itextpdf.text.BaseColor(52, 152, 219));
                cb.setLineWidth(3f);
                cb.moveTo(36, 806);
                cb.lineTo(559, 806);
                cb.stroke();

                // Titre du document
                Paragraph title = new Paragraph("CONTRAT DE VENTE", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Ajouter la référence du contrat
                Paragraph reference = new Paragraph("Référence: N°" + contrat.getId(), headerFont);
                reference.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                reference.setSpacingAfter(30);
                document.add(reference);

                // Informations sur les parties
                document.add(new Paragraph("ENTRE LES SOUSSIGNÉS:", headerFont));
                document.add(new Paragraph(" "));

                // Information vendeur
                Paragraph vendeur = new Paragraph("Vendeur: " + contrat.getNom_vendeur(), normalFont);
                vendeur.setIndentationLeft(20);
                document.add(vendeur);
                document.add(new Paragraph(" "));

                // Information acheteur
                Paragraph acheteur = new Paragraph("Acheteur: " + contrat.getNom_acheteur(), normalFont);
                acheteur.setIndentationLeft(20);
                document.add(acheteur);
                document.add(new Paragraph("\n"));

                // Détails du contrat
                document.add(new Paragraph("DÉTAILS DU CONTRAT:", headerFont));
                document.add(new Paragraph(" "));

                Paragraph info = new Paragraph(contrat.getInformation_contrat(), normalFont);
                info.setIndentationLeft(20);
                info.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
                document.add(info);

                // Ajouter une séparation avant la zone de signature
                document.add(new Paragraph("\n"));
                cb.setColorStroke(new com.itextpdf.text.BaseColor(189, 195, 199));
                cb.setLineWidth(1f);
                cb.moveTo(100, 300);
                cb.lineTo(495, 300);
                cb.stroke();

                // Zone de signature
                document.add(new Paragraph("\n"));
                Paragraph signatureTitle = new Paragraph("SIGNATURE DE L'ACHETEUR:", headerFont);
                signatureTitle.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                document.add(signatureTitle);
                document.add(new Paragraph("\n\n\n\n\n")); // Espace pour la signature

                // Date et lieu
                document.add(new Paragraph("Fait à _________________, le " +
                        new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()), normalFont));

                // Pied de page
                Paragraph footer = new Paragraph("Ce document constitue un contrat légal entre les parties mentionnées ci-dessus.", smallFont);
                footer.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(footer);

                document.close();

                selectedPDFFile = new File(fileName);
                useExistingPDF = true;
                return fileName;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(fileName -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);

                if (fileName != null) {
                    lblStatus.setText("PDF généré avec succès: " + fileName);

                    if (USE_LOCAL_SIGNING) {
                        // Activer directement le bouton signer
                        btnSigner.setDisable(false);
                    } else {
                        // Upload vers le serveur
                        uploadPDFToServer(selectedPDFFile);
                    }
                } else {
                    lblStatus.setText("Erreur lors de la génération du PDF");
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de générer le PDF du contrat");
                }
            });
        });
    }

    private void setupJavaScriptBridge() {
        webEngine.getLoadWorker().stateProperty().addListener(
                (ChangeListener<Worker.State>) (observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        JSObject window = (JSObject) webEngine.executeScript("window");
                        window.setMember("javaConnector", new JavaScriptConnector());
                    }
                }
        );
    }

    // Classe pour communiquer avec JavaScript
    public class JavaScriptConnector {
        public void receiveSignature(String signatureData) {
            handleSignatureReceived(signatureData);
        }

        public void cancel() {
            handleSignatureCancel();
        }
    }

    private void handleSignatureReceived(String signatureData) {
        this.signatureBase64 = signatureData;
        // Fermer la fenêtre de signature WebView
        containerSign.setVisible(false);

        // Procéder à l'application de la signature
        if (USE_LOCAL_SIGNING) {
            processSignedPDFLocally();
        } else {
            processSignedPDF();
        }
    }

    private void handleSignatureCancel() {
        containerSign.setVisible(false);
        showAlert(Alert.AlertType.INFORMATION, "Signature annulée", "L'opération de signature a été annulée.");
    }

    @FXML
    private void handleChoisirPDF(ActionEvent event) {
        if (useExistingPDF && selectedPDFFile != null) {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Un PDF a déjà été généré pour ce contrat.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un document PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documents PDF", "*.pdf")
        );

        selectedPDFFile = fileChooser.showOpenDialog(btnChoisirPDF.getScene().getWindow());
        if (selectedPDFFile != null) {
            lblStatus.setText("Fichier sélectionné: " + selectedPDFFile.getName());

            if (USE_LOCAL_SIGNING) {
                btnSigner.setDisable(false);
            } else {
                uploadPDFToServer(selectedPDFFile);
            }
        }
    }

    @FXML
    private void handleSigner(ActionEvent event) {
        if (contrat != null) {
            // Utiliser le nom de l'acheteur du contrat
            buyerName = contrat.getNom_acheteur();
        } else {
            // Demander le nom de l'acheteur (vous pouvez utiliser un dialogue ou un champ de texte)
            askBuyerName();
        }

        // Afficher l'interface de signature
        showSignaturePad();
    }

    private void askBuyerName() {
        // Dans une application réelle, utilisez un dialogue pour demander le nom
        // Pour simplifier, on utilise une valeur fixe ici
        buyerName = "John Doe";
    }

    private void showSignaturePad() {
        containerSign.setVisible(true);

        // Si on utilise l'API, charger la page de signature depuis le serveur
        if (!USE_LOCAL_SIGNING) {
            webEngine.load(API_BASE_URL + "/signature-pad");
        }
        // Sinon, on utilise déjà notre signature pad local
    }

    /**
     * Nouvelle méthode pour signer le PDF localement sans dépendre de l'API
     */
    private void processSignedPDFLocally() {
        if (selectedPDFFile == null || signatureBase64 == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Informations manquantes pour signer le document");
            return;
        }

        progressIndicator.setVisible(true);
        lblStatus.setText("Application de la signature...");

        CompletableFuture.supplyAsync(() -> {
            try {
                // Créer un fichier temporaire pour la signature
                String signatureFileName = "signature_temp.png";
                String signedPDFFileName = "Contrat_" + (contrat != null ? contrat.getId() + "_" : "") + "signé.pdf";

                // Convertir base64 en image
                String base64Image = signatureBase64.split(",")[1]; // Supprimer le préfixe "data:image/png;base64,"
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // Sauvegarder l'image de signature
                try (FileOutputStream fos = new FileOutputStream(signatureFileName)) {
                    fos.write(imageBytes);
                }

                // Ouvrir le PDF et ajouter la signature
                PdfReader reader = new PdfReader(selectedPDFFile.getAbsolutePath());
                PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(signedPDFFileName));

                // Charger l'image de signature
                Image signatureImage = Image.getInstance(signatureFileName);

                // Redimensionner l'image pour une meilleure apparence
                signatureImage.scaleAbsolute(180, 60);

                // Position améliorée de la signature
                PdfContentByte content = stamper.getOverContent(1);
                float pageHeight = reader.getPageSize(1).getHeight();
                float pageWidth = reader.getPageSize(1).getWidth();

                // Placement optimisé dans la zone de signature (ajustez selon la mise en page du document)
                float signatureX = 100;
                float signatureY = 250; // Position Y adaptée à la mise en page du document

                signatureImage.setAbsolutePosition(signatureX, signatureY);
                content.addImage(signatureImage);

                // Ajouter une date de signature et nom du signataire
                content.beginText();
                content.setFontAndSize(com.itextpdf.text.pdf.BaseFont.createFont(
                        com.itextpdf.text.pdf.BaseFont.HELVETICA, com.itextpdf.text.pdf.BaseFont.CP1252, false), 10);

                // Ajouter le nom du signataire en dessous de la signature
                content.setTextMatrix(signatureX, signatureY - 15);
                content.showText(buyerName);

                // Ajouter la date de signature
                content.setTextMatrix(signatureX + 300, signatureY - 15);
                content.showText("Date: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()));
                content.endText();

                // Ajouter un filigrane ou un tampon "DOCUMENT SIGNÉ"
                PdfContentByte under = stamper.getUnderContent(1);
                content.saveState();

                // Texte en filigrane
                content.setFontAndSize(com.itextpdf.text.pdf.BaseFont.createFont(
                        com.itextpdf.text.pdf.BaseFont.HELVETICA_BOLD, com.itextpdf.text.pdf.BaseFont.CP1252, false), 50);
                content.setColorFill(new com.itextpdf.text.BaseColor(220, 220, 220, 50)); // Gris transparent

                // Rotation et positionnement du filigrane
                content.beginText();
                content.showTextAligned(com.itextpdf.text.Element.ALIGN_CENTER, "DOCUMENT SIGNÉ",
                        pageWidth/2, pageHeight/2, 45);
                content.endText();
                content.restoreState();

                // Fermer le PDF
                stamper.close();
                reader.close();

                // Supprimer le fichier temporaire de la signature
                new File(signatureFileName).delete();

                return signedPDFFileName;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(signedFileName -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);

                if (signedFileName != null) {
                    lblStatus.setText("Document signé avec succès!");

                    // Mettre à jour le statut du contrat
                    if (contrat != null) {
                        contrat.setStatus("Signé électroniquement");
                        try {
                            contratService.update(contrat);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Demander à l'utilisateur où sauvegarder le fichier final
                    saveSignedPDFLocally(new File(signedFileName));
                } else {
                    lblStatus.setText("Erreur lors de la signature");
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'appliquer la signature au document");
                }
            });
        });
    }

    /**
     * Méthode pour sauvegarder le PDF signé localement
     */
    private void saveSignedPDFLocally(File signedFile) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le document signé");
            fileChooser.setInitialFileName(signedFile.getName());
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Documents PDF", "*.pdf")
            );

            File saveFile = fileChooser.showSaveDialog(btnSigner.getScene().getWindow());

            if (saveFile != null) {
                Files.copy(signedFile.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Le document signé a été enregistré avec succès : " + saveFile.getAbsolutePath());

                // Nettoyer le fichier temporaire
                signedFile.delete();

                // Fermer la fenêtre
                closeWindow();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer le fichier : " + e.getMessage());
        }
    }

    private void uploadPDFToServer(File pdfFile) {
        progressIndicator.setVisible(true);
        lblStatus.setText("Téléchargement du document...");

        CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost uploadFile = new HttpPost(API_BASE_URL + "/upload-pdf");

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addBinaryBody(
                        "file",
                        pdfFile,
                        ContentType.APPLICATION_OCTET_STREAM,
                        pdfFile.getName()
                );

                HttpEntity multipart = builder.build();
                uploadFile.setEntity(multipart);

                try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                    HttpEntity responseEntity = response.getEntity();
                    String responseString = EntityUtils.toString(responseEntity);

                    JSONObject jsonResponse = new JSONObject(responseString);
                    return jsonResponse.getString("file_id");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(id -> {
            Platform.runLater(() -> {
                if (id != null) {
                    this.fileId = id;
                    lblStatus.setText("Document prêt pour signature");
                    btnSigner.setDisable(false);
                    progressIndicator.setVisible(false);
                } else {
                    lblStatus.setText("Erreur lors du téléchargement");
                    progressIndicator.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de télécharger le document");
                }
            });
        });
    }

    private void processSignedPDF() {
        if (fileId == null || signatureBase64 == null || buyerName == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Informations manquantes pour signer le document");
            return;
        }

        progressIndicator.setVisible(true);
        lblStatus.setText("Application de la signature...");

        CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost signRequest = new HttpPost(API_BASE_URL + "/sign-pdf");
                signRequest.setHeader("Content-Type", "application/json");

                // Préparer les données de signature
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("file_id", fileId);
                requestData.put("signature_image", signatureBase64);
                requestData.put("buyer_name", buyerName);
                requestData.put("page", 0); // Première page
                requestData.put("x", 100); // Position X de la signature
                requestData.put("y", 200); // Position Y de la signature ajustée pour être plus visible

                String jsonRequestData = new JSONObject(requestData).toString();
                signRequest.setEntity(new StringEntity(jsonRequestData));

                try (CloseableHttpResponse response = httpClient.execute(signRequest)) {
                    HttpEntity responseEntity = response.getEntity();
                    String responseString = EntityUtils.toString(responseEntity);

                    JSONObject jsonResponse = new JSONObject(responseString);
                    return jsonResponse.getString("signed_pdf_id");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(signedId -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);

                if (signedId != null) {
                    lblStatus.setText("Document signé avec succès!");

                    // Si nous avons un contrat, mettre à jour son statut
                    if (contrat != null) {
                        contrat.setStatus("Signé électroniquement");
                        contrat.setSignature_id(signedId); // Stocker l'ID du document signé
                        try {
                            contratService.update(contrat);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    downloadSignedPDF(signedId);
                } else {
                    lblStatus.setText("Erreur lors de la signature");
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'appliquer la signature au document");
                }
            });
        });
    }

    private void downloadSignedPDF(String signedId) {
        progressIndicator.setVisible(true);
        lblStatus.setText("Téléchargement du document signé...");

        CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                // Préparer le nom du fichier téléchargé
                String outputFileName = "Contrat_" + (contrat != null ? contrat.getId() + "_" : "") + "signé.pdf";

                // Construire l'URL de téléchargement
                String downloadUrl = API_BASE_URL + "/get-signed-pdf/" + signedId +
                        "?download=true&filename=" + outputFileName;

                HttpGet downloadRequest = new HttpGet(downloadUrl);

                try (CloseableHttpResponse response = httpClient.execute(downloadRequest)) {
                    // Vérifier si la réponse est OK
                    if (response.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = response.getEntity();

                        // Demander à l'utilisateur où sauvegarder le fichier
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Enregistrer le document signé");
                        fileChooser.setInitialFileName(outputFileName);
                        fileChooser.getExtensionFilters().add(
                                new FileChooser.ExtensionFilter("Documents PDF", "*.pdf")
                        );

                        // Ici on doit utiliser Platform.runLater et attendre le résultat
                        CompletableFuture<File> futureResult = new CompletableFuture<>();
                        Platform.runLater(() -> {
                            File file = fileChooser.showSaveDialog(btnSigner.getScene().getWindow());
                            futureResult.complete(file);
                        });

                        File saveFile = futureResult.get();

                        if (saveFile != null) {
                            // Sauvegarder le contenu dans le fichier
                            Path outputPath = Paths.get(saveFile.getAbsolutePath());
                            Files.copy(entity.getContent(), outputPath, StandardCopyOption.REPLACE_EXISTING);
                            return saveFile.getAbsolutePath();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).thenAccept(filePath -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);

                if (filePath != null) {
                    lblStatus.setText("Document signé enregistré sous: " + filePath);
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Le document signé a été enregistré avec succès!");

                    // Nettoyer les fichiers temporaires sur le serveur
                    cleanupServerFiles();

                    // Fermer la fenêtre de signature
                    closeWindow();
                } else {
                    lblStatus.setText("Erreur lors de l'enregistrement du document signé");
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible d'enregistrer le document signé");
                }
            });
        });
    }

    private void cleanupServerFiles() {
        if (fileId == null) return;

        CompletableFuture.runAsync(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost cleanupRequest = new HttpPost(API_BASE_URL + "/cleanup/" + fileId);
                httpClient.execute(cleanupRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void closeWindow() {
        Stage stage = (Stage) btnSigner.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}