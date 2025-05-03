package services;

import interfaces.IService;
import Models.Contrat;
import Models.ParcelleProprietes;
import org.example.utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContratService implements IService<Contrat> {
    Connection cnx = MaConnexion.getInstance().getConn();

    // Instance du service ParcelleProprietes pour la jointure
    private final ParcelleProprietesService parcelleService = new ParcelleProprietesService();

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
            // La colonne 'status' n'existe pas dans la base de données

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
            // D'abord, mettre à jour les entrées d'historique_location pour supprimer la référence au contrat
            PreparedStatement updateHistorique = cnx.prepareStatement(
                    "UPDATE historique_location SET contrat_id = NULL WHERE contrat_id = ?"
            );
            updateHistorique.setInt(1, contrat.getId());
            updateHistorique.executeUpdate();
            updateHistorique.close();

            // Ensuite, supprimer le contrat
            PreparedStatement deleteContrat = cnx.prepareStatement("DELETE FROM contrat WHERE id = ?");
            deleteContrat.setInt(1, contrat.getId());
            int rowsDeleted = deleteContrat.executeUpdate();
            deleteContrat.close();

            if (rowsDeleted > 0) {
                System.out.println("Contrat supprimé avec succès !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur suppression : " + e.getMessage());
        }
    }

    @Override
    public List<Contrat> getAll() {
        List<Contrat> contrats = new ArrayList<>();
        String SQL = "SELECT * FROM contrat";

        try (Statement st = cnx.createStatement();
             ResultSet res = st.executeQuery(SQL)) {

            while (res.next()) {
                Contrat c = extractContratFromResultSet(res);
                contrats.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return contrats;
    }

    // Nouvelle méthode pour obtenir les contrats avec les informations de parcelle
    public List<Contrat> getAllWithParcelle() {
        List<Contrat> contrats = new ArrayList<>();
        String SQL = "SELECT c.*, p.id as parcelle_id_info, p.titre, p.description, p.prix, p.status as parcelle_status, " +
                "p.emplacement, p.taille, p.date_creation_annonce, p.date_misajour_annonce, p.est_disponible, " +
                "p.nom_proprietaire, p.contact_proprietaire, p.image, p.user_id_parcelle_id, p.type_terrain, " +
                "p.latitude, p.longitude, p.email " +
                "FROM contrat c " +
                "LEFT JOIN parcelle_proprietes p ON c.parcelle_id = p.id";

        try (Statement st = cnx.createStatement();
             ResultSet res = st.executeQuery(SQL)) {

            while (res.next()) {
                Contrat c = extractContratFromResultSet(res);

                // Ajouter les informations de la parcelle si elles existent
                if (res.getObject("parcelle_id_info") != null) {
                    ParcelleProprietes parcelle = extractParcelleProprieteFromResultSet(res);
                    c.setParcelleProprietes(parcelle);
                }

                contrats.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return contrats;
    }

    // Nouvelle méthode pour obtenir un contrat par ID avec les informations de parcelle
    public Contrat getByIdWithParcelle(int id) {
        String SQL = "SELECT c.*, p.id as parcelle_id_info, p.titre, p.description, p.prix, p.status as parcelle_status, " +
                "p.emplacement, p.taille, p.date_creation_annonce, p.date_misajour_annonce, p.est_disponible, " +
                "p.nom_proprietaire, p.contact_proprietaire, p.image, p.user_id_parcelle_id, p.type_terrain, " +
                "p.latitude, p.longitude, p.email " +
                "FROM contrat c " +
                "LEFT JOIN parcelle_proprietes p ON c.parcelle_id = p.id " +
                "WHERE c.id = ?";

        try (PreparedStatement st = cnx.prepareStatement(SQL)) {
            st.setInt(1, id);

            try (ResultSet res = st.executeQuery()) {
                if (res.next()) {
                    Contrat c = extractContratFromResultSet(res);

                    // Ajouter les informations de la parcelle si elles existent
                    if (res.getObject("parcelle_id_info") != null) {
                        ParcelleProprietes parcelle = extractParcelleProprieteFromResultSet(res);
                        c.setParcelleProprietes(parcelle);
                    }

                    return c;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Méthode pour obtenir des contrats par ID de parcelle
    public List<Contrat> getByParcelleId(int parcelleId) {
        List<Contrat> contrats = new ArrayList<>();
        String SQL = "SELECT * FROM contrat WHERE parcelle_id = ?";

        try (PreparedStatement st = cnx.prepareStatement(SQL)) {
            st.setInt(1, parcelleId);

            try (ResultSet res = st.executeQuery()) {
                while (res.next()) {
                    Contrat c = extractContratFromResultSet(res);
                    contrats.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return contrats;
    }

    // Méthode utilitaire pour extraire un objet Contrat d'un ResultSet
    private Contrat extractContratFromResultSet(ResultSet res) throws SQLException {
        Contrat c = new Contrat();
        c.setId(res.getInt("id"));
        c.setParcelle_id(res.getObject("parcelle_id") != null ? res.getInt("parcelle_id") : null);
        c.setDate_debut_contrat(res.getDate("date_debut_contrat"));
        c.setDatefin_contrat(res.getDate("datefin_contrat"));
        c.setNom_acheteur(res.getString("nom_acheteur"));
        c.setNom_vendeur(res.getString("nom_vendeur"));
        c.setInformation_contrat(res.getString("information_contrat"));
        c.setDatecreation_contrat(res.getTimestamp("datecreation_contrat"));
        c.setUser_id_contrat_id(res.getObject("user_id_contrat_id") != null ? res.getInt("user_id_contrat_id") : null);
        c.setSignature_id(res.getString("signature_id"));
        c.setDocument_id(res.getString("document_id"));
        c.setSigner_id(res.getString("signer_id"));

        // Récupérer le statut s'il existe dans la table
        try {
            c.setStatus(res.getString("status"));
        } catch (SQLException e) {
            // Ignorer si la colonne n'existe pas
        }

        return c;
    }

    // Méthode utilitaire pour extraire un objet ParcelleProprietes d'un ResultSet
    private ParcelleProprietes extractParcelleProprieteFromResultSet(ResultSet res) throws SQLException {
        ParcelleProprietes p = new ParcelleProprietes();

        p.setId(res.getInt("parcelle_id_info"));
        p.setTitre(res.getString("titre"));
        p.setDescription(res.getString("description"));
        p.setPrix(res.getDouble("prix"));
        p.setStatus(res.getString("parcelle_status"));
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

        return p;
    }
}