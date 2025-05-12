package controllers.FrontOffice.parcelles;

import controllers.FrontOffice.BaseFrontController;
import controllers.FrontOffice.contrats.Ajoutercontrat;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import Models.ParcelleProprietes;
import services.HistoriqueLocationService;

import java.io.IOException;

public class DetailParcelle {

    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label detailsLabel;
    @FXML private ImageView imageView;
    @FXML private Button createContractButton;

    private ParcelleProprietes parcelleCourante;
    private final HistoriqueLocationService historiqueService = new HistoriqueLocationService();

    public void initData(ParcelleProprietes parcelle) {
        this.parcelleCourante = parcelle;

        titleLabel.setText(parcelle.getTitre());
        priceLabel.setText(String.format("%.2f DT", parcelle.getPrix()));

        String details = String.format(
                "Type: %s\nStatut: %s\nTaille: %.2f m²\nPropriétaire: %s\nContact: %s",
                parcelle.getType_terrain(),
                parcelle.getStatus(),
                parcelle.getTaille(),
                parcelle.getNom_proprietaire(),
                parcelle.getContact_proprietaire()
        );
        detailsLabel.setText(details);

        try {
            Image image = new Image(getClass().getResourceAsStream("/" + parcelle.getImage()));
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.png")));
        }
    }

    @FXML
    private void handleCreateContract() {
        try {
            // 1. Charger le layout de base
            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            // 2. Charger le formulaire d'ajout de contrat
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/contrats/Ajoutercontrat.fxml"));
            Parent content = contentLoader.load();

            // 3. Récupérer le contrôleur et initialiser avec les données de la parcelle
            Ajoutercontrat ajouterContratController = contentLoader.getController();
            ajouterContratController.initWithParcelleInfo(parcelleCourante.getId(), parcelleCourante.getTitre());

            // 4. Injecter le contenu
            baseController.getContentPane().getChildren().setAll(content);

            // 5. Récupérer la fenêtre actuelle et changer de scène
            Stage stage = (Stage) createContractButton.getScene().getWindow();
            stage.setScene(new Scene(baseRoot));

            // 6. Synchroniser l'historique après la création du contrat (sera appelé après l'ajout)
            historiqueService.syncContratToHistorique(parcelleCourante.getId());

        } catch (IOException e) {
            System.err.println("Erreur lors de la navigation vers la page d'ajout de contrat: " + e.getMessage());
            e.printStackTrace();
        }
    }
}