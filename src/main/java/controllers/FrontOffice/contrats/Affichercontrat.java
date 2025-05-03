package controllers.FrontOffice.contrats;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import controllers.FrontOffice.BaseFrontController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import Models.Contrat;
import services.ContratService;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Base64;
import javafx.scene.control.Tooltip;

public class Affichercontrat {

    @FXML private ListView<Contrat> listView;
    @FXML private Button btnActualiser;
    @FXML private Button btnRetour;

    private final ContratService service = new ContratService();

    @FXML
    public void initialize() {
        configureListView();
        loadData();
    }

    private void configureListView() {
        listView.setCellFactory(param -> new ListCell<Contrat>() {
            private final VBox card = new VBox();
            private final HBox header = new HBox();
            private final Label contratId = new Label();
            private final Label dates = new Label();
            private final Separator sep1 = new Separator();

            private final GridPane details = new GridPane();
            private final Label acheteurLabel = new Label("Acheteur :");
            private final Label acheteur = new Label();
            private final Label vendeurLabel = new Label("Vendeur :");
            private final Label vendeur = new Label();
            private final Label infoLabel = new Label("Informations :");
            private final Label info = new Label();
            private final Label creation = new Label();

            private final Separator sep2 = new Separator();
            private final HBox actions = new HBox();
            private final Button btnModifier = new Button("Éditer");
            private final Button btnSupprimer = new Button("Supprimer");
            private final Button btnPDF = new Button("Télécharger (PDF)");
            private final Button btnSigner = new Button("Envoyer pour signature");
            private final Label status = new Label();

            {
                // Configuration existante
                card.setSpacing(8);
                card.setStyle("-fx-background-color: #ffffff; -fx-padding: 15; "
                        + "-fx-border-color: #e0e0e0; -fx-border-width: 1; "
                        + "-fx-border-radius: 4; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 3);");

                contratId.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2A4365;");
                dates.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
                header.setSpacing(20);

                details.setHgap(10);
                details.setVgap(8);
                acheteurLabel.setStyle("-fx-font-weight: bold;");
                vendeurLabel.setStyle("-fx-font-weight: bold;");
                infoLabel.setStyle("-fx-font-weight: bold;");
                creation.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6;");

                actions.setSpacing(10);
                btnModifier.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                btnPDF.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                btnSigner.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand;");
                status.setStyle("-fx-font-style: italic;");

                header.getChildren().addAll(contratId, dates);
                details.addRow(0, acheteurLabel, acheteur);
                details.addRow(1, vendeurLabel, vendeur);
                details.addRow(2, infoLabel, info);

                actions.getChildren().addAll(btnModifier, btnSupprimer, btnPDF, btnSigner, status);

                card.getChildren().addAll(
                        header,
                        sep1,
                        details,
                        new Separator(),
                        creation,
                        sep2,
                        actions
                );

                // Gestionnaires existants
                btnModifier.setOnAction(event -> openModificationWindow(getItem()));
                btnSupprimer.setOnAction(event -> confirmDelete(getItem()));
                btnPDF.setOnAction(event -> generatePDF(getItem()));

                // Nouveau gestionnaire signature
                btnSigner.setOnAction(event -> envoyerPourSignature(getItem()));


                card.setOnMouseClicked(event -> {
                    if (!isEmpty()) {
                        openDetailView(getItem());
                    }
                });
                card.setStyle(card.getStyle() + "; -fx-cursor: hand;");

                // Add a tooltip to indicate the card is clickable
                Tooltip tooltip = new Tooltip("Cliquez pour voir les détails");
                Tooltip.install(card, tooltip);

            }

            @Override
            protected void updateItem(Contrat contrat, boolean empty) {
                super.updateItem(contrat, empty);
                if (empty || contrat == null) {
                    setGraphic(null);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    contratId.setText("Contrat N°" + contrat.getId());
                    dates.setText(String.format("Début : %s | Fin : %s",
                            sdf.format(contrat.getDate_debut_contrat()),
                            sdf.format(contrat.getDatefin_contrat())));
                    acheteur.setText(contrat.getNom_acheteur());
                    vendeur.setText(contrat.getNom_vendeur());
                    info.setText(contrat.getInformation_contrat());
                    creation.setText("Créé le : " + sdf.format(contrat.getDatecreation_contrat()));

                    // Mise à jour du statut
                    if(contrat.getStatus() != null) {
                        status.setText(contrat.getStatus());
                        status.setStyle("-fx-text-fill: " +
                                (contrat.getStatus().contains("Envoyé") ? "#f39c12" :
                                        contrat.getStatus().contains("Signé") ? "#27ae60" : "#e74c3c"));
                    }
                    setGraphic(card);
                }
            }
        });
    }

