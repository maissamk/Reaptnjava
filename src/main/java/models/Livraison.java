package models;

import java.util.Date;

public class Livraison {

    private int id;
    private String adresse;
    private Date dateLiv;
    private String status;
    private Commande commande;

    // Constructeur vide
    public Livraison() {
    }

    // Constructeur avec tous les champs sauf id
    public Livraison(String adresse, Date dateLiv, String status, Commande commande) {
        this.adresse = adresse;
        this.dateLiv = dateLiv;
        this.status = status;
        this.commande = commande;
    }

    // Constructeur complet
    public Livraison(int id, String adresse, Date dateLiv, String status, Commande commande) {
        this.id = id;
        this.adresse = adresse;
        this.dateLiv = dateLiv;
        this.status = status;
        this.commande = commande;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getAdresse() {
        return adresse;
    }

    public Date getDateLiv() {
        return dateLiv;
    }

    public String getStatus() {
        return status;
    }

    public Commande getCommande() {
        return commande;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setDateLiv(Date dateLiv) {
        this.dateLiv = dateLiv;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    @Override
    public String toString() {
        return "Livraison{" +
                "id=" + id +
                ", adresse='" + adresse + '\'' +
                ", dateLiv=" + dateLiv +
                ", status='" + status + '\'' +
                ", commande=" + (commande != null ? commande.getId() : "null") +
                '}';
    }
}
