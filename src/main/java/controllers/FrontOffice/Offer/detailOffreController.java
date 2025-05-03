package controllers.FrontOffice.Offer;
import controllers.FrontOffice.BaseFrontController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Offre;
import models.user;
import javafx.event.ActionEvent;
import services.OffreService;
import models.Employe;
import services.EmployeService;
import services.UserServices;
import utils.LanguageManager;
import utils.SessionManager;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import utils.GeminiService;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import javafx.beans.property.ReadOnlyStringWrapper;

import javafx.scene.Parent;

import java.sql.SQLException;
import utils.LanguageManager;
import java.util.ResourceBundle;
import java.net.URL;
import java.util.Locale;


public class detailOffreController {

    @FXML private Label titre;
    @FXML private Label titreLabel;

    @FXML private Label desc;
    @FXML private Label descLabel;

    @FXML private Label competence;
    @FXML private Label competenceLabel;

    @FXML private Label statut;
    @FXML private Label statutLabel;

    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private Offre currentOffre;

    @FXML
    private Button langButton;
    @FXML
    private Button englishButton;

    private Map<Integer, String> tempNomMap = new HashMap<>();
    private Map<Integer, String> tempPrenomMap = new HashMap<>();
    private Map<Integer, String> tempEmailMap = new HashMap<>();

    public void initialize() {



        // ⚡️ Add LanguageManager loading
        LanguageManager.selectedLanguage = "default";  // Default to English



        LanguageManager.loadLanguage();
        updateUIText(); // <- You already have this function to refresh your labels/buttons etc.



    }


    public void setOffre(Offre offre) {

        System.out.println("Selected offer ID: " + offre.getId()); // ⬅️ Add this

        if (offre != null) {
            currentOffre = offre; // ✅ This was missing!
            titreLabel.setText(offre.getTitre());
            descLabel.setText(offre.getDescr());
            competenceLabel.setText(offre.getComp());
            statutLabel.setText(offre.isStatut() ? "Validé" : "Non Validé");

            //THE EMPLOYEES TABLE
            loadEmployesForOffre(offre.getId());


        }
    }

    @FXML
    private void handleEditOffre(ActionEvent event) {
        try {

            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/Frontoffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();


            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/modifierOffre.fxml"));
            Parent content = contentLoader.load(); // content with its own controller & methods


            // Inject the page content into base layout
            baseController.getContentPane().getChildren().setAll(content);

            modifierOffreController modifierController = contentLoader.getController();
            modifierController.prefillForm(currentOffre);  // Set the current data

            // Now show the complete scene
            Scene scene = new Scene(baseRoot);
            Stage stage = (Stage) editButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteOffre(ActionEvent event) {
        try {
            OffreService service = new OffreService();
            service.delete(currentOffre.getId());

            FXMLLoader baseLoader = new FXMLLoader(getClass().getResource("/FrontOffice/baseFront.fxml"));
            Parent baseRoot = baseLoader.load();
            BaseFrontController baseController = baseLoader.getController();

            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/FrontOffice/Offre/indexOffre.fxml"));
            Parent content = contentLoader.load();

            baseController.getContentPane().getChildren().setAll(content);

            Scene scene = new Scene(baseRoot);
            Stage stage = (Stage) deleteButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //*******************************************************************************************************//
    //////////////////////////////////////PARTIE EMPLOYE///////////////////////////////////////////////////////





    @FXML private TableView<Employe> employeTable;
    @FXML private TableColumn<Employe, Integer> user_identifierColumn;





    @FXML private TableColumn<Employe, String> compColumn;

    @FXML private TableColumn<Employe, String> dispoColumn;

    @FXML
    private TextField userIdField;
    @FXML
    private TextField compField;

    @FXML
    private CheckBox mondayCheckBox;
    @FXML
    private CheckBox tuesdayCheckBox;
    @FXML
    private CheckBox wednesdayCheckBox;
    @FXML
    private CheckBox thursdayCheckBox;
    @FXML
    private CheckBox fridayCheckBox;
    @FXML
    private CheckBox saturdayCheckBox;
    @FXML
    private CheckBox sundayCheckBox;
    @FXML
    private TableColumn<Employe, String> suggestedColumn;

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label PostulerLabel;
    @FXML
    private Label DisponibiliteLabel;




    // Labels for offer details
    @FXML private Label detailsLabel;

    @FXML private Label titreValueLabel;

    @FXML private Label descValueLabel;

    @FXML private Label competenceValueLabel;

    @FXML private Label statutValueLabel;

    // Buttons for edit and delete actions


    // Table for displaying employees


    // Label and TextFields for the employe inscription form
    @FXML private Label postulerLabel;
    @FXML private Label userIdLabel;

    @FXML private Label compLabel;

    @FXML private Label dispoLabel;

    @FXML
    private TableColumn<Employe, String> prenomColumn;

    @FXML
    private TableColumn<Employe, String> nomColumn;

    @FXML
    private TableColumn<Employe, String> emailColumn;

    // Checkboxes for days of availability


    // Buttons for applying and canceling application


    // Language switch buttons


    // VBox and HBox layouts
    @FXML private VBox mainVBox;
    @FXML private HBox buttonHBox;










    private String dispo;
    private EmployeService employeService = new EmployeService();





    private void loadEmployesForOffre(int offreId) {
        // Setting up columns

        // For displaying Prenom
        prenomColumn.setCellValueFactory(cellData -> {
            Employe emp = cellData.getValue();
            String prenom = tempPrenomMap.get(emp.getId());  // Fetch prenom from the map
            return new ReadOnlyStringWrapper(prenom != null ? prenom : "Unknown");
        });

        // For displaying Nom
        nomColumn.setCellValueFactory(cellData -> {
            Employe emp = cellData.getValue();
            String nom = tempNomMap.get(emp.getId());  // Fetch nom from the map
            return new ReadOnlyStringWrapper(nom != null ? nom : "Unknown");
        });

        // For displaying Email
        emailColumn.setCellValueFactory(cellData -> {
            Employe emp = cellData.getValue();
            String email = tempEmailMap.get(emp.getId());  // Fetch email from the map
            return new ReadOnlyStringWrapper(email != null ? email : "Unknown");
        });

        // Set up the compColumn (competence)
        compColumn.setCellValueFactory(new PropertyValueFactory<>("comp"));

        // Set up the dispoColumn for the availability
        dispoColumn.setCellValueFactory(cellData -> {
            Employe emp = cellData.getValue();
            ArrayList<String> days = emp.getDispo();
            String formattedDays = (days != null && !days.isEmpty()) ? String.join(", ", days) : "No Availability";
            return new ReadOnlyStringWrapper(formattedDays);
        });

        // Set the CellValueFactory for the "Suggested" column
        suggestedColumn.setCellValueFactory(cellData -> {
            Employe emp = cellData.getValue();
            try {
                boolean isSuggested = GeminiService.isSuitable(currentOffre.getComp(), emp.getComp());

                // Update the suggested field in the database
                try {
                    employeService.updateSuggestedField(emp.getId(), isSuggested);
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Error updating the suggested field: " + e.getMessage());
                }

                return new ReadOnlyStringWrapper(isSuggested ? "Yes" : "No");
            } catch (IOException e) {
                e.printStackTrace();
                return new ReadOnlyStringWrapper("No");
            }
        });

        // Set up color-coding for the "Suggested" column
        suggestedColumn.setCellFactory(column -> new TableCell<Employe, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Yes")) {
                        setStyle("-fx-background-color: lightgreen;");
                    } else {
                        setStyle("-fx-background-color: lightcoral;");
                    }
                }
            }
        });

        // Set the items in the table
        ObservableList<Employe> employes = FXCollections.observableArrayList(
                employeService.getEmployesByOffreId(offreId, tempNomMap, tempPrenomMap, tempEmailMap)
        );

        employeTable.setItems(employes);
    }








