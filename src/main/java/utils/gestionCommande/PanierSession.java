package utils.gestionCommande;

import models.gestionCommande.PanierItem;
import models.gestionCommande.ProduitTestN;

import java.util.ArrayList;
import java.util.List;

public class PanierSession {
    private static List<PanierItem> panier = new ArrayList<>();

    public static void ajouterProduit(ProduitTestN produit, int quantite) {
        for (PanierItem item : panier) {
            if (item.getProduit().getNom().equals(produit.getNom())) {
                // Incrémente la quantité existante
                int nouvelleQuantite = item.getQuantite() + quantite;
                panier.set(panier.indexOf(item), new PanierItem(produit, nouvelleQuantite));
                return;
            }
        }
        // Ajoute un nouvel item si le produit n'existe pas
        panier.add(new PanierItem(produit, quantite));
    }

    public static List<PanierItem> getPanier() {
        return panier;
    }

    public static void viderPanier() {
        panier.clear();
    }
    public static void retirerProduit(ProduitTestN produit) {
        for (PanierItem item : panier) {
            if (item.getProduit().getNom().equals(produit.getNom())) {
                int nouvelleQuantite = item.getQuantite() - 1;
                if (nouvelleQuantite <= 0) {
                    panier.remove(item);
                } else {
                    panier.set(panier.indexOf(item), new PanierItem(produit, nouvelleQuantite));
                }
                return;
            }
        }
    }

    public static void supprimerProduit(ProduitTestN produit) {
        panier.removeIf(item -> item.getProduit().getNom().equals(produit.getNom()));
    }

}
