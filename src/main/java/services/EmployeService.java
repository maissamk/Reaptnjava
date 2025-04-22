package services;

import models.Employe;

import models.Offre;
import utils.MaConnexion;
import interfaces.IService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeService implements IService<Employe> {
    Connection connection;
    public  EmployeService(){
        connection= MaConnexion.getInstance().getConnection();
    }
    @Override
    public void add(Employe employe) throws SQLException {
        String sql = "insert into employe (user_identifier,comp,offre_id,date_join) values( ?,?,?,?)";

        try {
            PreparedStatement st =connection.prepareStatement(sql);
            st.setInt(1,employe.getUser_identifier());

            st.setString(2,employe.getComp());
            st.setInt(3,employe.getOffre_id());

            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
            st.setTimestamp(4,currentTimestamp);

            st.executeUpdate();
            System.out.println("Employé ajouté avec succés");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


    }

    @Override
    public void update(Employe employe) throws SQLException {
        String sql = "UPDATE employe SET user_identifier = ?, comp = ?, offre_id = ?, conf = ?, suggested = ?, dispo = ? WHERE id = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, employe.getUser_identifier());
            st.setString(2, employe.getComp());
            st.setInt(3, employe.getOffre_id());
            st.setBoolean(4, employe.isConf());
            st.setBoolean(5, employe.isSuggested());
            st.setString(6, employe.getDispo());
            st.setInt(7, employe.getId());
            st.executeUpdate();
            System.out.println("Employe updated successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM employe WHERE id = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, id);
            st.executeUpdate();
            System.out.println("Employe deleted successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean deleteByUserAndOffre(int userId, int offreId) throws SQLException {
        String sql = "DELETE FROM employe WHERE user_identifier = ? AND offre_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, offreId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public List<Employe> select() throws SQLException {
        String sql = "SELECT * FROM employe"; // Select all columns
        List<Employe> employes = new ArrayList<>();

        try {
            Statement st = connection.createStatement();
            ResultSet res = st.executeQuery(sql);

            while (res.next()) {
                Employe employe = new Employe();
                employe.setId(res.getInt("id")); // Assuming 'id' is a column in your table
                employe.setOffre_id(res.getInt("offre_id")); // Assuming 'offre_id' is a column
                employe.setUser_identifier(res.getInt("user_identifier")); // Assuming 'user_identifier' is a column
                employe.setComp(res.getString("comp")); // Assuming 'comp' is a column
                employe.setDispo(res.getString("dispo")); // Assuming 'dispo' is a column
                employe.setConf(res.getBoolean("conf")); // Assuming 'conf' is a column
                employe.setSuggested(res.getBoolean("suggested")); // Assuming 'suggested' is a column
                java.sql.Timestamp timestamp = res.getTimestamp("date_join"); // Assuming 'date_join' is a column
                if (timestamp != null) {
                    employe.setDate_join(timestamp.toLocalDateTime()); // Convert to LocalDateTime
                } else {
                    employe.setDate_join(null); // Handle null case if necessary
                }

                employes.add(employe);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return employes;
    }

    public List<Employe> getEmployesByOffreId(int offreId) {
        List<Employe> list = new ArrayList<>();
        try {
            String query = "SELECT * FROM employe WHERE offre_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, offreId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Employe e = new Employe();
                e.setId(rs.getInt("id"));
                e.setUser_identifier(rs.getInt("user_identifier"));
                e.setComp(rs.getString("comp"));
                e.setOffre_id(rs.getInt("offre_id"));
                list.add(e);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

}
