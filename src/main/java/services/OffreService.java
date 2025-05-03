package services;


import models.Offre;
import models.Employe;
import utils.MaConnexion;
import interfaces.IService;

import java.sql.*;
import java.util.Map;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class OffreService implements IService<Offre> {
    Connection connection;
    public  OffreService(){
        connection= MaConnexion.getInstance().getConnection();
    }

    @Override
    public void add(Offre offre) throws SQLException {
        String sql = "insert into offre (statut,descr,titre,comp) values(?,?,?,?)";

        try {
            PreparedStatement st =connection.prepareStatement(sql);

            st.setBoolean(1,offre.isStatut());
            st.setString(2,offre.getDescr());
            st.setString(3,offre.getTitre());
            st.setString(4,offre.getComp());
            st.executeUpdate();
            System.out.println("Offre ajoutée avec succés");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


    }

    @Override
    public void update(Offre offre) throws SQLException {
        // Log the data to check if it is correct
        System.out.println("Executing update with id: " + offre.getId());
        System.out.println("New title: " + offre.getTitre());
        System.out.println("New description: " + offre.getDescr());
        System.out.println("New competence: " + offre.getComp());
        System.out.println("New statut: " + offre.isStatut());

        String sql = "UPDATE offre SET titre = ?, descr = ?, comp = ?, statut = ? WHERE id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, offre.getTitre());
        pstmt.setString(2, offre.getDescr());
        pstmt.setString(3, offre.getComp());
        pstmt.setBoolean(4, offre.isStatut());
        pstmt.setInt(5, offre.getId());

        // Execute the update and catch any potential exceptions
        try {
            pstmt.executeUpdate();
            System.out.println("Offer updated successfully.");
        } catch (SQLException e) {
            System.out.println("Error updating offer: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM offre WHERE id = ?";
        Connection conn = MaConnexion.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.executeUpdate();
        stmt.close(); // No conn.close()
    }

    @Override
    public List<Offre> select() throws SQLException {

        String sql="select * from offre";
        List<Offre> offres= new ArrayList<>();

        try {
            Statement st=connection.createStatement();
            ResultSet res= st.executeQuery(sql);

            while (res.next()) {
                Offre offre=new Offre();
                offre.setId(res.getInt("id"));
                offre.setStatut(res.getBoolean("statut"));
                offre.setDescr(res.getString("descr"));
                offre.setTitre(res.getString("titre"));
                offre.setComp(res.getString("comp"));
                offres.add(offre);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return offres;
    }

    public Offre getOffreById(int id) {
        String sql = "SELECT * FROM offre WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Offre offre = new Offre();
                offre.setId(rs.getInt("id")); // Ensure id is fetched correctly
                offre.setTitre(rs.getString("titre"));
                offre.setDescr(rs.getString("descr"));
                offre.setComp(rs.getString("comp"));
                offre.setStatut(rs.getBoolean("statut"));
                return offre;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no matching Offre is found
    }

    public void addBack(Offre offre) throws SQLException {
        String sql = "INSERT INTO offre (ida, ide, titre, descr, competence, statut) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, offre.getIda());
            stmt.setInt(2, offre.getIde());
            stmt.setString(3, offre.getTitre());
            stmt.setString(4, offre.getDescr());
            stmt.setString(5, offre.getComp());
            stmt.setBoolean(6, offre.isStatut());
            stmt.executeUpdate();
        }
    }

    public void updateBack(Offre offre) throws SQLException {
        String sql = "UPDATE offre SET ida = ?, ide = ?, titre = ?, descr = ?, competence = ?, statut = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, offre.getIda());
            stmt.setInt(2, offre.getIde());
            stmt.setString(3, offre.getTitre());
            stmt.setString(4, offre.getDescr());
            stmt.setString(5, offre.getComp());
            stmt.setBoolean(6, offre.isStatut());
            stmt.setInt(7, offre.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteBack(int id) throws SQLException {
        String sql = "DELETE FROM offre WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Offre> selectBack() throws SQLException {
        List<Offre> list = new ArrayList<>();
        String sql = "SELECT * FROM offre";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Offre offre = new Offre();
                offre.setId(rs.getInt("id"));
                offre.setIda(rs.getInt("ida"));
                offre.setIde(rs.getInt("ide"));
                offre.setTitre(rs.getString("titre"));
                offre.setDescr(rs.getString("descr"));
                offre.setComp(rs.getString("competence"));
                offre.setStatut(rs.getBoolean("statut"));

                list.add(offre);
            }
        }
        return list;
    }

    public Offre findById(int id) throws SQLException {
        String sql = "SELECT * FROM offre WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Offre offre = new Offre();
                offre.setId(rs.getInt("id"));
                offre.setIda(rs.getInt("ida"));
                offre.setIde(rs.getInt("ide"));
                offre.setTitre(rs.getString("titre"));
                offre.setDescr(rs.getString("descr"));
                offre.setComp(rs.getString("competence"));
                offre.setStatut(rs.getBoolean("statut"));
                return offre;
            }
        }
        return null;
    }

    public static void generateTxtForOffre(Offre offre, List<Employe> employes, Map<Integer, String> nomMap, Map<Integer, String> prenomMap, Map<Integer, String> emailMap) {
        // Define the txt file path
        String filePath = "offre_" + offre.getId() + "_details.txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Writing the offer details
            writer.println("=== Détails de l'Offre ===");
            writer.println("ID: " + offre.getId());
            writer.println("Titre: " + offre.getTitre());
            writer.println("Description: " + offre.getDescr());
            writer.println();  // Add a blank line

            // Writing employee details
            writer.println("=== Employés liés ===");
            for (Employe e : employes) {
                int id = e.getId();
                writer.println("Nom: " + nomMap.get(id));
                writer.println("Prénom: " + prenomMap.get(id));
                writer.println("Email: " + emailMap.get(id));
                writer.println("-----");  // Separator between employee records
            }

            // Optionally notify where the file is saved
            System.out.println("The details have been saved to: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
