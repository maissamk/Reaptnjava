package services;

import models.Commande;
import models.Livraison;
import utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LivraisonService {
    private Connection cnx;

    public LivraisonService() {
        cnx = MaConnexion.getInstance().getConn();
    }

    public void ajouter(Livraison livraison) {
        String sql = "INSERT INTO livraison (adresse, date_liv, status, commande_id) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, livraison.getAdresse());
            ps.setDate(2, new java.sql.Date(livraison.getDateLiv().getTime()));
            ps.setString(3, livraison.getStatus());
            ps.setInt(4, livraison.getCommande().getId());

            ps.executeUpdate();
            System.out.println("Livraison enregistrée !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l’ajout de la livraison : " + e.getMessage());
        }
    }

    public Livraison findByCommandeId(int commandeId) {
        String sql = "SELECT * FROM livraison WHERE commande_id = ?";
        Livraison livraison = null;

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, commandeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                livraison = new Livraison();
                livraison.setId(rs.getInt("id"));
                livraison.setAdresse(rs.getString("adresse"));
                livraison.setDateLiv(rs.getDate("date_liv"));
                livraison.setStatus(rs.getString("status"));

                Commande commande = new Commande();
                commande.setId(commandeId);
                livraison.setCommande(commande);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de la livraison : " + e.getMessage());
        }

        return livraison;
    }

}
