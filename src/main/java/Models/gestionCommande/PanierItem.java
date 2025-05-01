package Models.gestionCommande;

import Models.MaterielVente;

public class PanierItem {
    private MaterielVente produit;
    private int quantite;

    public PanierItem(MaterielVente produit, int quantite) {
        this.produit = produit;
        this.quantite = quantite;
    }

    public MaterielVente getProduit() {
        return produit;
    }
    public int getQuantite() {
        return quantite;
    }

    public void setProduit(MaterielVente produit) {
        this.produit = produit;
    }

    public double getTotal() {
        return produit.getPrix() * quantite;
    }
}
