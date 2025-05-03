package services;

import interfaces.IService;
import Models.Contrat;
import Models.HistoriqueLocation;
import org.example.utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoriqueLocationService implements IService<HistoriqueLocation> {

    Connection cnx = MaConnexion.getInstance().getConn();

    @Override
    public void add(HistoriqueLocation historiqueLocation) {
        String SQL = "INSERT INTO historique_location " +
                "(parcelle_id, date_debut, date_fin, nom_locataire, prix_location, statut, contrat_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setObject(1, historiqueLocation.getParcelle_id(), Types.INTEGER);
            st.setDate(2, new java.sql.Date(historiqueLocation.getDate_debut().getTime()));
            st.setDate(3, new java.sql.Date(historiqueLocation.getDate_fin().getTime()));
            st.setString(4, historiqueLocation.getNom_locataire());
            st.setDouble(5, historiqueLocation.getPrix_location());
            st.setString(6, historiqueLocation.getStatut());

            // Ajouter le contrat_id
            if (historiqueLocation.getContrat_id() != null) {
                st.setInt(7, historiqueLocation.getContrat_id());
            } else {
                st.setNull(7, Types.INTEGER);
            }

            st.executeUpdate();
            System.out.println("Historique de location ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de l'historique : " + e.getMessage());
        }
    }

    @Override
    public void update(HistoriqueLocation historiqueLocation) {
        String SQL = "UPDATE historique_location SET " +
                "parcelle_id = ?, date_debut = ?, date_fin = ?, " +
                "nom_locataire = ?, prix_location = ?, statut = ?, contrat_id = ? " +
                "WHERE id = ?";

        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setObject(1, historiqueLocation.getParcelle_id(), Types.INTEGER);
            st.setDate(2, new java.sql.Date(historiqueLocation.getDate_debut().getTime()));
            st.setDate(3, new java.sql.Date(historiqueLocation.getDate_fin().getTime()));
            st.setString(4, historiqueLocation.getNom_locataire());
            st.setDouble(5, historiqueLocation.getPrix_location());
            st.setString(6, historiqueLocation.getStatut());

            // Ajouter le contrat_id
            if (historiqueLocation.getContrat_id() != null) {
                st.setInt(7, historiqueLocation.getContrat_id());
            } else {
                st.setNull(7, Types.INTEGER);
            }

            st.setInt(8, historiqueLocation.getId());

            int rowsUpdated = st.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Historique mis à jour avec succès !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour de l'historique : " + e.getMessage());
        }
    }

    @Override
    public void delete(HistoriqueLocation historiqueLocation) {
        String SQL = "DELETE FROM historique_location WHERE id = ?";

        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, historiqueLocation.getId());
            st.executeUpdate();
            System.out.println("Historique supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de l'historique : " + e.getMessage());
        }
    }

    @Override
    public List<HistoriqueLocation> getAll() {
        List<HistoriqueLocation> historiques = new ArrayList<>();
        String SQL = "SELECT h.*, c.* FROM historique_location h " +
                "LEFT JOIN contrat c ON h.contrat_id = c.id";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(SQL);

            while (rs.next()) {
                HistoriqueLocation hl = new HistoriqueLocation();
                hl.setId(rs.getInt("h.id"));
                hl.setParcelle_id(rs.getObject("h.parcelle_id") != null ? rs.getInt("h.parcelle_id") : null);
                hl.setDate_debut(rs.getDate("h.date_debut"));
                hl.setDate_fin(rs.getDate("h.date_fin"));
                hl.setNom_locataire(rs.getString("h.nom_locataire"));
                hl.setPrix_location(rs.getDouble("h.prix_location"));
                hl.setStatut(rs.getString("h.statut"));

                // Récupérer le contrat_id s'il existe
                if (rs.getObject("h.contrat_id") != null) {
                    hl.setContrat_id(rs.getInt("h.contrat_id"));

                    // Si le contrat existe, le charger également
                    if (rs.getObject("c.id") != null) {
                        Contrat contrat = new Contrat();
                        contrat.setId(rs.getInt("c.id"));
                        contrat.setParcelle_id(rs.getObject("c.parcelle_id") != null ? rs.getInt("c.parcelle_id") : null);
                        contrat.setDate_debut_contrat(rs.getDate("c.date_debut_contrat"));
                        contrat.setDatefin_contrat(rs.getDate("c.datefin_contrat"));
                        contrat.setNom_acheteur(rs.getString("c.nom_acheteur"));
                        contrat.setNom_vendeur(rs.getString("c.nom_vendeur"));
                        contrat.setInformation_contrat(rs.getString("c.information_contrat"));
                        contrat.setDatecreation_contrat(rs.getDate("c.datecreation_contrat"));
                        // Définir un statut par défaut "louer"
                        contrat.setStatus("louer");

                        hl.setContrat(contrat);
                    }
                }

                historiques.add(hl);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des historiques : " + e.getMessage());
        }

        return historiques;
    }

    // Méthode spécifique pour récupérer l'historique d'une parcelle
    public List<HistoriqueLocation> getHistoriqueByParcelleId(int parcelleId) {
        List<HistoriqueLocation> historiques = new ArrayList<>();
        String SQL = "SELECT h.*, c.* FROM historique_location h " +
                "LEFT JOIN contrat c ON h.contrat_id = c.id " +
                "WHERE h.parcelle_id = ?";

        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, parcelleId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                HistoriqueLocation hl = new HistoriqueLocation();
                hl.setId(rs.getInt("h.id"));
                hl.setParcelle_id(rs.getInt("h.parcelle_id"));
                hl.setDate_debut(rs.getDate("h.date_debut"));
                hl.setDate_fin(rs.getDate("h.date_fin"));
                hl.setNom_locataire(rs.getString("h.nom_locataire"));
                hl.setPrix_location(rs.getDouble("h.prix_location"));
                hl.setStatut(rs.getString("h.statut"));

                // Récupérer le contrat_id s'il existe
                if (rs.getObject("h.contrat_id") != null) {
                    hl.setContrat_id(rs.getInt("h.contrat_id"));

                    // Si le contrat existe, le charger également
                    if (rs.getObject("c.id") != null) {
                        Contrat contrat = new Contrat();
                        contrat.setId(rs.getInt("c.id"));
                        contrat.setParcelle_id(rs.getObject("c.parcelle_id") != null ? rs.getInt("c.parcelle_id") : null);
                        contrat.setDate_debut_contrat(rs.getDate("c.date_debut_contrat"));
                        contrat.setDatefin_contrat(rs.getDate("c.datefin_contrat"));
                        contrat.setNom_acheteur(rs.getString("c.nom_acheteur"));
                        contrat.setNom_vendeur(rs.getString("c.nom_vendeur"));
                        contrat.setInformation_contrat(rs.getString("c.information_contrat"));
                        contrat.setDatecreation_contrat(rs.getDate("c.datecreation_contrat"));
                        // Définir un statut par défaut "louer"
                        contrat.setStatus("louer");

                        hl.setContrat(contrat);
                    }
                }

                historiques.add(hl);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de l'historique : " + e.getMessage());
        }

        return historiques;
    }

    // Méthode pour compter le nombre de locations par parcelle
    public int getLocationCountByParcelleId(int parcelleId) {
        String SQL = "SELECT COUNT(*) FROM historique_location WHERE parcelle_id = ?";
        int count = 0;

        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, parcelleId);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage des locations : " + e.getMessage());
        }

        return count;
    }

    // Méthode pour obtenir les historiques basés sur les contrats
    public List<HistoriqueLocation> getHistoriqueFromContrats(int parcelleId) {
        List<HistoriqueLocation> historiques = new ArrayList<>();
        // Modifié: ne plus rechercher c.status car il n'existe pas
        String SQL = "SELECT c.* FROM contrat c WHERE c.parcelle_id = ?";

        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, parcelleId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                // Créer un objet HistoriqueLocation à partir des données du contrat
                HistoriqueLocation hl = new HistoriqueLocation();
                hl.setParcelle_id(parcelleId);
                hl.setDate_debut(rs.getDate("date_debut_contrat"));
                hl.setDate_fin(rs.getDate("datefin_contrat"));
                hl.setNom_locataire(rs.getString("nom_acheteur"));

                // Déterminer le statut en fonction des dates du contrat
                // Utiliser java.util.Date pour la date courante
                java.util.Date now = new java.util.Date();
                Date dateDebut = rs.getDate("date_debut_contrat");
                Date dateFin = rs.getDate("datefin_contrat");

                String statut;
                if (dateDebut != null && dateFin != null) {
                    if (now.before(dateDebut)) {
                        statut = "Planifié";
                    } else if (now.after(dateFin)) {
                        statut = "Terminé";
                    } else {
                        statut = "En cours";
                    }
                } else {
                    statut = "Non défini";
                }
                hl.setStatut(statut);

                // Autres informations
                hl.setContrat_id(rs.getInt("id"));

                // Récupérer les informations financières du contrat si disponibles
                String infoContrat = rs.getString("information_contrat");
                double prix = 0.0;
                try {
                    // Essayer de parser un montant si disponible dans les informations du contrat
                    if (infoContrat != null && infoContrat.contains("prix:")) {
                        String prixStr = infoContrat.substring(infoContrat.indexOf("prix:") + 5);
                        prix = Double.parseDouble(prixStr.split(" ")[0]);
                    }
                } catch (Exception e) {
                    // En cas d'erreur, utiliser une valeur par défaut
                    prix = 0.0;
                }
                hl.setPrix_location(prix);

                historiques.add(hl);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des contrats : " + e.getMessage());
        }

        return historiques;
    }

    // Méthode pour synchroniser les contrats avec l'historique
    public void syncContratToHistorique(int parcelleId) {
        // 1. Obtenir tous les contrats pour cette parcelle
        // Modifié: ne plus rechercher avec la condition sur status car il n'existe pas
        String SQL_CONTRATS = "SELECT * FROM contrat WHERE parcelle_id = ? AND id NOT IN " +
                "(SELECT contrat_id FROM historique_location WHERE contrat_id IS NOT NULL)";

        try {
            PreparedStatement st = cnx.prepareStatement(SQL_CONTRATS);
            st.setInt(1, parcelleId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                // Pour chaque contrat non lié à un historique, créer un nouvel enregistrement
                HistoriqueLocation hl = new HistoriqueLocation();
                hl.setParcelle_id(parcelleId);
                hl.setDate_debut(rs.getDate("date_debut_contrat"));
                hl.setDate_fin(rs.getDate("datefin_contrat"));
                hl.setNom_locataire(rs.getString("nom_acheteur"));
                hl.setContrat_id(rs.getInt("id"));

                // Déterminer le statut en fonction des dates
                // Utiliser java.util.Date pour la date courante
                java.util.Date now = new java.util.Date();
                Date dateDebut = rs.getDate("date_debut_contrat");
                Date dateFin = rs.getDate("datefin_contrat");

                String statut;
                if (dateDebut != null && dateFin != null) {
                    if (now.before(dateDebut)) {
                        statut = "Planifié";
                    } else if (now.after(dateFin)) {
                        statut = "Terminé";
                    } else {
                        statut = "En cours";
                    }
                } else {
                    statut = "Non défini";
                }
                hl.setStatut(statut);

                // Extraire le prix s'il est disponible
                double prix = 0.0;
                String infoContrat = rs.getString("information_contrat");
                if (infoContrat != null && !infoContrat.isEmpty()) {
                    // Cette partie peut être améliorée en fonction du format des informations du contrat
                    try {
                        if (infoContrat.contains("prix:")) {
                            String prixStr = infoContrat.substring(infoContrat.indexOf("prix:") + 5);
                            prix = Double.parseDouble(prixStr.split(" ")[0]);
                        }
                    } catch (Exception e) {
                        System.out.println("Impossible d'extraire le prix: " + e.getMessage());
                    }
                }
                hl.setPrix_location(prix);

                // Ajouter le nouvel historique
                this.add(hl);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la synchronisation des contrats : " + e.getMessage());
        }
    }
}