package Models;

import java.util.Date;

public class HistoriqueLocation {
    private int id;
    private Integer parcelle_id;
    private Date date_debut;
    private Date date_fin;
    private String nom_locataire;
    private double prix_location;
    private String statut; // "Terminé", "En cours", "Planifié"

    // Nouvelle relation avec Contrat
    private Integer contrat_id;
    private Contrat contrat;

    // Constructeur par défaut
    public HistoriqueLocation() {
    }

    // Constructeur avec paramètres
    public HistoriqueLocation(Integer parcelle_id, Date date_debut, Date date_fin,
                              String nom_locataire, double prix_location, String statut) {
        this.parcelle_id = parcelle_id;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.nom_locataire = nom_locataire;
        this.prix_location = prix_location;
        this.statut = statut;
    }

    // Constructeur complet avec ID
    public HistoriqueLocation(int id, Integer parcelle_id, Date date_debut, Date date_fin,
                              String nom_locataire, double prix_location, String statut) {
        this.id = id;
        this.parcelle_id = parcelle_id;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.nom_locataire = nom_locataire;
        this.prix_location = prix_location;
        this.statut = statut;
    }

    // Constructeur avec contrat_id
    public HistoriqueLocation(Integer parcelle_id, Date date_debut, Date date_fin,
                              String nom_locataire, double prix_location, String statut, Integer contrat_id) {
        this.parcelle_id = parcelle_id;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.nom_locataire = nom_locataire;
        this.prix_location = prix_location;
        this.statut = statut;
        this.contrat_id = contrat_id;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getParcelle_id() {
        return parcelle_id;
    }

    public void setParcelle_id(Integer parcelle_id) {
        this.parcelle_id = parcelle_id;
    }

    public Date getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(Date date_debut) {
        this.date_debut = date_debut;
    }

    public Date getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(Date date_fin) {
        this.date_fin = date_fin;
    }

    public String getNom_locataire() {
        return nom_locataire;
    }

    public void setNom_locataire(String nom_locataire) {
        this.nom_locataire = nom_locataire;
    }

    public double getPrix_location() {
        return prix_location;
    }

    public void setPrix_location(double prix_location) {
        this.prix_location = prix_location;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    // Nouveaux getters et setters pour la relation avec Contrat
    public Integer getContrat_id() {
        return contrat_id;
    }

    public void setContrat_id(Integer contrat_id) {
        this.contrat_id = contrat_id;
    }

    public Contrat getContrat() {
        return contrat;
    }

    public void setContrat(Contrat contrat) {
        this.contrat = contrat;
        if (contrat != null) {
            this.contrat_id = contrat.getId();
        }
    }

    @Override
    public String toString() {
        return "HistoriqueLocation{" +
                "id=" + id +
                ", parcelle_id=" + parcelle_id +
                ", date_debut=" + date_debut +
                ", date_fin=" + date_fin +
                ", nom_locataire='" + nom_locataire + '\'' +
                ", prix_location=" + prix_location +
                ", statut='" + statut + '\'' +
                ", contrat_id=" + contrat_id +
                '}';
    }
}