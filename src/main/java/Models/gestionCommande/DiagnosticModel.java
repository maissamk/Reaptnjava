package Models.gestionCommande;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DiagnosticModel {
    @SerializedName("suggestions")
    private List<PlantSuggestion> suggestions;

    public DiagnosticModel() { }

    public DiagnosticModel(List<PlantSuggestion> suggestions) {
        this.suggestions = suggestions;
    }

    public List<PlantSuggestion> getSuggestions() { return suggestions; }
    public void setSuggestions(List<PlantSuggestion> suggestions) { this.suggestions = suggestions; }
}
