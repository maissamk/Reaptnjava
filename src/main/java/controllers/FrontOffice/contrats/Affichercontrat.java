package controllers.FrontOffice.contrats;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import controllers.FrontOffice.BaseFrontController;
import controllers.FrontOffice.Home;
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

    //new nouton afficher parcelle
    @FXML
    private void handleAfficherParcelles() {
        try {
            // Load Home.fxml which contains the navbar
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent homeRoot = homeLoader.load();
            Home homeController = homeLoader.getController();

            // Load the parcelle list content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/parcelles/Afficherparcelles.fxml"));
            Parent content = contentLoader.load();

            // Set the content in Home's content pane
            homeController.getMainContentPane().getChildren().setAll(content);

            // Update the stage
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(homeRoot));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Navigation impossible : " + e.getMessage());
        }
    }




    private void openDetailView(Contrat contrat) {
        try {
            // Load Home.fxml which contains the navbar
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent homeRoot = homeLoader.load();
            Home homeController = homeLoader.getController();

            // Load the detail content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/contrats/DetailleContrat.fxml"));
            Parent content = contentLoader.load();

            // Get the detail controller and pass the selected Contrat
            DetailContrat controller = contentLoader.getController();
            controller.initData(contrat);

            // Set the content in Home's content pane
            homeController.getMainContentPane().getChildren().setAll(content);

            // Update the stage
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(homeRoot));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", e.getMessage());
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

    // Remplacer la méthode envoyerPourSignature dans la classe Affichercontrat
    private void envoyerPourSignature(Contrat contrat) {
        try {
            // Ouvrir la fenêtre de signature
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/contrats/SignatureContrat.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et initialiser avec le contrat sélectionné
            SignatureContratController controller = loader.getController();
            controller.initWithContrat(contrat);

            // Afficher dans une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Signature Électronique - Contrat N°" + contrat.getId());
            stage.showAndWait();

            // Après fermeture de la fenêtre, actualiser la liste
            loadData();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface de signature : " + e.getMessage());
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
            // Load Home.fxml which contains the navbar
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Home.fxml"));
            Parent homeRoot = homeLoader.load();
            Home homeController = homeLoader.getController();

            // Load the add contract content
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/contrats/Ajoutercontrat.fxml"));
            Parent content = contentLoader.load();

            // Set the content in Home's content pane
            homeController.getMainContentPane().getChildren().setAll(content);

            // Update the stage
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(homeRoot));
            stage.show();
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
