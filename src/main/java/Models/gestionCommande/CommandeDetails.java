package Models.gestionCommande;

public class CommandeDetails {
    private Commande commande;
    private Livraison livraison;
    private Paiement paiement;

    public CommandeDetails(Commande commande, Livraison livraison, Paiement paiement) {
        this.commande = commande;
        this.livraison = livraison;
        this.paiement = paiement;
    }

    public Commande getCommande() {
        return commande;
    }

    public Livraison getLivraison() {
        return livraison;
    }

    public Paiement getPaiement() {
        return paiement;
    }
}
