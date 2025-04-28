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
import utils.LanguageManager;
import utils.SessionManager;
import java.io.IOException;

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

    // Checkboxes for days of availability


    // Buttons for applying and canceling application


    // Language switch buttons


    // VBox and HBox layouts
    @FXML private VBox mainVBox;
    @FXML private HBox buttonHBox;








    private String dispo;
    private EmployeService employeService = new EmployeService();




    private void loadEmployesForOffre(int offreId) {
        // Setting up columns for "ID Employé" and "Compétence"
        user_identifierColumn.setCellValueFactory(new PropertyValueFactory<>("user_identifier"));
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
                // Call the GeminiService to check if the employee is suitable
                boolean isSuggested = GeminiService.isSuitable(currentOffre.getComp(), emp.getComp());

                // Update the suggested field in the database
                try {
                    employeService.updateSuggestedField(emp.getId(), isSuggested);
                } catch (SQLException e) {
                    // Handle the exception (e.g., log it, show an error message, etc.)
                    e.printStackTrace();
                    // Optionally, you can provide a user-friendly message
                    System.out.println("Error updating the suggested field: " + e.getMessage());
                }

                // Return the value for the column (Yes/No)
                return new ReadOnlyStringWrapper(isSuggested ? "Yes" : "No");
            } catch (IOException e) {
                e.printStackTrace();
                return new ReadOnlyStringWrapper("No");
            }
        });

        // Set up the color-coding of the "Suggested" column based on the value
        suggestedColumn.setCellFactory(column -> {
            return new TableCell<Employe, String>() {
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
            };
        });

        // Set the items in the table
        ObservableList<Employe> employes = FXCollections.observableArrayList(
                employeService.getEmployesByOffreId(offreId)
        );

        // Populate the TableView with the employees
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

        // Buttons
        editButton.setText(LanguageManager.getString("edit"));
        deleteButton.setText(LanguageManager.getString("delete"));
        saveButton.setText(LanguageManager.getString("save"));
        cancelButton.setText(LanguageManager.getString("cancel"));

        // Table column names
        user_identifierColumn.setText(LanguageManager.getString("employeeId"));
        compColumn.setText(LanguageManager.getString("competence"));
        dispoColumn.setText(LanguageManager.getString("availability"));
        suggestedColumn.setText(LanguageManager.getString("suggested"));

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
