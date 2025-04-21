package Models.gestionCommande;

public class PanierItem {
    private ProduitTestN produit;
    private int quantite;

    public PanierItem(ProduitTestN produit, int quantite) {
        this.produit = produit;
        this.quantite = quantite;
    }

    public ProduitTestN getProduit() {
        return produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public float getTotal() {
        return produit.getPrix() * quantite;
    }
}
