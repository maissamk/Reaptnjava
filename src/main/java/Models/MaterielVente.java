package Models;

import java.time.LocalDateTime;

public class MaterielVente {
    private int id;
    private String nom;
    private double prix;
    private String description;
    private boolean disponibilite;
    private String image;
    private Integer user_id_materielvente_id;
    private Integer commande_id;
    private Integer categorie_id;
    private String slug;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

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

    public Integer getUserIdMaterielVenteId() {
        return user_id_materielvente_id;
    }

    public void setUserIdMaterielVenteId(Integer user_id_materielvente_id) {
        this.user_id_materielvente_id = user_id_materielvente_id;
    }

    public Integer getCommandeId() {
        return commande_id;
    }

    public void setCommandeId(Integer commande_id) {
        this.commande_id = commande_id;
    }

    public Integer getCategorieId() {
        return categorie_id;
    }

    public void setCategorieId(Integer categorie_id) {
        this.categorie_id = categorie_id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public LocalDateTime getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public MaterielVente() {

    }

    public MaterielVente(int id, String nom, double prix, String description, boolean disponibilite, String image, Integer user_id_materielvente_id, Integer commande_id, Integer categorie_id, String slug, LocalDateTime created_at, LocalDateTime updated_at) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.description = description;
        this.disponibilite = disponibilite;
        this.image = image;
        this.user_id_materielvente_id = user_id_materielvente_id;
        this.commande_id = commande_id;
        this.categorie_id = categorie_id;
        this.slug = slug;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
}