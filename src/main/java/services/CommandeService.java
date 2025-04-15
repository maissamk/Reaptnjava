package services;

import interfaces.IService;
import models.Commande;
import utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeService implements IService<Commande> {
    Connection cnx = MaConnexion.getInstance().getConn();

    @Override
    public void add(Commande commande) {
        String SQL = "INSERT INTO commande(quantite, date_commande, totale) VALUES (?, ?, ?)";
        try {
            PreparedStatement st = cnx.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, commande.getQuantite());
            st.setDate(2, new java.sql.Date(commande.getDate_commande().getTime()));
            st.setFloat(3, commande.getTotale());

            int rows = st.executeUpdate();

            if (rows > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    commande.setId(generatedId);
                    System.out.println("Commande ajoutée avec ID : " + generatedId);
                }
            } else {
                System.out.println("Échec de l'ajout de la commande.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Commande commande) {
        String SQL = "UPDATE commande SET quantite=?, date_commande=?, totale=? WHERE id=?";
        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, commande.getQuantite());
            st.setDate(2, new java.sql.Date(commande.getDate_commande().getTime()));
            st.setFloat(3, commande.getTotale());
            st.setInt(4, commande.getId());
            st.executeUpdate();
            System.out.println("Commande modifiée avec succès");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Commande commande) {
        String SQL = "DELETE FROM commande WHERE id=?";
        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, commande.getId());
            st.executeUpdate();
            System.out.println("Commande supprimée avec succès");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Commande> getAll() {
        String SQL = "SELECT * FROM commande";
        List<Commande> commandes = new ArrayList<>();

        try {
            Statement st = cnx.createStatement();
            ResultSet res = st.executeQuery(SQL);

            while (res.next()) {
                Commande c = new Commande();
                c.setId(res.getInt("id"));
                c.setQuantite(res.getInt("quantite"));
                c.setDate_commande(res.getDate("date_commande"));
                c.setTotale(res.getFloat("totale"));
                commandes.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return commandes;
    }

    @Override
    public Commande getById(int id) {
        String SQL = "SELECT * FROM commande WHERE id = ?";
        Commande commande = null;

        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, id);
            ResultSet res = st.executeQuery();

            if (res.next()) {
                commande = new Commande();
                commande.setId(res.getInt("id"));
                commande.setQuantite(res.getInt("quantite"));
                commande.setDate_commande(res.getDate("date_commande"));
                commande.setTotale(res.getFloat("totale"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return commande;
    }
}