package services;

import interfaces.IService;
import models.Contrat;
import org.example.utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContratService implements IService<Contrat> {
    Connection cnx= MaConnexion.getInstance().getConn();



    @Override
    public void add(Contrat contrat) {
        String SQL = "INSERT INTO contrat (" +
                "parcelle_id, date_debut_contrat, datefin_contrat, nom_acheteur, nom_vendeur, " +
                "information_contrat, datecreation_contrat, user_id_contrat_id, " +
                "signature_id, document_id, signer_id" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setObject(1, contrat.getParcelle_id(), Types.INTEGER);
            st.setDate(2, new java.sql.Date(contrat.getDate_debut_contrat().getTime()));
            st.setDate(3, new java.sql.Date(contrat.getDatefin_contrat().getTime()));
            st.setString(4, contrat.getNom_acheteur());
            st.setString(5, contrat.getNom_vendeur());
            st.setString(6, contrat.getInformation_contrat());
            st.setDate(7, new java.sql.Date(contrat.getDatecreation_contrat().getTime()));
            st.setObject(8, contrat.getUser_id_contrat_id(), Types.INTEGER);
            st.setString(9, contrat.getSignature_id());
            st.setString(10, contrat.getDocument_id());
            st.setString(11, contrat.getSigner_id());

            st.executeUpdate();
            System.out.println("Contrat ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du contrat : " + e.getMessage());
        }
    }

    @Override
    public void update(Contrat contrat) {
        String SQL = "UPDATE contrat SET " +
                "parcelle_id = ?, " +
                "date_debut_contrat = ?, " +
                "datefin_contrat = ?, " +
                "nom_acheteur = ?, " +
                "nom_vendeur = ?, " +
                "information_contrat = ?, " +
                "signature_id = ? " +
                "WHERE id = ?";

        try (PreparedStatement st = cnx.prepareStatement(SQL)) {
            st.setObject(1, contrat.getParcelle_id(), Types.INTEGER);
            st.setDate(2, new java.sql.Date(contrat.getDate_debut_contrat().getTime()));
            st.setDate(3, new java.sql.Date(contrat.getDatefin_contrat().getTime()));
            st.setString(4, contrat.getNom_acheteur());
            st.setString(5, contrat.getNom_vendeur());
            st.setString(6, contrat.getInformation_contrat());
            st.setString(7, contrat.getSignature_id());
            st.setInt(8, contrat.getId());

            int rowsUpdated = st.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Contrat mis à jour avec succès !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @Override
    public void delete(Contrat contrat) {
        try {
            PreparedStatement st = cnx.prepareStatement("DELETE FROM contrat WHERE id=?");
            st.setInt(1, contrat.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur suppression : " + e.getMessage());
        }}

    @Override
    public List<Contrat> getAll() {
        List<Contrat> contrats = new ArrayList<>();
        String SQL = "SELECT * FROM contrat";

        try (Statement st = cnx.createStatement();
             ResultSet res = st.executeQuery(SQL)) {

            while (res.next()) {
                Contrat c = new Contrat();
                // Correction des noms de colonnes
                c.setId(res.getInt("id"));
                c.setParcelle_id(res.getObject("parcelle_id") != null ? res.getInt("parcelle_id") : null);
                c.setDate_debut_contrat(res.getDate("date_debut_contrat")); // Colonne corrigée
                c.setDatefin_contrat(res.getDate("datefin_contrat")); // Colonne corrigée
                c.setNom_acheteur(res.getString("nom_acheteur"));
                c.setNom_vendeur(res.getString("nom_vendeur"));
                c.setInformation_contrat(res.getString("information_contrat"));
                c.setDatecreation_contrat(res.getTimestamp("datecreation_contrat")); // Colonne corrigée
                c.setSignature_id(res.getString("signature_id")); // Colonne corrigée
                c.setDocument_id(res.getString("document_id"));
                c.setSigner_id(res.getString("signer_id"));

                contrats.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return contrats;
    }
}