    private void confirmDelete(Contrat contrat) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer ce contrat ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation suppression");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                service.delete(contrat);
                loadData();
            }
        });
    }

    private void openDetailView(Contrat contrat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/contrats/DetailContrat.fxml"));
            Parent root = loader.load();

            DetailContrat controller = loader.getController();
            controller.initData(contrat);

            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du Contrat");
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la vue détaillée : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openModificationWindow(Contrat contrat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/contrats/Modifiercontrat.fxml"));
            Parent root = loader.load();

            Modifiercontrat controller = loader.getController();
            controller.initData(contrat);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modification Contrat");
            stage.showAndWait();

            loadData();
        } catch (IOException e) {
            showAlert("Erreur", "Ouverture impossible : " + e.getMessage());
        }
    }

    private void generatePDF(Contrat contrat) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 70, 50);
            String fileName = "Contrat_" + contrat.getId() + ".pdf";
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));

            writer.setPageEvent(new PDFHeaderFooter(contrat.getId()));

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new BaseColor(44, 62, 80));
            Paragraph title = new Paragraph("CONTRAT AGRICOLE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 16, new BaseColor(52, 73, 94));
            Paragraph numContrat = new Paragraph("N° " + contrat.getId(), subtitleFont);
            numContrat.setAlignment(Element.ALIGN_CENTER);
            numContrat.setSpacingAfter(20);
            document.add(numContrat);

            addSectionHeader(document, "PARTIES CONTRACTANTES");

            PdfPTable parties = new PdfPTable(2);
            parties.setWidthPercentage(100);
            parties.addCell(createCell("ACHETEUR", contrat.getNom_acheteur()));
            parties.addCell(createCell("VENDEUR", contrat.getNom_vendeur()));
            document.add(parties);

            addSectionHeader(document, "DÉTAILS DU CONTRAT");

            PdfPTable details = new PdfPTable(2);
            details.setWidths(new float[]{0.3f, 0.7f});
            addTableRow(details, "Date début", new SimpleDateFormat("dd/MM/yyyy").format(contrat.getDate_debut_contrat()));
            addTableRow(details, "Date fin", new SimpleDateFormat("dd/MM/yyyy").format(contrat.getDatefin_contrat()));
            document.add(details);

            document.close();

            Desktop.getDesktop().open(new File(fileName));
            showAlert("Succès", "PDF généré : " + fileName);

        } catch (Exception e) {
            showAlert("Erreur", "Échec génération PDF : " + e.getMessage());
        }
    }

    private void envoyerPourSignature(Contrat contrat) {
        try {
            // Génération PDF
            String pdfPath = generatePDFForSignature(contrat);
            File pdfFile = new File(pdfPath);

            // Configuration requête
            // CORRECTION: Utiliser la bonne URL d'API selon la version de l'API YouSign (v3)
            URL url = new URL("https://api-sandbox.yousign.app/v3");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            // CORRECTION: Utiliser le bon format d'autorisation et clé API
            conn.setRequestProperty("Authorization", "Bearer CSF81RXIvlkd9QUtvzsM0EwLmVGd5Tn2");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Construction du corps de la requête selon la documentation v3
            JSONObject body = new JSONObject();
            body.put("name", "Contrat Agricole #" + contrat.getId());
            body.put("delivery_mode", "email");
            body.put("timezone", "Europe/Paris");

            // Paramètres de notification
            JSONObject notification = new JSONObject();
            notification.put("procedure", true);
            body.put("notification", notification);

            // Configuration de la procédure
            JSONObject config = new JSONObject();
            config.put("type", "electronic_signature");
            body.put("config", config);

            // Ajouter le fichier encodé en base64
            JSONArray documents = new JSONArray();
            JSONObject document = new JSONObject();
            document.put("name", pdfFile.getName());
            document.put("type", "signable");
            document.put("content", encodeFileToBase64(pdfFile));
            documents.put(document);
            body.put("documents", documents);

            // Ajouter les signataires
            JSONArray signers = new JSONArray();

            // Premier signataire (acheteur)
            JSONObject acheteur = new JSONObject();
            acheteur.put("first_name", splitName(contrat.getNom_acheteur())[0]);
            acheteur.put("last_name", splitName(contrat.getNom_acheteur())[1]);
            acheteur.put("email", "yosrsfaihi@gmail.com");
            acheteur.put("phone", "+33600000000");

            // Méthode d'authentification pour l'acheteur
            JSONObject acheteurAuth = new JSONObject();
            acheteurAuth.put("type", "email");
            acheteur.put("verification", acheteurAuth);

            signers.put(acheteur);

            // Deuxième signataire (vendeur)
            JSONObject vendeur = new JSONObject();
            vendeur.put("first_name", splitName(contrat.getNom_vendeur())[0]);
            vendeur.put("last_name", splitName(contrat.getNom_vendeur())[1]);
            vendeur.put("email", "yosrsfaihi@gmail.com");
            vendeur.put("phone", "+33600000000");

            // Méthode d'authentification pour le vendeur
            JSONObject vendeurAuth = new JSONObject();
            vendeurAuth.put("type", "email");
            vendeur.put("verification", vendeurAuth);

            signers.put(vendeur);

            body.put("signers", signers);

            // Configurer les champs de signature pour chaque signataire
            JSONArray signatureFields = new JSONArray();

            // Champ de signature pour l'acheteur (premier signataire)
            JSONObject acheteurSignature = new JSONObject();
            acheteurSignature.put("document_index", 0);
            acheteurSignature.put("signer_index", 0);
            acheteurSignature.put("page", 1);
            acheteurSignature.put("x", 50);
            acheteurSignature.put("y", 700);
            signatureFields.put(acheteurSignature);

            // Champ de signature pour le vendeur (deuxième signataire)
            JSONObject vendeurSignature = new JSONObject();
            vendeurSignature.put("document_index", 0);
            vendeurSignature.put("signer_index", 1);
            vendeurSignature.put("page", 1);
            vendeurSignature.put("x", 300);
            vendeurSignature.put("y", 700);
            signatureFields.put(vendeurSignature);

            body.put("signature_fields", signatureFields);

            // Logging pour debugging
            System.out.println("Request body: " + body.toString());

            // Envoi de la requête
            try (OutputStream os = conn.getOutputStream();
                 OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8")) {
                osw.write(body.toString());
                osw.flush();
            }

            // Traitement de la réponse
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                // Lecture de la réponse
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                // Parsing de la réponse JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                System.out.println("Success response: " + jsonResponse.toString());

                // Stockage des IDs dans l'objet contrat
                if (jsonResponse.has("id")) {
                    contrat.setSignature_id(jsonResponse.getString("id"));
                }

                // Mise à jour du statut du contrat
                contrat.setStatus("Envoyé pour signature");
                service.update(contrat);
                loadData();

                showAlert("Succès", "Demande de signature envoyée avec succès!");
            } else {
                // Lecture du message d'erreur
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                }

                System.err.println("Error response: " + errorResponse.toString());
                showAlert("Erreur", "Échec de l'envoi : " + responseCode + "\n" + errorResponse.toString());
            }

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'envoi : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generatePDFForSignature(Contrat contrat) throws Exception {
        String fileName = "Contrat_Signature_" + contrat.getId() + ".pdf";
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        // Contenu identique à generatePDF()
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.BLACK);
        document.add(new Paragraph("CONTRAT POUR SIGNATURE", titleFont));

        PdfPTable table = new PdfPTable(2);
        table.addCell("Acheteur");
        table.addCell(contrat.getNom_acheteur());
        table.addCell("Vendeur");
        table.addCell(contrat.getNom_vendeur());
        document.add(table);

        document.close();
        return fileName;
    }

    private String encodeFileToBase64(File file) throws IOException {
        byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    private String[] splitName(String fullName) {
        String[] names = fullName.split(" ", 2);
        return names.length == 1 ? new String[]{names[0], ""} : names;
    }

    private PdfPCell createCell(String title, String content) throws DocumentException {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(10);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        cell.addElement(new Paragraph(title, titleFont));
        cell.addElement(new Paragraph(content, contentFont));

        return cell;
    }

    private void addSectionHeader(Document document, String text) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(20);
        p.setSpacingAfter(10);
        document.add(p);
        document.add(new LineSeparator());
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        table.addCell(new Phrase(label, labelFont));
        table.addCell(new Phrase(value, valueFont));
    }

    private static class PDFHeaderFooter extends PdfPageEventHelper {
        private final int contractId;

        public PDFHeaderFooter(int contractId) {
            this.contractId = contractId;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(1);
            footer.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());

            Paragraph p = new Paragraph(
                    "Contrat N°" + contractId + " - Page " + writer.getPageNumber(),
                    FontFactory.getFont(FontFactory.HELVETICA, 8)
            );
            p.setAlignment(Element.ALIGN_CENTER);

            PdfPCell cell = new PdfPCell(p);
            cell.setBorder(Rectangle.TOP);
            cell.setBorderColor(BaseColor.LIGHT_GRAY);
            footer.addCell(cell);

            footer.writeSelectedRows(0, -1,
                    document.leftMargin(),
                    document.bottomMargin() + 10,
                    writer.getDirectContent());
        }
    }

    private void loadData() {
        try {
            List<Contrat> contrats = service.getAll();
            ObservableList<Contrat> data = FXCollections.observableArrayList(contrats);
            listView.setItems(data);
        } catch (Exception e) {
            showAlert("Erreur", "Chargement échoué : " + e.getMessage());
        }
    }

    @FXML
    private void handleActualiser() {
        loadData();
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();

            BaseFrontController controller = baseLoader.getController();
            Parent content = FXMLLoader.load(getClass().getResource("/FrontOffice/contrats/Ajoutercontrat.fxml"));
            controller.getContentPane().getChildren().setAll(content);

            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(baseRoot));

        } catch (IOException e) {
            showAlert("Erreur", "Navigation impossible : " + e.getMessage());
        }
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
