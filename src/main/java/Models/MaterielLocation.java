package Models;

public class MaterielLocation {
    private int id;
    private String nom;
    private double prix;
    private String description;
    private boolean disponibilite;
    private String image;
    private Integer user_id_materiellocation_id;

    public MaterielLocation(int id, String nom, double prix, String description, boolean disponibilite, String image, Integer user_id_materiellocation_id) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.description = description;
        this.disponibilite = disponibilite;
        this.image = image;
        this.user_id_materiellocation_id = user_id_materiellocation_id;
    }

    public MaterielLocation() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(boolean disponibilite) {
        this.disponibilite = disponibilite;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getUserIdMaterielLocationId() {
        return user_id_materiellocation_id;
    }

    public void setUserIdMaterielLocationId(Integer user_id_materiellocation_id) {
        this.user_id_materiellocation_id = user_id_materiellocation_id;
    }
}