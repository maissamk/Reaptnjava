package services;


import models.Categorie;
import interfaces.ICategorieService;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements ICategorieService {

    private Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void ajouter(Categorie categorie) {
        String req = "INSERT INTO categorie(nom, description) VALUES (?,?)";
        try (PreparedStatement pstmt = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, categorie.getNom());
            pstmt.setString(2, categorie.getDescription());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    categorie.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Categorie categorie) {
        String req = "UPDATE categorie SET nom=?, description=? WHERE id=?";
        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            pstmt.setString(1, categorie.getNom());
            pstmt.setString(2, categorie.getDescription());
            pstmt.setInt(3, categorie.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) {
        String req = "DELETE FROM categorie WHERE id=?";
        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Categorie> findAll() {
        List<Categorie> categories = new ArrayList<>();
        String req = "SELECT * FROM categorie";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                Categorie c = new Categorie();
                c.setId(res.getInt("id"));
                c.setNom(res.getString("nom"));
                c.setDescription(res.getString("description"));
                categories.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public Categorie findById(int id) {
        String req = "SELECT * FROM categorie WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();
            if (res.next()) {
                Categorie c = new Categorie();
                c.setId(res.getInt("id"));
                c.setNom(res.getString("nom"));
                c.setDescription(res.getString("description"));
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Categorie> searchByName(String name) {
        List<Categorie> categories = new ArrayList<>();
        String req = "SELECT * FROM categorie WHERE nom LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, "%" + name + "%");
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                Categorie c = new Categorie();
                c.setId(res.getInt("id"));
                c.setNom(res.getString("nom"));
                c.setDescription(res.getString("description"));
                categories.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
}