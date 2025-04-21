package Models.gestionCommande;

public class ProduitTestN {
    private String nom;
    private float prix;
    private String description;

    public ProduitTestN(String nom, float prix, String description) {
        this.nom = nom;
        this.prix = prix;
        this.description = description;
    }

    public String getNom() {
        return nom;
    }

    public float getPrix() {
        return prix;
    }

    public String getDescription() {
        return description;
    }
}
