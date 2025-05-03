package services;

import Models.Employe;
import utils.GeminiService;
import Models.Offre;
import utils.MaConnexion;
import interfaces.IService;

import java.util.Map;
import java.util.HashMap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;


public class EmployeService implements IService<Employe> {

    private Map<Integer, String> tempNomMap = new HashMap<>();
    private Map<Integer, String> tempPrenomMap = new HashMap<>();
    private Map<Integer, String> tempEmailMap = new HashMap<>();


    Connection connection;
    public  EmployeService(){
        connection= MaConnexion.getInstance().getConnection();
    }
    @Override
    public void add(Employe employe) throws SQLException {
        String sql = "insert into employe (user_identifier, comp, offre_id, date_join, dispo) values( ?, ?, ?, ?, ?)";

        try {
            PreparedStatement st = connection.prepareStatement(sql);

            // Setting user identifier
            st.setInt(1, employe.getUser_identifier());

            // Setting compétence
            st.setString(2, employe.getComp());

            // Setting offer ID
            st.setInt(3, employe.getOffre_id());

            // Setting current timestamp for date_join
            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
            st.setTimestamp(4, currentTimestamp);

            // Serialize the dispo field (array of days) into a PHP serialized string
            ArrayList<String> dispoList = employe.getDispo();
            String serializedDispo = toPhpSerializedArray(dispoList); // Serialize the list
            st.setString(5, serializedDispo); // Set the serialized dispo

            // Execute the update
            st.executeUpdate();
            System.out.println("Employé ajouté avec succès");
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

            // Convert dispo ArrayList to serialized PHP array format
            ArrayList<String> dispoList = employe.getDispo();
            String serializedDispo = toPhpSerializedArray(dispoList); // Use your function to serialize the list
            st.setString(6, serializedDispo);

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

    public List<Employe> select() throws SQLException {
        String sql = "SELECT * FROM employe"; // Select all columns
        List<Employe> employes = new ArrayList<>();

        try (Statement st = connection.createStatement(); ResultSet res = st.executeQuery(sql)) {

            while (res.next()) {
                Employe employe = new Employe();
                employe.setId(res.getInt("id"));
                employe.setOffre_id(res.getInt("offre_id"));
                employe.setUser_identifier(res.getInt("user_identifier"));
                employe.setComp(res.getString("comp"));

                // Deserialize dispo from PHP serialized format
                String serializedDispo = res.getString("dispo");
                ArrayList<String> dispoList = parseSerializedDays(serializedDispo); // Use your function to parse the serialized string
                employe.setDispo(dispoList);

                employe.setConf(res.getBoolean("conf"));
                employe.setSuggested(res.getBoolean("suggested"));

                // Get the Offre object based on the offer ID (if needed)
                OffreService offreService = new OffreService();
                Offre offre = offreService.getOffreById(employe.getOffre_id()); // Make sure to implement this method

                // Call GeminiService to determine if the employee is suitable for the offer
                try {
                    boolean isSuggestedByAI = GeminiService.isSuitable(offre.getComp(), employe.getComp());
                    employe.setSuggested(isSuggestedByAI); // Update suggested field based on AI logic
                } catch (IOException e) {
                    e.printStackTrace(); // Handle exception in case the API call fails
                }

                java.sql.Timestamp timestamp = res.getTimestamp("date_join");
                if (timestamp != null) {
                    employe.setDate_join(timestamp.toLocalDateTime());
                } else {
                    employe.setDate_join(null);
                }

                employes.add(employe);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while fetching employees", e);
        }

        return employes;
    }


    public List<Employe> getEmployesByOffreId(int offreId,
                                              Map<Integer, String> tempNomMap,
                                              Map<Integer, String> tempPrenomMap,
                                              Map<Integer, String> tempEmailMap) {
        List<Employe> list = new ArrayList<>();
        try {
            String query = "SELECT e.*, u.nom, u.prenom, u.email " +
                    "FROM employe e " +
                    "JOIN user u ON e.user_identifier = u.id " +
                    "WHERE e.offre_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, offreId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Employe e = new Employe();
                e.setId(rs.getInt("id"));
                e.setUser_identifier(rs.getInt("user_identifier"));
                e.setComp(rs.getString("comp"));
                e.setOffre_id(rs.getInt("offre_id"));

                String serializedDispo = rs.getString("dispo");
                ArrayList<String> dispoList = parseSerializedDays(serializedDispo);
                e.setDispo(dispoList);

                // Fill the maps passed from controller
                tempNomMap.put(e.getId(), rs.getString("nom"));
                tempPrenomMap.put(e.getId(), rs.getString("prenom"));
                tempEmailMap.put(e.getId(), rs.getString("email"));

                list.add(e);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public static ArrayList<String> parseSerializedDays(String serialized) {
        ArrayList<String> days = new ArrayList<>();

        // Check if serialized is null, return empty list if it is
        if (serialized == null || serialized.isEmpty()) {
            return days; // Return empty list if null or empty
        }

        // Regex to match values like s:6:"Monday"; or s:7:"Tuesday";
        Pattern pattern = Pattern.compile("s:\\d+:\"(.*?)\";");
        Matcher matcher = pattern.matcher(serialized);

        while (matcher.find()) {
            days.add(matcher.group(1)); // Add the extracted value
        }

        return days;
    }

    public static String toPhpSerializedArray(ArrayList<String> list) {
        StringBuilder serialized = new StringBuilder();
        serialized.append("a:").append(list.size()).append(":{");

        for (int i = 0; i < list.size(); i++) {
            String value = list.get(i);
            serialized.append("i:").append(i)
                    .append(";s:").append(value.length())
                    .append(":\"").append(value).append("\";");
        }

        serialized.append("}");
        return serialized.toString();
    }

    public void updateSuggestedField(int id, boolean suggested) throws SQLException {
        // Make sure the connection is available
        if (connection == null) {
            throw new SQLException("Database connection not established.");
        }

        // SQL query to update the 'suggested' field
        String sql = "UPDATE employe SET suggested = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, suggested); // Set the 'suggested' value (true or false)
            stmt.setInt(2, id); // Set the employee identifier

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Employee's suggested field updated successfully.");
            } else {
                System.out.println("No employee found with the given identifier.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error while updating the 'suggested' field.", e);
        }
    }


    public Map<Integer, Integer> getEmployeCountPerOffre() {
        Map<Integer, Integer> result = new HashMap<>();
        String query = "SELECT offre_id, COUNT(*) as count FROM employe GROUP BY offre_id";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int offreId = rs.getInt("offre_id");
                int count = rs.getInt("count");
                result.put(offreId, count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }


}
