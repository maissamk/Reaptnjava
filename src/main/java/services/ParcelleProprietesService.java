package services;

import interfaces.IService;
import models.ParcelleProprietes;
import org.example.utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParcelleProprietesService implements IService<ParcelleProprietes> {

    Connection cnx= MaConnexion.getInstance().getConn();


    @Override
    public void add(ParcelleProprietes p) {

        String SQL ="INSERT INTO parcelle_proprietes ("
                + "titre, description, prix, status, emplacement, taille, "
                + "date_creation_annonce, date_misajour_annonce, est_disponible, "
                + "nom_proprietaire, contact_proprietaire, image,  "
                + "type_terrain, latitude, longitude, email"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setString(1, p.getTitre());
            st.setString(2, p.getDescription());
            st.setDouble(3, p.getPrix());
            st.setString(4, p.getStatus());
            st.setString(5, p.getEmplacement());
            st.setDouble(6, p.getTaille());
            st.setTimestamp(7, p.getDate_creation_annonce());
            st.setTimestamp(8, p.getDate_misajour_annonce());
            st.setBoolean(9, p.isEst_disponible());
            st.setString(10, p.getNom_proprietaire());
            st.setString(11, p.getContact_proprietaire());
            st.setString(12, p.getImage()); // image ici
            //st.setInt(13, p.getUser_id_parcelle_id());
            st.setString(13, p.getType_terrain());
            st.setString(14, p.getLatitude());
            st.setString(15, p.getLongitude());
            st.setString(16, p.getEmail());

            st.executeUpdate();
            System.out.println("Parcelle insérée avec succès");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'insertion : " + e.getMessage());
        }


    }

    @Override
    public void update(ParcelleProprietes parcelle) {
        String sql = "UPDATE parcelle_proprietes SET titre = ?, prix = ?, status = ?, type_terrain = ?, taille = ?, emplacement = ?, nom_proprietaire = ?, contact_proprietaire = ?, email = ?, est_disponible = ?, date_misajour_annonce = ?, image = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {

            if (cnx == null || cnx.isClosed()) {
                System.out.println("Connexion fermée !");
                return;
            }

            stmt.setString(1, parcelle.getTitre());
            stmt.setDouble(2, parcelle.getPrix());
            stmt.setString(3, parcelle.getStatus());
            stmt.setString(4, parcelle.getType_terrain());
            stmt.setDouble(5, parcelle.getTaille());
            stmt.setString(6, parcelle.getEmplacement());
            stmt.setString(7, parcelle.getNom_proprietaire());
            stmt.setString(8, parcelle.getContact_proprietaire());
            stmt.setString(9, parcelle.getEmail());
            stmt.setBoolean(10, parcelle.isEst_disponible());
            stmt.setTimestamp(11, parcelle.getDate_misajour_annonce());
            stmt.setString(12, parcelle.getImage());
            stmt.setInt(13, parcelle.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Mise à jour réussie : " + affectedRows + " ligne(s) modifiée(s)");
            } else {
                System.out.println("Aucune parcelle trouvée avec cet ID.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }


    @Override
    public void delete(ParcelleProprietes parcelle) {
        String SQL = "DELETE FROM parcelle_proprietes WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(SQL)) {
            // Vérification critique de l'ID avant suppression
            if (parcelle.getId() <= 0) {
                System.err.println("ID invalide pour la suppression : " + parcelle.getId());
                return;
            }

            stmt.setInt(1, parcelle.getId());

            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✅ Suppression réussie - Parcelle ID " + parcelle.getId() + " supprimée");
            } else {
                System.out.println("⚠️ Aucune parcelle trouvée avec l'ID " + parcelle.getId());
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur critique pendant la suppression :");
            e.printStackTrace();

            // Gestion spécifique des contraintes d'intégrité référentielle
            if (e.getSQLState().startsWith("23")) {
                System.err.println("Impossible de supprimer : données liées existantes (clé étrangère)");
            }
        }
    }

    @Override
    public List<ParcelleProprietes> getAll() {
        List<ParcelleProprietes> parcelles = new ArrayList<>();
        String SQL = "SELECT * FROM parcelle_proprietes";

        try {
            Statement st = cnx.createStatement();
            ResultSet res = st.executeQuery(SQL);

            while (res.next()) {
                ParcelleProprietes p = new ParcelleProprietes();

                // Récupération de l'ID - CORRECTION ESSENTIELLE
                p.setId(res.getInt("id"));

                // Récupération des autres champs
                p.setTitre(res.getString("titre"));
                p.setDescription(res.getString("description"));
                p.setPrix(res.getDouble("prix"));
                p.setStatus(res.getString("status"));
                p.setEmplacement(res.getString("emplacement"));
                p.setTaille(res.getDouble("taille"));
                p.setDate_creation_annonce(res.getTimestamp("date_creation_annonce"));
                p.setDate_misajour_annonce(res.getTimestamp("date_misajour_annonce"));
                p.setEst_disponible(res.getBoolean("est_disponible"));
                p.setNom_proprietaire(res.getString("nom_proprietaire"));
                p.setContact_proprietaire(res.getString("contact_proprietaire"));
                p.setImage(res.getString("image"));
                p.setUser_id_parcelle_id(res.getInt("user_id_parcelle_id"));
                p.setType_terrain(res.getString("type_terrain"));
                p.setLatitude(res.getString("latitude"));
                p.setLongitude(res.getString("longitude"));
                p.setEmail(res.getString("email"));

                parcelles.add(p);
            }

            // Fermeture propre des ressources
            res.close();
            st.close();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des parcelles : " + e.getMessage());
        }

        return parcelles;
    }

    // Ajouter cette méthode de filtrage
    public List<ParcelleProprietes> filterByLocation(String location) {
        return getAll().stream()
                .filter(p -> p.getEmplacement().toLowerCase().contains(location.toLowerCase()))
                .collect(Collectors.toList());
    }

}
