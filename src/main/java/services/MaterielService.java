package services;

import Models.Categorie;
import Models.MaterielLocation;
import Models.MaterielVente;
import com.itextpdf.text.DocumentException;
import interfaces.IMaterielService;
import utils.EmailSender;
import utils.MyDataBase;
import utils.PDFGenerator;


import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class MaterielService implements IMaterielService {

    private Connection cnx = MyDataBase.getInstance().getCnx();







      public void louerMateriel(int materielId, int userId) throws SQLException {
        String req = "UPDATE materiellocation SET disponibilite = false, user_id_materiellocation_id = ? WHERE id = ? AND disponibilite = true";

        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, materielId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Material is not available for rent or doesn't exist");
            }
        }
    }

     public void acheterMateriel(int materielId, int userId) throws SQLException {
        String req = "UPDATE materielvente SET disponibilite = false, user_id_materielvente_id = ? WHERE id = ? AND disponibilite = true";

        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, materielId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Material is not available for purchase or doesn't exist");
            }
        }
    }


    public Categorie findByCategory(int id) {
        Categorie category = null;
        String req = "SELECT * FROM categorie WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();
            if (res.next()) {
                category = new Categorie();
                category.setId(res.getInt("id"));
                category.setNom(res.getString("nom"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return category;
    }


    public List<MaterielLocation> getMaterielsLocationLoues() {
        List<MaterielLocation> locations = new ArrayList<>();
        String req = "SELECT ml.*, u.nom as user_nom FROM materiellocation ml " +
                "LEFT JOIN user u ON ml.user_id_materiellocation_id = u.id " +
                "WHERE ml.user_id_materiellocation_id IS NOT NULL";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                MaterielLocation m = new MaterielLocation();
                m.setId(res.getInt("id"));
                m.setNom(res.getString("nom"));
                m.setPrix(res.getDouble("prix"));
                m.setDescription(res.getString("description"));
                m.setDisponibilite(res.getBoolean("disponibilite"));
                m.setImage(res.getString("image"));
                m.setUserIdMaterielLocationId(res.getObject("user_id_materiellocation_id", Integer.class));
                // Ajout du nom de l'utilisateur
                //  m.setUserNom(res.getString("user_nom"));
                locations.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return locations;
    }

    public List<MaterielVente> getMaterielsVenteAchetes() {
        List<MaterielVente> ventes = new ArrayList<>();
        String req = "SELECT mv.*, u.nom as user_nom FROM materielvente mv " +
                "LEFT JOIN user u ON mv.user_id_materielvente_id = u.id " +
                "WHERE mv.user_id_materielvente_id IS NOT NULL";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                MaterielVente m = new MaterielVente();
                m.setId(res.getInt("id"));
                m.setNom(res.getString("nom"));
                m.setPrix(res.getDouble("prix"));
                m.setDescription(res.getString("description"));
                m.setDisponibilite(res.getBoolean("disponibilite"));
                m.setImage(res.getString("image"));
                m.setUserIdMaterielVenteId(res.getObject("user_id_materielvente_id", Integer.class));
                m.setCommandeId(res.getObject("commande_id", Integer.class));
                m.setCategorieId(res.getObject("categorie_id", Integer.class));
                m.setSlug(res.getString("slug"));
                m.setCreatedAt(res.getTimestamp("created_at").toLocalDateTime());
                m.setUpdatedAt(res.getTimestamp("updated_at").toLocalDateTime());
                // Ajout du nom de l'utilisateur
                ventes.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ventes;
    }







    @Override
    public void ajouterLocation(MaterielLocation materiel) {
        String req = "INSERT INTO materiellocation(nom, prix, description, disponibilite, image, user_id_materiellocation_id) VALUES (?,?,?,?,?,?)";

        try {
            try (PreparedStatement pstmt = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
                     pstmt.setString(1, materiel.getNom());
                    pstmt.setDouble(2, materiel.getPrix());
                    pstmt.setString(3, materiel.getDescription());
                    pstmt.setBoolean(4, materiel.isDisponibilite());
                    pstmt.setString(5, materiel.getImage() != null ? materiel.getImage() : "");
                    pstmt.setObject(6, materiel.getUserIdMaterielLocationId());
                    pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        materiel.setId(generatedKeys.getInt(1));
                    }
                }
            }

            // Send email notification with PDF
            File pdfFile = PDFGenerator.generateMaterialLocationPDF(materiel);
            String adminEmail = "maissakhemiri01@gmail.com"; // or get from config
            EmailSender.sendMaterialAddedEmail(adminEmail, "Location", pdfFile, materiel.getNom());

        } catch (SQLException | IOException | DocumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifierLocation(MaterielLocation materiel) {
        String req = "UPDATE materiellocation SET nom=?, prix=?, description=?, disponibilite=?, image=?, user_id_materiellocation_id=? WHERE id=?";
        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            pstmt.setString(1, materiel.getNom());
            pstmt.setDouble(2, materiel.getPrix());
            pstmt.setString(3, materiel.getDescription());
            pstmt.setBoolean(4, materiel.isDisponibilite());
            pstmt.setString(5, materiel.getImage());
            pstmt.setObject(6, materiel.getUserIdMaterielLocationId());
            pstmt.setInt(7, materiel.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<MaterielLocation> findAllLocation() {
        List<MaterielLocation> materiels = new ArrayList<>();
        String req = "SELECT * FROM materiellocation";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                MaterielLocation m = new MaterielLocation();
                m.setId(res.getInt("id"));
                m.setNom(res.getString("nom"));
                m.setPrix(res.getDouble("prix"));
                m.setDescription(res.getString("description"));
                m.setDisponibilite(res.getBoolean("disponibilite"));
                m.setImage(res.getString("image"));
                m.setUserIdMaterielLocationId(res.getObject("user_id_materiellocation_id", Integer.class));
                materiels.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materiels;
    }

    @Override
    public List<MaterielLocation> searchLocation(String query) {
        List<MaterielLocation> locations = new ArrayList<>();
        String req = "SELECT * FROM materiel_location WHERE nom LIKE ?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, "%" + query + "%");
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                MaterielLocation ml = new MaterielLocation(
                        res.getInt("id"),
                        res.getString("nom"),
                        res.getDouble("prix"),
                        res.getString("description"),
                        res.getBoolean("disponibilite"),
                        res.getString("image"),
                        res.getObject("user_id_materiellocation_id") != null ? res.getInt("user_id_materiellocation_id") : null
                );
                locations.add(ml);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return locations;
    }

    // Méthodes pour MaterielVente
    @Override
    public void ajouterVente(MaterielVente materiel) {
        String req = "INSERT INTO materielvente(nom, prix, description, disponibilite, image, user_id_materielvente_id, commande_id, categorie_id, slug, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try {
            try (PreparedStatement pstmt = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, materiel.getNom());
                pstmt.setDouble(2, materiel.getPrix());
                pstmt.setString(3, materiel.getDescription());
                pstmt.setBoolean(4, materiel.isDisponibilite());
                pstmt.setString(5, materiel.getImage() != null ? materiel.getImage() : "");
                pstmt.setObject(6, materiel.getUserIdMaterielVenteId());
                pstmt.setObject(7, materiel.getCommandeId());
                pstmt.setObject(8, materiel.getCategorieId());
                pstmt.setString(9, materiel.getSlug());
                if (materiel.getCreatedAt() == null) {
                    materiel.setCreatedAt(LocalDateTime.now());
                }
                if (materiel.getUpdatedAt() == null) {
                    materiel.setUpdatedAt(LocalDateTime.now());
                }
                pstmt.setTimestamp(10, Timestamp.valueOf(materiel.getCreatedAt()));
                pstmt.setTimestamp(11, Timestamp.valueOf(materiel.getUpdatedAt()));

                pstmt.executeUpdate();


                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        materiel.setId(generatedKeys.getInt(1));
                    }
                }
            }

            // Send email notification with PDF
            File pdfFile = PDFGenerator.generateMaterialVentePDF(materiel);
            String adminEmail = "maissakhemiri01@gmail.com"; // or get from config
            EmailSender.sendMaterialAddedEmail(adminEmail, "Vente", pdfFile, materiel.getNom());

        } catch (SQLException | IOException | DocumentException e) {
            System.out.println("❌ Erreur lors de l'ajout de la vente : " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void modifierVente(MaterielVente materiel) {
        String req = "UPDATE materielvente SET " +
                "nom = ?, " +
                "prix = ?, " +
                "description = ?, " +
                "disponibilite = ?, " +
                "image = ?, " +
                "user_id_materielvente_id = ?, " +
                "commande_id = ?, " +
                "categorie_id = ?, " +
                "slug = ?, " +
                "updated_at = ? " +
                "WHERE id = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            // Set current timestamp for updated_at
            LocalDateTime now = LocalDateTime.now();

            pstmt.setString(1, materiel.getNom());
            pstmt.setDouble(2, materiel.getPrix());
            pstmt.setString(3, materiel.getDescription());
            pstmt.setBoolean(4, materiel.isDisponibilite());
            pstmt.setString(5, materiel.getImage() != null ? materiel.getImage() : "");
            pstmt.setObject(6, materiel.getUserIdMaterielVenteId());
            pstmt.setObject(7, materiel.getCommandeId());
            pstmt.setObject(8, materiel.getCategorieId());
            pstmt.setString(9, materiel.getSlug());
            pstmt.setTimestamp(10, Timestamp.valueOf(now));
            pstmt.setInt(11, materiel.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("⚠️ No rows affected - material with ID " + materiel.getId() + " may not exist");
            } else {
                // Update the updatedAt field in the object if the update was successful
                materiel.setUpdatedAt(now);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error updating material sale: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update material sale", e);
        }
    }

    @Override
    public List<MaterielVente> findAllVente() {
        List<MaterielVente> materiels = new ArrayList<>();
        String req = "SELECT * FROM materielvente";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                MaterielVente m = new MaterielVente();
                m.setId(res.getInt("id"));
                m.setNom(res.getString("nom"));
                m.setPrix(res.getDouble("prix"));
                m.setDescription(res.getString("description"));
                m.setDisponibilite(res.getBoolean("disponibilite"));
                m.setImage(res.getString("image"));
                m.setUserIdMaterielVenteId(res.getObject("user_id_materielvente_id", Integer.class));
                m.setCommandeId(res.getObject("commande_id", Integer.class));
                m.setCategorieId(res.getObject("categorie_id", Integer.class));
                m.setSlug(res.getString("slug"));
                m.setCreatedAt(res.getTimestamp("created_at").toLocalDateTime());
                m.setUpdatedAt(res.getTimestamp("updated_at").toLocalDateTime());
                materiels.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materiels;
    }

    @Override
    public List<MaterielVente> searchVente(String query) {
        List<MaterielVente> ventes = new ArrayList<>();
        String req = "SELECT * FROM materiel_vente WHERE nom LIKE ?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, "%" + query + "%");
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                MaterielVente mv = new MaterielVente(
                        res.getInt("id"),
                        res.getString("nom"),
                        res.getDouble("prix"),
                        res.getString("description"),
                        res.getBoolean("disponibilite"),
                        res.getString("image"),
                        res.getObject("user_id_materielvente_id") != null ? res.getInt("user_id_materielvente_id") : null,
                        res.getObject("commande_id") != null ? res.getInt("commande_id") : null,
                        res.getObject("categorie_id") != null ? res.getInt("categorie_id") : null,
                        res.getString("slug"),
                        res.getTimestamp("created_at").toLocalDateTime(),
                        res.getTimestamp("updated_at").toLocalDateTime()
                );
                ventes.add(mv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ventes;
    }

    @Override
    public List<MaterielVente> findByCategorie(int categorieId) {
        List<MaterielVente> materiels = new ArrayList<>();
        String req = "SELECT * FROM materielvente WHERE categorie_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, categorieId);
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                MaterielVente m = new MaterielVente();
                m.setId(res.getInt("id"));
                m.setNom(res.getString("nom"));
                m.setPrix(res.getDouble("prix"));
                m.setDescription(res.getString("description"));
                m.setDisponibilite(res.getBoolean("disponibilite"));
                m.setImage(res.getString("image"));
                m.setUserIdMaterielVenteId(res.getObject("user_id_materielvente_id", Integer.class));
                m.setCommandeId(res.getObject("commande_id", Integer.class));
                m.setCategorieId(res.getObject("categorie_id", Integer.class));
                m.setSlug(res.getString("slug"));
                m.setCreatedAt(res.getTimestamp("created_at").toLocalDateTime());
                m.setUpdatedAt(res.getTimestamp("updated_at").toLocalDateTime());
                materiels.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materiels;
    }

    // Méthodes communes
    @Override
    public void supprimer(int id, String type) {
        String table = type.equals("location") ? "materiellocation" : "materielvente";
        String req = "DELETE FROM " + table + " WHERE id=?";
        try (PreparedStatement pstmt = cnx.prepareStatement(req)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}