    @FXML
    private void handleAddEmploye() {
        String competence = compField.getText().trim();

        // 1. Vérifier les champs vides
        if (competence.isEmpty()) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // 2. Get the current logged-in user's ID from SessionManager
        SessionManager sessionManager = SessionManager.getInstance(); // Get the singleton instance of SessionManager
        user currentUser = sessionManager.getCurrentUser(); // Get the logged-in user

        if (currentUser == null) {
            showAlert("Erreur", "Aucun utilisateur connecté.");
            return;
        }

        int userIdentifier = currentUser.getId(); // Get the logged-in user's ID

        // 3. Get the selected days from the checkboxes
        ArrayList<String> dispoList = new ArrayList<>();
        if (mondayCheckBox.isSelected()) dispoList.add("Monday");
        if (tuesdayCheckBox.isSelected()) dispoList.add("Tuesday");
        if (wednesdayCheckBox.isSelected()) dispoList.add("Wednesday");
        if (thursdayCheckBox.isSelected()) dispoList.add("Thursday");
        if (fridayCheckBox.isSelected()) dispoList.add("Friday");
        if (saturdayCheckBox.isSelected()) dispoList.add("Saturday");
        if (sundayCheckBox.isSelected()) dispoList.add("Sunday");

        // 4. Vérifier si l'utilisateur a sélectionné au moins un jour
        if (dispoList.isEmpty()) {
            showAlert("Champs manquants", "Veuillez sélectionner au moins un jour de disponibilité.");
            return;
        }

        try {
            if (currentOffre == null) {
                showAlert("Erreur", "Offre non définie. Impossible de postuler.");
                return;
            }

            Employe newEmploye = new Employe();
            newEmploye.setUser_identifier(userIdentifier); // Set the current logged-in user's ID
            newEmploye.setComp(competence);
            newEmploye.setOffre_id(currentOffre.getId()); // Safe because we checked it's not null
            newEmploye.setDispo(dispoList); // Set the selected availability days

            employeService.add(newEmploye); // This will set date_join inside

            showAlert("Succès", "Candidature envoyée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de l'envoi de la candidature.");
        }
    }





    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteEmploye() {
        String userIdText = userIdField.getText().trim();

        if (userIdText.isEmpty()) {
            showAlert("Champ manquant", "Veuillez entrer l'identifiant de l'utilisateur.");
            return;
        }

        int userIdentifier;
        try {
            userIdentifier = Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "L'identifiant utilisateur doit être un nombre entier.");
            return;
        }

