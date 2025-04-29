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
import models.Contrat;
import services.ContratService;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

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
            private final Label status = new Label("Contrat envoyé pour signature");

            {
                // Configuration du style
                card.setSpacing(8);
                card.setStyle("-fx-background-color: #ffffff; -fx-padding: 15; "
                        + "-fx-border-color: #e0e0e0; -fx-border-width: 1; "
                        + "-fx-border-radius: 4; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 3);");

                // Style header
                contratId.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2A4365;");
                dates.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
                header.setSpacing(20);

                // Configuration grid
                details.setHgap(10);
                details.setVgap(8);
                acheteurLabel.setStyle("-fx-font-weight: bold;");
                vendeurLabel.setStyle("-fx-font-weight: bold;");
                infoLabel.setStyle("-fx-font-weight: bold;");
                creation.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6;");

                // Style des boutons
                actions.setSpacing(10);
                btnModifier.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                btnPDF.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                status.setStyle("-fx-text-fill: #f39c12; -fx-font-style: italic;");

                // Construction du layout
                header.getChildren().addAll(contratId, dates);
                details.addRow(0, acheteurLabel, acheteur);
                details.addRow(1, vendeurLabel, vendeur);
                details.addRow(2, infoLabel, info);

                actions.getChildren().addAll(btnModifier, btnSupprimer, btnPDF, status);

                card.getChildren().addAll(
                        header,
                        sep1,
                        details,
                        new Separator(),
                        creation,
                        sep2,
                        actions
                );

                // Gestionnaires d'événements
                btnModifier.setOnAction(event -> openModificationWindow(getItem()));
                btnSupprimer.setOnAction(event -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Êtes-vous sûr de vouloir supprimer ce contrat ?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Confirmation de suppression");
                    confirm.setHeaderText("Supprimer le contrat n°" + getItem().getId());

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            service.delete(getItem());
                            loadData();
                        }
                    });
                });
                btnPDF.setOnAction(event -> generatePDF(getItem()));
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
                    setGraphic(card);
                }
            }
        });
    }

    private void openModificationWindow(Contrat contrat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/contrats/Modifiercontrat.fxml"));
            Parent root = loader.load();

            Modifiercontrat controller = loader.getController();
            controller.initData(contrat);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Contrat");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadData();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generatePDF(Contrat contrat) {
        try {
            // Configuration du document
            Document document = new Document(PageSize.A4, 50, 50, 70, 50);
            String fileName = "Contrat_" + contrat.getId() + ".pdf";
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));

            // Ajout d'événements pour entête et pied de page
            PDFHeaderFooter headerFooter = new PDFHeaderFooter(contrat.getId());
            writer.setPageEvent(headerFooter);

            document.open();

            // Titre du contrat
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new BaseColor(44, 62, 80));
            Paragraph title = new Paragraph("CONTRAT AGRICOLE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Numéro du contrat
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 16, new BaseColor(52, 73, 94));
            Paragraph contractNumber = new Paragraph("N° " + contrat.getId(), subtitleFont);
            contractNumber.setAlignment(Element.ALIGN_CENTER);
            contractNumber.setSpacingAfter(30);
            document.add(contractNumber);

            // Section des Parties
            addSectionHeader(document, "ENTRE LES PARTIES");

            // Information des parties
            PdfPTable partiesTable = new PdfPTable(2);
            partiesTable.setWidthPercentage(100);
            partiesTable.setSpacingBefore(10);
            partiesTable.setSpacingAfter(20);

            // Colonne Acheteur
            PdfPCell buyerCell = new PdfPCell();
            buyerCell.setBorder(Rectangle.BOX);
            buyerCell.setBorderColor(new BaseColor(189, 195, 199));
            buyerCell.setPadding(10);
            buyerCell.setBackgroundColor(new BaseColor(236, 240, 241));

            Paragraph buyerTitle = new Paragraph("ACHETEUR", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            buyerTitle.setSpacingAfter(5);
            buyerCell.addElement(buyerTitle);

            Paragraph buyerName = new Paragraph(contrat.getNom_acheteur(),
                    FontFactory.getFont(FontFactory.HELVETICA, 11));
            buyerCell.addElement(buyerName);

            // Colonne Vendeur
            PdfPCell sellerCell = new PdfPCell();
            sellerCell.setBorder(Rectangle.BOX);
            sellerCell.setBorderColor(new BaseColor(189, 195, 199));
            sellerCell.setPadding(10);
            sellerCell.setBackgroundColor(new BaseColor(236, 240, 241));

            Paragraph sellerTitle = new Paragraph("VENDEUR", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            sellerTitle.setSpacingAfter(5);
            sellerCell.addElement(sellerTitle);

            Paragraph sellerName = new Paragraph(contrat.getNom_vendeur(),
                    FontFactory.getFont(FontFactory.HELVETICA, 11));
            sellerCell.addElement(sellerName);

            partiesTable.addCell(buyerCell);
            partiesTable.addCell(sellerCell);
            document.add(partiesTable);

            // Section des modalités
            addSectionHeader(document, "MODALITÉS");

            // Dates du contrat
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10);
            infoTable.setSpacingAfter(20);
            float[] columnWidths = {0.3f, 0.7f};
            infoTable.setWidths(columnWidths);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            addTableRow(infoTable, "Date de début:", sdf.format(contrat.getDate_debut_contrat()));
            addTableRow(infoTable, "Date de fin:", sdf.format(contrat.getDatefin_contrat()));
            addTableRow(infoTable, "Date de création:", sdf.format(contrat.getDatecreation_contrat()));

            document.add(infoTable);

            // Informations du contrat
            addSectionHeader(document, "INFORMATIONS COMPLÉMENTAIRES");

            Paragraph infoParagraph = new Paragraph(contrat.getInformation_contrat(),
                    FontFactory.getFont(FontFactory.HELVETICA, 11));
            infoParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
            infoParagraph.setSpacingAfter(20);
            document.add(infoParagraph);

            // Section signatures
            addSectionHeader(document, "SIGNATURES");

            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            signatureTable.setSpacingBefore(15);

            // Signature acheteur
            PdfPCell buyerSignature = new PdfPCell();
            buyerSignature.setBorder(Rectangle.NO_BORDER);
            buyerSignature.setPadding(10);

            Paragraph buyerSignTitle = new Paragraph("L'Acheteur",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            buyerSignTitle.setAlignment(Element.ALIGN_CENTER);
            buyerSignTitle.setSpacingAfter(50);
            buyerSignature.addElement(buyerSignTitle);

            Paragraph buyerSignName = new Paragraph(contrat.getNom_acheteur(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            buyerSignName.setAlignment(Element.ALIGN_CENTER);
            buyerSignature.addElement(buyerSignName);

            // Signature vendeur
            PdfPCell sellerSignature = new PdfPCell();
            sellerSignature.setBorder(Rectangle.NO_BORDER);
            sellerSignature.setPadding(10);

            Paragraph sellerSignTitle = new Paragraph("Le Vendeur",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            sellerSignTitle.setAlignment(Element.ALIGN_CENTER);
            sellerSignTitle.setSpacingAfter(50);
            sellerSignature.addElement(sellerSignTitle);

            Paragraph sellerSignName = new Paragraph(contrat.getNom_vendeur(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            sellerSignName.setAlignment(Element.ALIGN_CENTER);
            sellerSignature.addElement(sellerSignName);

            signatureTable.addCell(buyerSignature);
            signatureTable.addCell(sellerSignature);

            document.add(signatureTable);

            document.close();

            // Ouvrir le PDF
            Desktop.getDesktop().open(new File(fileName));

            showAlert("Succès", "Le PDF a été généré avec succès et enregistré sous: " + fileName);

        } catch (Exception e) {
            showAlert("Erreur PDF", "Échec de la génération : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addSectionHeader(Document document, String title) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(41, 128, 185));
        Paragraph sectionTitle = new Paragraph(title, sectionFont);
        sectionTitle.setSpacingBefore(15);
        sectionTitle.setSpacingAfter(10);

        // Ligne horizontale
        LineSeparator line = new LineSeparator(1, 100, new BaseColor(189, 195, 199), Element.ALIGN_CENTER, -5);

        document.add(sectionTitle);
        document.add(line);
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        PdfPCell cell1 = new PdfPCell(new Phrase(label, labelFont));
        cell1.setBorderColor(new BaseColor(189, 195, 199));
        cell1.setPadding(8);
        cell1.setBackgroundColor(new BaseColor(236, 240, 241));

        PdfPCell cell2 = new PdfPCell(new Phrase(value, valueFont));
        cell2.setBorderColor(new BaseColor(189, 195, 199));
        cell2.setPadding(8);

        table.addCell(cell1);
        table.addCell(cell2);
    }

    // Classe interne pour gérer les en-têtes et pieds de page
    private static class PDFHeaderFooter extends PdfPageEventHelper {
        private final int contractId;

        public PDFHeaderFooter(int contractId) {
            this.contractId = contractId;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // En-tête
            PdfPTable header = new PdfPTable(2);
            try {
                header.setWidths(new float[] {0.7f, 0.3f});
                header.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
                header.setLockedWidth(true);

                // Logo ou titre
                PdfPCell logoCell = new PdfPCell(new Phrase("AGRISERVE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(46, 204, 113))));
                logoCell.setBorder(Rectangle.BOTTOM);
                logoCell.setBorderColor(new BaseColor(189, 195, 199));
                logoCell.setPadding(10);

                // Numéro de contrat
                PdfPCell contractCell = new PdfPCell(new Phrase("Contrat N° " + contractId, FontFactory.getFont(FontFactory.HELVETICA, 9)));
                contractCell.setBorder(Rectangle.BOTTOM);
                contractCell.setBorderColor(new BaseColor(189, 195, 199));
                contractCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                contractCell.setPadding(10);

                header.addCell(logoCell);
                header.addCell(contractCell);

                header.writeSelectedRows(0, -1, document.leftMargin(), document.getPageSize().getHeight() - 20, cb);

                // Pied de page
                PdfPTable footer = new PdfPTable(2);
                footer.setWidths(new float[] {0.8f, 0.2f});
                footer.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
                footer.setLockedWidth(true);

                // Copyright
                PdfPCell copyrightCell = new PdfPCell(new Phrase("© 2025 AgriServe - Tous droits réservés", FontFactory.getFont(FontFactory.HELVETICA, 8, new BaseColor(127, 140, 141))));
                copyrightCell.setBorder(Rectangle.TOP);
                copyrightCell.setBorderColor(new BaseColor(189, 195, 199));
                copyrightCell.setPadding(5);

                // Numéro de page
                PdfPCell pageNumCell = new PdfPCell(new Phrase(String.format("Page %d", writer.getPageNumber()), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                pageNumCell.setBorder(Rectangle.TOP);
                pageNumCell.setBorderColor(new BaseColor(189, 195, 199));
                pageNumCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pageNumCell.setPadding(5);

                footer.addCell(copyrightCell);
                footer.addCell(pageNumCell);

                footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() + 15, cb);

            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadData() {
        try {
            List<Contrat> contrats = service.getAll();
            ObservableList<Contrat> data = FXCollections.observableArrayList(contrats);
            listView.setItems(data);
        } catch (Exception e) {
            showAlert("Erreur de chargement", "Impossible de charger les contrats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActualiser() {
        loadData();
    }

    @FXML
    private void handleRetour() {
        try {
            // 1. Charger le layout de base
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            // 2. Charger le contenu Ajoutercontrat
            Parent content = FXMLLoader.load(getClass().getResource("/FrontOffice/contrats/Ajoutercontrat.fxml"));

            // 3. Injecter dans le contentPane
            baseController.getContentPane().getChildren().setAll(content);

            // 4. Récupérer la fenêtre actuelle
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(baseRoot));

        } catch (IOException e) {
            showAlert("Erreur Navigation",
                    "Impossible de revenir à l'ajout :\n"
                            + "1. Vérifiez que Ajoutercontrat.fxml existe\n"
                            + "2. Vérifiez le chemin: /FrontOffice/contrats/\n"
                            + "Erreur technique: " + e.getMessage());
            e.printStackTrace();
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
