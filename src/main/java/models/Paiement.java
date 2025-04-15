package models;

import java.util.Date;

public class Paiement {
    private int id;
    private Date datePaiement;
    private String methodePaiement;
    private Commande commande;

    // Constructeurs
    public Paiement() {}

    public Paiement(Date datePaiement, String methodePaiement, Commande commande) {
        this.datePaiement = datePaiement;
        this.methodePaiement = methodePaiement;
        this.commande = commande;
    }

    // Getters et Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(Date datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getMethodePaiement() {
        return methodePaiement;
    }

    public void setMethodePaiement(String methodePaiement) {
        this.methodePaiement = methodePaiement;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", datePaiement=" + datePaiement +
                ", methodePaiement='" + methodePaiement + '\'' +
                ", commande=" + (commande != null ? commande.getId() : "null") +
                '}';
    }
}

