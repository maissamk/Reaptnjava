package controllers.BackOffice.GestionCommandeBack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import Models.gestionCommande.CommandeDetails;
import services.gestionCommande.CommandeService;

public class ArchivesController {

    @FXML private TableView<CommandeDetails> tableArchive;
    @FXML private TableColumn<CommandeDetails, Integer> colIdCommande;
    @FXML private TableColumn<CommandeDetails, Integer> colQuantite;
    @FXML private TableColumn<CommandeDetails, Float> colTotal;
    @FXML private TableColumn<CommandeDetails, java.util.Date> colDateCommande;
    @FXML private TableColumn<CommandeDetails, String> colAdresse;
    @FXML private TableColumn<CommandeDetails, String> colStatut;

    @FXML
    public void initialize() {
        colIdCommande.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCommande().getId()).asObject());
        colQuantite.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCommande().getQuantite()).asObject());
        colTotal.setCellValueFactory(data -> new SimpleFloatProperty(data.getValue().getCommande().getTotale()).asObject());
        colDateCommande.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCommande().getDate_commande()));
        colAdresse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLivraison().getAdresse()));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLivraison().getStatus()));

        CommandeService service = new CommandeService();
        ObservableList<CommandeDetails> commandesLivrees = FXCollections.observableArrayList();

        for (CommandeDetails cmd : service.getCommandesAvecDetails()) {
            if (cmd.getLivraison() != null && "Livrée".equalsIgnoreCase(cmd.getLivraison().getStatus())) {
                commandesLivrees.add(cmd);
            }
        }

        tableArchive.setItems(commandesLivrees);
    }
}
