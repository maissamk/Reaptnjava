package models.gestionCommande;

import java.util.Date;

public class Commande {
    private int id, quantite;
private Date date_commande;
private float totale;

    public Commande() {
    }
    public Commande(int quantite, Date date_commande, float totale) {
        this.quantite=quantite;
        this.date_commande=date_commande;
        this.totale=totale;
    }

    public Commande(int id, int quantite, Date date_commande, float totale) {
        this.id = id;
        this.quantite=quantite;
        this.date_commande=date_commande;
        this.totale=totale;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", quantite=" + quantite +
                ", date_commande=" + date_commande +
                ", totale=" + totale +
                '}';
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getTotale() {
        return totale;
    }

    public void setTotale(float totale) {
        this.totale = totale;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public Date getDate_commande() {
        return date_commande;
    }

    public void setDate_commande(Date date_commande) {
        this.date_commande = date_commande;
    }
}
