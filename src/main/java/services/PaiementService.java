package services;

import models.Paiement;
import utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

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
}
