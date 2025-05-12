package Models.gestionCommande;

import com.google.gson.annotations.SerializedName;

public class PlantSuggestion {
    @SerializedName("name")
    private String plantName;

    @SerializedName("probability")
    private double probability;

    @SerializedName("description")
    private String description;

    @SerializedName("url")
    private String wikiUrl;

    // Constructeurs
    public PlantSuggestion() { }

    public PlantSuggestion(String plantName, double probability, String description, String wikiUrl) {
        this.plantName = plantName;
        this.probability = probability;
        this.description = description;
        this.wikiUrl = wikiUrl;
    }

    // Getters & Setters
    public String getPlantName() { return plantName; }
    public void setPlantName(String plantName) { this.plantName = plantName; }

    public double getProbability() { return probability; }
    public void setProbability(double probability) { this.probability = probability; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWikiUrl() { return wikiUrl; }
    public void setWikiUrl(String wikiUrl) { this.wikiUrl = wikiUrl; }
}