package services.gestionCommande;

import models.gestionCommande.Commande;
import models.gestionCommande.Paiement;
import utils.MaConnexion;

import java.sql.*;

public class PaiementService {
    private Connection connection;

    public PaiementService() {
        connection = MaConnexion.getInstance().getConn();
    }

    public void ajouter(Paiement paiement) {
        String sql = "INSERT INTO paiement (date_paiement, methode_paiement, commande_id) VALUES (?, ?, ?)";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            // Conversion de java.util.Date vers Timestamp pour MySQL
            Timestamp datePaiement = new Timestamp(paiement.getDatePaiement().getTime());
            statement.setTimestamp(1, datePaiement);
            statement.setString(2, paiement.getMethodePaiement());
            statement.setInt(3, paiement.getCommande().getId());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println(" Paiement ajouté avec succès !");
            } else {
                System.out.println("Échec de l'ajout du paiement.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du paiement : " + e.getMessage());
        }
    }


    public Paiement findByCommandeId(int commandeId) {
        String sql = "SELECT * FROM paiement WHERE commande_id = ?";
        Paiement paiement = null;

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, commandeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                paiement = new Paiement();
                paiement.setId(rs.getInt("id"));
                paiement.setDatePaiement(rs.getTimestamp("date_paiement"));
                paiement.setMethodePaiement(rs.getString("methode_paiement"));

                Commande commande = new Commande();
                commande.setId(commandeId);
                paiement.setCommande(commande);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du paiement : " + e.getMessage());
        }

        return paiement;
    }

}