        if (currentOffre == null) {
            showAlert("Erreur", "Offre non définie. Impossible de supprimer la candidature.");
            return;
        }

        try {
            boolean success = employeService.deleteByUserAndOffre(userIdentifier, currentOffre.getId());

            if (success) {
                showAlert("Succès", "Candidature supprimée avec succès !");
                loadEmployesForOffre(currentOffre.getId()); // Refresh the table
            } else {
                showAlert("Erreur", "Aucune candidature trouvée pour cet utilisateur.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de la suppression.");
        }
    }


    @FXML
    private void downloadDetails(ActionEvent event) {
        try {
            int offreId = currentOffre.getId();  // Use the current offer's ID
            OffreService offreService = new OffreService();
            EmployeService employeService = new EmployeService();

            // Fetch the offer by ID
            Offre offre = offreService.getOffreById(offreId);

            // Maps to hold employee details
            Map<Integer, String> nomMap = new HashMap<>();
            Map<Integer, String> prenomMap = new HashMap<>();
            Map<Integer, String> emailMap = new HashMap<>();

            // Get employees linked to the offer
            List<Employe> employes = employeService.getEmployesByOffreId(offreId, nomMap, prenomMap, emailMap);

            // Generate the .txt file
            File txtFile = new File("offre_" + offreId + "_details.txt");
            try (PrintWriter writer = new PrintWriter(txtFile)) {
                writer.println("=== Détails de l'Offre ===");
                writer.println("ID: " + offre.getId());
                writer.println("Titre: " + offre.getTitre());
                writer.println("Description: " + offre.getDescr());
                writer.println();
                writer.println("=== Employés liés ===");

                for (Employe e : employes) {
                    int id = e.getId();
                    writer.println("Nom: " + nomMap.get(id));
                    writer.println("Prénom: " + prenomMap.get(id));
                    writer.println("Email: " + emailMap.get(id));
                    writer.println("-----");
                }
            }

            // Call the service method to generate the PDF
            OffreService.generateTxtForOffre(offre, employes, nomMap, prenomMap, emailMap);

            // Optional: Show success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Réussi");
            alert.setHeaderText(null);
            alert.setContentText("Les détails ont été enregistrés dans " + txtFile.getAbsolutePath() + " et un PDF a été généré.");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Method to switch to Deutsch
    @FXML
    private void switchLang() {
        LanguageManager.selectedLanguage = "de";  // Set the language to Deutsch
        LanguageManager.loadLanguage();
        updateUIText();  // Update UI after changing language
    }

    @FXML
    private void switchToEnglish() {
        LanguageManager.selectedLanguage = "default";  // Set the language to English
        LanguageManager.loadLanguage();
        updateUIText();  // Update UI after changing language
    }

    private void updateUIText() {

        // Labels
        titre.setText(LanguageManager.getString("title"));
        desc.setText(LanguageManager.getString("description"));
        competence.setText(LanguageManager.getString("competence"));
        statut.setText(LanguageManager.getString("status"));
        PostulerLabel.setText(LanguageManager.getString("PostulerLabel"));
        DisponibiliteLabel.setText(LanguageManager.getString("DisponibiliteLabel"));

        // Buttons
        editButton.setText(LanguageManager.getString("edit"));
        deleteButton.setText(LanguageManager.getString("delete"));
        saveButton.setText(LanguageManager.getString("save"));
        cancelButton.setText(LanguageManager.getString("cancel"));

        // Table column names
        if (nomColumn != null) {
            nomColumn.setText(LanguageManager.getString("nom"));
        }
        if (prenomColumn != null) {
            prenomColumn.setText(LanguageManager.getString("prenom"));
        }
        if (emailColumn != null) {
            emailColumn.setText(LanguageManager.getString("email"));
        }
        if (compColumn != null) {
            compColumn.setText(LanguageManager.getString("competence"));
        }
        if (dispoColumn != null) {
            dispoColumn.setText(LanguageManager.getString("availability"));
        }
        if (suggestedColumn != null) {
            suggestedColumn.setText(LanguageManager.getString("suggested"));
        }

        // Checkboxes (Availability days)
        mondayCheckBox.setText(LanguageManager.getString("monday"));
        tuesdayCheckBox.setText(LanguageManager.getString("tuesday"));
        wednesdayCheckBox.setText(LanguageManager.getString("wednesday"));
        thursdayCheckBox.setText(LanguageManager.getString("thursday"));
        fridayCheckBox.setText(LanguageManager.getString("friday"));
        saturdayCheckBox.setText(LanguageManager.getString("saturday"));
        sundayCheckBox.setText(LanguageManager.getString("sunday"));

        // Buttons for switching language
        langButton.setText(LanguageManager.getString("german"));
        englishButton.setText(LanguageManager.getString("english"));
    }

}
