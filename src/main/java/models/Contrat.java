package models;

import java.util.Date;

public class Contrat {
    private int id;
    private Integer parcelle_id;
    private Date date_debut_contrat;
    private Date datefin_contrat;
    private String nom_acheteur;
    private String nom_vendeur;
    private String information_contrat;
    private Date datecreation_contrat;
    private Integer user_id_contrat_id;
    private String signature_id;
    private String document_id;
    private String signer_id;

    // Constructeur par défaut
    public Contrat() {
    }

    // Constructeur complet
    public Contrat(int id, Integer parcelle_id, Date date_debut_contrat, Date datefin_contrat, String nom_acheteur,
                   String nom_vendeur, String information_contrat, Date datecreation_contrat, Integer user_id_contrat_id,
                   String signature_id, String document_id, String signer_id) {
        this.id = id;
        this.parcelle_id = parcelle_id;
        this.date_debut_contrat = date_debut_contrat;
        this.datefin_contrat = datefin_contrat;
        this.nom_acheteur = nom_acheteur;
        this.nom_vendeur = nom_vendeur;
        this.information_contrat = information_contrat;
        this.datecreation_contrat = datecreation_contrat;
        this.user_id_contrat_id = user_id_contrat_id;
        this.signature_id = signature_id;
        this.document_id = document_id;
        this.signer_id = signer_id;
    }

    // Constructeur sans id (pour l’ajout)
    public Contrat(Integer parcelle_id, Date date_debut_contrat, Date datefin_contrat, String nom_acheteur,
                   String nom_vendeur, String information_contrat, Date datecreation_contrat, Integer user_id_contrat_id,
                   String signature_id, String document_id, String signer_id) {
        this.parcelle_id = parcelle_id;
        this.date_debut_contrat = date_debut_contrat;
        this.datefin_contrat = datefin_contrat;
        this.nom_acheteur = nom_acheteur;
        this.nom_vendeur = nom_vendeur;
        this.information_contrat = information_contrat;
        this.datecreation_contrat = datecreation_contrat;
        this.user_id_contrat_id = user_id_contrat_id;
        this.signature_id = signature_id;
        this.document_id = document_id;
        this.signer_id = signer_id;
    }

    @Override
    public String toString() {
        return "Contrat{" +
                "id=" + id +
                ", parcelle_id=" + parcelle_id +
                ", date_debut_contrat=" + date_debut_contrat +
                ", datefin_contrat=" + datefin_contrat +
                ", nom_acheteur='" + nom_acheteur + '\'' +
                ", nom_vendeur='" + nom_vendeur + '\'' +
                ", information_contrat='" + information_contrat + '\'' +
                ", datecreation_contrat=" + datecreation_contrat +
                ", user_id_contrat_id=" + user_id_contrat_id +
                ", signature_id='" + signature_id + '\'' +
                ", document_id='" + document_id + '\'' +
                ", signer_id='" + signer_id + '\'' +
                '}';
    }

    public Integer getParcelle_id() {
        return parcelle_id;
    }

    public void setParcelle_id(Integer parcelle_id) {
        this.parcelle_id = parcelle_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate_debut_contrat() {
        return date_debut_contrat;
    }

    public void setDate_debut_contrat(Date date_debut_contrat) {
        this.date_debut_contrat = date_debut_contrat;
    }

    public Date getDatefin_contrat() {
        return datefin_contrat;
    }

    public void setDatefin_contrat(Date datefin_contrat) {
        this.datefin_contrat = datefin_contrat;
    }

    public String getNom_acheteur() {
        return nom_acheteur;
    }

    public void setNom_acheteur(String nom_acheteur) {
        this.nom_acheteur = nom_acheteur;
    }

    public String getNom_vendeur() {
        return nom_vendeur;
    }

    public void setNom_vendeur(String nom_vendeur) {
        this.nom_vendeur = nom_vendeur;
    }

    public String getInformation_contrat() {
        return information_contrat;
    }

    public void setInformation_contrat(String information_contrat) {
        this.information_contrat = information_contrat;
    }

    public Date getDatecreation_contrat() {
        return datecreation_contrat;
    }

    public void setDatecreation_contrat(Date datecreation_contrat) {
        this.datecreation_contrat = datecreation_contrat;
    }

    public Integer getUser_id_contrat_id() {
        return user_id_contrat_id;
    }

    public void setUser_id_contrat_id(Integer user_id_contrat_id) {
        this.user_id_contrat_id = user_id_contrat_id;
    }

    public String getSignature_id() {
        return signature_id;
    }

    public void setSignature_id(String signature_id) {
        this.signature_id = signature_id;
    }

    public String getDocument_id() {
        return document_id;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
    }

    public String getSigner_id() {
        return signer_id;
    }

    public void setSigner_id(String signer_id) {
        this.signer_id = signer_id;
    }

    //new

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
