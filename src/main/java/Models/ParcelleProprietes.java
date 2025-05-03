package Models;

import java.sql.Timestamp;
import java.util.Date;

public class ParcelleProprietes {
    private int id;
    private String titre;
    private String description;
    private double prix;
    private String status;
    private String emplacement;
    private double taille;
    private Date date_creation_annonce;
    private Date date_misajour_annonce;
    private boolean est_disponible;
    private String nom_proprietaire;
    private String contact_proprietaire;
    private String image;
    private int user_id_parcelle_id;
    private String type_terrain;
    private String latitude;
    private String longitude;
    private String email;

    // Constructeur par défaut
    public ParcelleProprietes() {
    }

    public ParcelleProprietes(String text, String s, double v, String value, String value1, double v1, Timestamp timestamp, Timestamp timestamp1, String text1, String text2, String text3, String text4, boolean selected, String imagePath, String s1, String s2) {
    }

    @Override
    public String toString() {
        return "ParcelleProprietes{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", status='" + status + '\'' +
                ", emplacement='" + emplacement + '\'' +
                ", taille=" + taille +
                ", date_creation_annonce=" + date_creation_annonce +
                ", date_misajour_annonce=" + date_misajour_annonce +
                ", est_disponible=" + est_disponible +
                ", nom_proprietaire='" + nom_proprietaire + '\'' +
                ", contact_proprietaire='" + contact_proprietaire + '\'' +
                ", image='" + image + '\'' +
                ", user_id_parcelle_id=" + user_id_parcelle_id +
                ", type_terrain='" + type_terrain + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    // Constructeur avec tous les champs ( + id : affichage)
    public ParcelleProprietes(int id, String titre, String description, double prix, String status, String emplacement,
                              double taille, Date date_creation_annonce, Date date_misajour_annonce, boolean est_disponible,
                              String nom_proprietaire, String contact_proprietaire, String image, int user_id_parcelle_id,
                              String type_terrain, String latitude, String longitude, String email) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.status = status;
        this.emplacement = emplacement;
        this.taille = taille;
        this.date_creation_annonce = date_creation_annonce;
        this.date_misajour_annonce = date_misajour_annonce;
        this.est_disponible = est_disponible;
        this.nom_proprietaire = nom_proprietaire;
        this.contact_proprietaire = contact_proprietaire;
        this.image = image;
        this.user_id_parcelle_id = user_id_parcelle_id;
        this.type_terrain = type_terrain;
        this.latitude = latitude;
        this.longitude = longitude;
        this.email = email;
    }


    //constructeur sans id ( - id : ajout )
    public ParcelleProprietes(String titre, String description, double prix, String status, String emplacement,
                              double taille, Date date_creation_annonce, Date date_misajour_annonce, boolean est_disponible,
                              String nom_proprietaire, String contact_proprietaire, String image, int user_id_parcelle_id,
                              String type_terrain, String latitude, String longitude, String email) {

        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.status = status;
        this.emplacement = emplacement;
        this.taille = taille;
        this.date_creation_annonce = date_creation_annonce;
        this.date_misajour_annonce = date_misajour_annonce;
        this.est_disponible = est_disponible;
        this.nom_proprietaire = nom_proprietaire;
        this.contact_proprietaire = contact_proprietaire;
        this.image = image;
        this.user_id_parcelle_id = user_id_parcelle_id;
        this.type_terrain = type_terrain;
        this.latitude = latitude;
        this.longitude = longitude;
        this.email = email;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmplacement() {
        return emplacement;
    }

    public void setEmplacement(String emplacement) {
        this.emplacement = emplacement;
    }

    public double getTaille() {
        return taille;
    }

    public void setTaille(double taille) {
        this.taille = taille;
    }

    public Timestamp getDate_creation_annonce() {
        return new Timestamp(date_creation_annonce.getTime());
    }

    public void setDate_creation_annonce(Date date_creation_annonce) {
        this.date_creation_annonce = date_creation_annonce;
    }

    public Timestamp getDate_misajour_annonce() {
        return new Timestamp(date_misajour_annonce.getTime());
    }

    public void setDate_misajour_annonce(Date date_misajour_annonce) {
        this.date_misajour_annonce = date_misajour_annonce;
    }

    public boolean isEst_disponible() {
        return est_disponible;
    }

    public void setEst_disponible(boolean est_disponible) {
        this.est_disponible = est_disponible;
    }

    public String getNom_proprietaire() {
        return nom_proprietaire;
    }

    public void setNom_proprietaire(String nom_proprietaire) {
        this.nom_proprietaire = nom_proprietaire;
    }

    public String getContact_proprietaire() {
        return contact_proprietaire;
    }

    public void setContact_proprietaire(String contact_proprietaire) {
        this.contact_proprietaire = contact_proprietaire;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getUser_id_parcelle_id() {
        return user_id_parcelle_id;
    }

    public void setUser_id_parcelle_id(int user_id_parcelle_id) {
        this.user_id_parcelle_id = user_id_parcelle_id;
    }

    public String getType_terrain() {
        return type_terrain;
    }

    public void setType_terrain(String type_terrain) {
        this.type_terrain = type_terrain;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
