package utils.gestionCommande;

import Models.MaterielVente;
import Models.gestionCommande.PanierItem;
import utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PanierSession {
    private static List<PanierItem> panier = new ArrayList<>();

    public static void ajouterProduit(MaterielVente produit, int quantite) {
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
    public static void supprimerProduit(MaterielVente produit) {
        panier.removeIf(item -> item.getProduit().equals(produit));
    }



    public static void retirerProduit(MaterielVente produit) {
        for (int i = 0; i < panier.size(); i++) {
            PanierItem item = panier.get(i);
            if (item.getProduit().getNom().equals(produit.getNom())) {
                int nouvelleQuantite = item.getQuantite() - 1;
                if (nouvelleQuantite <= 0) {
                    panier.remove(i);
                } else {
                    panier.set(i, new PanierItem(produit, nouvelleQuantite));
                }
                return;
            }
        }
    }


    public static MaterielVente getProduitParId(int id) {
        MaterielVente produit = null;
        try (Connection conn = MaConnexion.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM materiel_vente WHERE id = ?")) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                produit = new MaterielVente(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getDouble("prix"),
                        rs.getString("description"),
                        rs.getBoolean("disponibilite"),
                        rs.getString("image"),
                        rs.getInt("user_id_materielvente_id"),
                        rs.getInt("commande_id"),
                        rs.getInt("categorie_id"),
                        rs.getString("slug"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du produit : " + e.getMessage());
        }

        return produit;
    }


}
