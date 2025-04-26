package controllers.BackOffice.GestionCommandeBack;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import Models.gestionCommande.CommandeDetails;
import services.gestionCommande.CommandeService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleFloatProperty;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class ArchivesController {

    @FXML
    private TableView<CommandeDetails> tableArchives;
    @FXML
    private TableColumn<CommandeDetails, Integer> colIdArchive;
    @FXML
    private TableColumn<CommandeDetails, Integer> colQuantiteArchive;
    @FXML
    private TableColumn<CommandeDetails, Float> colTotalArchive;
    @FXML
    private TableColumn<CommandeDetails, Date> colDateCommandeArchive;
    @FXML
    private TableColumn<CommandeDetails, String> colAdresseArchive;
    @FXML
    private TableColumn<CommandeDetails, Date> colDateLivraisonArchive;
    @FXML
    private TableColumn<CommandeDetails, String> colPaiementArchive;
    @FXML
    private TableColumn<CommandeDetails, Date> colDatePaiementArchive;
    @FXML
    private TableColumn<CommandeDetails, Void> colActionArchive;

    @FXML
    private Label lblTotalArchives;
    @FXML
    private Label lblValeurTotale;
    @FXML
    private Label lblDelaiMoyen;

    @FXML
    private PieChart chartPaiements;
    @FXML
    private BarChart<String, Number> chartArchivesParMois;

    @FXML
    private TextField txtRechercheArchive;
    @FXML
    private DatePicker datePicker;

    private ObservableList<CommandeDetails> commandesArchivees;
    private FilteredList<CommandeDetails> commandesArchiveesFiltered;
    private CommandeService service;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    // Modifiez la méthode initialize() pour ajouter l'initialisation du label de dernière mise à jour
    @FXML
    public void initialize() {
        service = new CommandeService();

        setupTableColumns();
        loadArchivedData();
        setupStatistics();
        setupFilters();

        // Initialiser le label de dernière mise à jour
        SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    private void setupTableColumns() {
        // Configuration des colonnes de base
        colIdArchive.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCommande().getId()).asObject());
        colQuantiteArchive.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCommande().getQuantite()).asObject());
        colTotalArchive.setCellValueFactory(data -> new SimpleFloatProperty(data.getValue().getCommande().getTotale()).asObject());
        colTotalArchive.setCellFactory(column -> new TableCell<CommandeDetails, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", item));
                }
            }
        });

        colDateCommandeArchive.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCommande().getDate_commande()));
        colDateCommandeArchive.setCellFactory(column -> new TableCell<CommandeDetails, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });

        colAdresseArchive.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLivraison() != null ? data.getValue().getLivraison().getAdresse() : "N/A"));

        colDateLivraisonArchive.setCellValueFactory(data -> new SimpleObjectProperty<>(
                data.getValue().getLivraison() != null ? data.getValue().getLivraison().getDateLiv() : null));
        colDateLivraisonArchive.setCellFactory(column -> new TableCell<CommandeDetails, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });

        colPaiementArchive.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPaiement() != null ? data.getValue().getPaiement().getMethodePaiement() : "N/A"));

        colDatePaiementArchive.setCellValueFactory(data -> new SimpleObjectProperty<>(
                data.getValue().getPaiement() != null ? data.getValue().getPaiement().getDatePaiement() : null));
        colDatePaiementArchive.setCellFactory(column -> new TableCell<CommandeDetails, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });

        setupActionButtons();
    }

    private void setupActionButtons() {
        colActionArchive.setCellFactory(new Callback<TableColumn<CommandeDetails, Void>, TableCell<CommandeDetails, Void>>() {
            @Override
            public TableCell<CommandeDetails, Void> call(TableColumn<CommandeDetails, Void> param) {
                return new TableCell<CommandeDetails, Void>() {
                    private final Button btnDetails = new Button("Détails");

                    {
                        btnDetails.getStyleClass().add("button-outline");
                        btnDetails.setOnAction(event -> {
                            CommandeDetails commande = getTableView().getItems().get(getIndex());
                            showCommandeDetails(commande);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnDetails);
                        }
                    }
                };
            }
        });
    }

    private void loadArchivedData() {
        // Charger les commandes archivées (celles avec statut "Livrée")
        List<CommandeDetails> listeCommandes = service.getCommandesAvecDetails();
        commandesArchivees = FXCollections.observableArrayList();

        for (CommandeDetails cmd : listeCommandes) {
            if (cmd.getLivraison() != null && "Livrée".equalsIgnoreCase(cmd.getLivraison().getStatus())) {
                commandesArchivees.add(cmd);
            }
        }

        commandesArchivees.sort((a, b) -> b.getCommande().getDate_commande().compareTo(a.getCommande().getDate_commande()));

        // Créer la liste filtrée
        commandesArchiveesFiltered = new FilteredList<>(commandesArchivees, p -> true);
        tableArchives.setItems(commandesArchiveesFiltered);
    }

    private void setupStatistics() {
        // Nombre total d'archives
        lblTotalArchives.setText(String.valueOf(commandesArchivees.size()));

        // Valeur totale
        double totalValue = commandesArchivees.stream()
                .mapToDouble(cmd -> cmd.getCommande().getTotale())
                .sum();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        lblValeurTotale.setText(currencyFormat.format(totalValue));

        // Délai moyen de livraison
        double delaiMoyen = 0;
        int commandesAvecDates = 0;

        for (CommandeDetails cmd : commandesArchivees) {
            if (cmd.getLivraison() != null && cmd.getLivraison().getDateLiv() != null && cmd.getCommande().getDate_commande() != null) {
                // Correction pour java.sql.Date
                LocalDate dateCommande;
                if (cmd.getCommande().getDate_commande() instanceof java.sql.Date) {
                    // Conversion d'une date SQL en LocalDate
                    dateCommande = ((java.sql.Date) cmd.getCommande().getDate_commande()).toLocalDate();
                } else {
                    // Conversion d'une date util en LocalDate
                    dateCommande = cmd.getCommande().getDate_commande().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }

                LocalDate dateLivraison;
                if (cmd.getLivraison().getDateLiv() instanceof java.sql.Date) {
                    // Conversion d'une date SQL en LocalDate
                    dateLivraison = ((java.sql.Date) cmd.getLivraison().getDateLiv()).toLocalDate();
                } else {
                    // Conversion d'une date util en LocalDate
                    dateLivraison = cmd.getLivraison().getDateLiv().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }

                long delai = ChronoUnit.DAYS.between(dateCommande, dateLivraison);
                if (delai >= 0) {  // Éviter les dates incohérentes
                    delaiMoyen += delai;
                    commandesAvecDates++;
                }
            }
        }

        if (commandesAvecDates > 0) {
            delaiMoyen = delaiMoyen / commandesAvecDates;
            lblDelaiMoyen.setText(String.format("%.1f jours", delaiMoyen));
        } else {
            lblDelaiMoyen.setText("N/A");
        }

        setupPaymentChart();
        setupMonthlyArchivesChart();
    }

    private void setupPaymentChart() {
        // Statistiques des méthodes de paiement
        Map<String, Integer> paiementsCount = new HashMap<>();

        for (CommandeDetails cmd : commandesArchivees) {
            if (cmd.getPaiement() != null) {
                String methodePaiement = cmd.getPaiement().getMethodePaiement();
                paiementsCount.put(methodePaiement, paiementsCount.getOrDefault(methodePaiement, 0) + 1);
            } else {
                paiementsCount.put("Non spécifié", paiementsCount.getOrDefault("Non spécifié", 0) + 1);
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        paiementsCount.forEach((methode, count) -> {
            if (count > 0) {
                pieChartData.add(new PieChart.Data(methode + " (" + count + ")", count));
            }
        });

        chartPaiements.setData(pieChartData);
    }

    private void setupMonthlyArchivesChart() {
        // Statistiques par mois
        Map<String, Integer> archivesParMois = new TreeMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy");

        // Initialiser les 6 derniers mois
        Calendar cal = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            cal.add(Calendar.MONTH, -1);
            String monthYear = monthFormat.format(cal.getTime());
            archivesParMois.put(monthYear, 0);
        }
        cal.add(Calendar.MONTH, 6);  // Remettre à la date actuelle

        // Compter les commandes par mois
        for (CommandeDetails cmd : commandesArchivees) {
            Date dateLivraison = cmd.getLivraison() != null ? cmd.getLivraison().getDateLiv() : null;
            if (dateLivraison != null) {
                String monthYear = monthFormat.format(dateLivraison);
                archivesParMois.put(monthYear, archivesParMois.getOrDefault(monthYear, 0) + 1);
            }
        }

        // Créer la série de données
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Commandes livrées");

        // Utiliser les 6 derniers mois uniquement
        List<String> lastSixMonths = new ArrayList<>(archivesParMois.keySet());
        Collections.sort(lastSixMonths);
        if (lastSixMonths.size() > 6) {
            lastSixMonths = lastSixMonths.subList(lastSixMonths.size() - 6, lastSixMonths.size());
        }

        for (String monthYear : lastSixMonths) {
            series.getData().add(new XYChart.Data<>(monthYear, archivesParMois.getOrDefault(monthYear, 0)));
        }

        chartArchivesParMois.getData().clear();
        chartArchivesParMois.getData().add(series);
    }

    private void setupFilters() {
        // Configuration du filtre de recherche textuelle
        txtRechercheArchive.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(newValue, datePicker.getValue());
        });

        // Configuration du filtre de date avec le DatePicker
        datePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, formatter);
                } else {
                    return null;
                }
            }
        });

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(txtRechercheArchive.getText(), newValue);
        });

        // Bouton pour effacer les filtres
        Button btnClearFilters = new Button("Effacer filtres");
        btnClearFilters.getStyleClass().add("button-secondary");
        btnClearFilters.setOnAction(event -> {
            txtRechercheArchive.clear();
            datePicker.setValue(null);
            applyFilters("", null);
        });

        // Ajouter le bouton à côté du DatePicker (à implémenter si nécessaire)
    }

    private void applyFilters(String searchText, LocalDate filterDate) {
        commandesArchiveesFiltered.setPredicate(commande -> {
            // Vérifier si la commande correspond à la date sélectionnée
            boolean matchesDate = true;
            if (filterDate != null) {
                Date dateCommande = commande.getCommande().getDate_commande();
                if (dateCommande != null) {
                    LocalDate commandeLocalDate;
                    if (dateCommande instanceof java.sql.Date) {
                        commandeLocalDate = ((java.sql.Date) dateCommande).toLocalDate();
                    } else {
                        commandeLocalDate = dateCommande.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    }
                    matchesDate = commandeLocalDate.equals(filterDate);
                } else {
                    matchesDate = false;
                }
            }

            return matchesDate;
        });

        // Mettre à jour les statistiques en fonction des filtres appliqués
        updateFilteredStatistics();
    }

    private void updateFilteredStatistics() {
        // Mise à jour des statistiques en fonction des commandes filtrées
        int filteredCount = commandesArchiveesFiltered.size();
        lblTotalArchives.setText(String.valueOf(filteredCount));

        double totalValue = commandesArchiveesFiltered.stream()
                .mapToDouble(cmd -> cmd.getCommande().getTotale())
                .sum();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        lblValeurTotale.setText(currencyFormat.format(totalValue));

        // Mettre à jour le délai moyen si nécessaire
        if (filteredCount > 0) {
            double delaiMoyen = 0;
            int commandesAvecDates = 0;

            for (CommandeDetails cmd : commandesArchiveesFiltered) {
                if (cmd.getLivraison() != null && cmd.getLivraison().getDateLiv() != null && cmd.getCommande().getDate_commande() != null) {
                    LocalDate dateCommande;
                    if (cmd.getCommande().getDate_commande() instanceof java.sql.Date) {
                        dateCommande = ((java.sql.Date) cmd.getCommande().getDate_commande()).toLocalDate();
                    } else {
                        dateCommande = cmd.getCommande().getDate_commande().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    }

                    LocalDate dateLivraison;
                    if (cmd.getLivraison().getDateLiv() instanceof java.sql.Date) {
                        dateLivraison = ((java.sql.Date) cmd.getLivraison().getDateLiv()).toLocalDate();
                    } else {
                        dateLivraison = cmd.getLivraison().getDateLiv().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    }

                    long delai = ChronoUnit.DAYS.between(dateCommande, dateLivraison);
                    if (delai >= 0) {
                        delaiMoyen += delai;
                        commandesAvecDates++;
                    }
                }
            }

            if (commandesAvecDates > 0) {
                delaiMoyen = delaiMoyen / commandesAvecDates;
                lblDelaiMoyen.setText(String.format("%.1f jours", delaiMoyen));
            } else {
                lblDelaiMoyen.setText("N/A");
            }
        } else {
            lblDelaiMoyen.setText("N/A");
        }
    }

    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null;
        }

        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        } else {
            return date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
    }

    private void showCommandeDetails(CommandeDetails commande) {
        try {
            // Créer une fenêtre simple avec les informations de base
            VBox root = new VBox(10);
            root.setPadding(new Insets(20));

            Label lblTitle = new Label("Détails de la Commande #" + commande.getCommande().getId());
            lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            Label lblCommande = new Label("Date commande: " +
                    (commande.getCommande().getDate_commande() != null ?
                            dateFormat.format(commande.getCommande().getDate_commande()) : "N/A"));

            Label lblTotal = new Label("Total: " + String.format("%.2f DT", commande.getCommande().getTotale()));

            Label lblLivraison = new Label("Livraison à: " +
                    (commande.getLivraison() != null ? commande.getLivraison().getAdresse() : "N/A"));

            Label lblPaiement = new Label("Méthode de paiement: " +
                    (commande.getPaiement() != null ? commande.getPaiement().getMethodePaiement() : "N/A"));

            Button btnClose = new Button("Fermer");
            btnClose.setOnAction(e -> ((Stage) root.getScene().getWindow()).close());

            root.getChildren().addAll(lblTitle, lblCommande, lblTotal, lblLivraison, lblPaiement, btnClose);

            Stage stage = new Stage();
            stage.setTitle("Détails de la Commande #" + commande.getCommande().getId());
            stage.setScene(new Scene(root, 400, 300));
            stage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'afficher les détails");
            alert.setContentText("Une erreur est survenue: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    public void retourAuxCommandes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/GestionCommandeBack/CommandeBackOffice.fxml"));
            Parent root = loader.load();

            Scene scene = tableArchives.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);

            stage.setTitle("Gestion des Commandes");
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de revenir à la gestion des commandes");
            alert.setContentText("Une erreur est survenue: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    // Ajoutez ces déclarations à la liste des variables FXML
    @FXML
    private Label lblLastUpdate;

    // Ajoutez cette méthode dans la classe ArchivesController
    @FXML
    public void effacerFiltres() {
        txtRechercheArchive.clear();
        datePicker.setValue(null);
        applyFilters("", null);

        // Mettre à jour le label de dernière mise à jour
        SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        lblLastUpdate.setText(timeFormat.format(new Date()));
    }


    // Modifiez la méthode rafraichirDonnees() pour mettre à jour la date de dernière mise à jour
    public void rafraichirDonnees() {
        loadArchivedData();
        setupStatistics();
        txtRechercheArchive.clear();
        datePicker.setValue(null);

        // Mettre à jour le label de dernière mise à jour
        SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        lblLastUpdate.setText(timeFormat.format(new Date()));
    }



}