package controllers.FrontOffice.contrats;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
                    service.delete(getItem());
                    loadData();
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
        }
    }

    private void generatePDF(Contrat contrat) {
        try {
            Document document = new Document();
            String fileName = "Contrat_" + contrat.getId() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();

            // En-tête
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph("CONTRAT AGRICOLE N°" + contrat.getId(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Contenu
            document.add(new Paragraph("\n"));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addTableRow(table, "Date Début :", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(contrat.getDate_debut_contrat()));
            addTableRow(table, "Date Fin :", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(contrat.getDatefin_contrat()));
            addTableRow(table, "Acheteur :", contrat.getNom_acheteur());
            addTableRow(table, "Vendeur :", contrat.getNom_vendeur());
            addTableRow(table, "Informations :", contrat.getInformation_contrat());

            document.add(table);
            document.close();

            // Ouvrir le PDF
            Desktop.getDesktop().open(new File(fileName));

        } catch (Exception e) {
            showAlert("Erreur PDF", "Échec de la génération : " + e.getMessage());
        }
    }

    private void addTableRow(PdfPTable table, String label, String value) throws DocumentException {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        PdfPCell cell2 = new PdfPCell(new Phrase(value));
        cell1.setBorder(PdfPCell.NO_BORDER);
        cell2.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell1);
        table.addCell(cell2);
    }

    private void loadData() {
        List<Contrat> contrats = service.getAll();
        ObservableList<Contrat> data = FXCollections.observableArrayList(contrats);
        listView.setItems(data);
    }

    @FXML
    private void handleActualiser() {
        loadData();
    }

    @FXML
    private void handleRetour() {
//        try {
//            Parent root = FXMLLoader.load(getClass().getResource("/FrontOffice/contrats/Ajoutercontrat.fxml"));
//            Stage stage = (Stage) listView.getScene().getWindow();
//            stage.setScene(new Scene(root));
//            stage.show();
//        } catch (IOException e) {
//            showAlert("Erreur", "Impossible de charger la vue d'ajout");
//        }



        //new code :
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