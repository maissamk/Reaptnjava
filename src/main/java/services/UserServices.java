package services;

import Models.user;
import interfaces.UserCrud;
import utils.MaConnexion;
import utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserServices implements UserCrud<user> {

    Connection cnx = MaConnexion.getInstance().getConn();

    public user authenticateSymfonyUser(String email, String plainPassword) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try {
            PreparedStatement st = cnx.prepareStatement(sql);
            st.setString(1, email);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                String symfonyHash = rs.getString("password");
                System.out.println("Stored hash from DB: " + symfonyHash);
                System.out.println("Password entered by user: " + plainPassword);

                boolean match = PasswordUtils.checkSymfonyPassword(plainPassword, symfonyHash);
                System.out.println("Password match: " + match);

                if (match) {
                    return extractUserFromResultSet(rs);
                } else {
                    System.out.println(" Password did not match");
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    public user getUserById(int userId) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement st = cnx.prepareStatement(sql)) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
        }
        return null;
    }

    private user extractUserFromResultSet(ResultSet rs) throws SQLException {
        user user = new user();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRoles(rs.getString("roles"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setTelephone(rs.getString("telephone"));
        user.setAvatar(rs.getString("avatar"));
        user.setStatus(rs.getString("status"));
        return user;
    }

    @Override
    public void add(user user) {
        String sql = "INSERT INTO user (email, password, roles, nom, prenom, telephone, status, avatar) VALUES (?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement st = cnx.prepareStatement(sql);
            st.setString(1, user.getEmail());
            st.setString(2, user.getPassword());            String rolesJson = "[\"" + user.getRoles() + "\"]";
            st.setString(3, rolesJson);
            st.setString(4, user.getNom());
            st.setString(5, user.getPrenom());
            st.setString(6, user.getTelephone());
            st.setString(7, user.getStatus());
            st.setString(8, user.getAvatar());

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }


    @Override
    public void delete(user user) {
        String sql = "DELETE FROM user WHERE id = ?";
        try {
            PreparedStatement st = cnx.prepareStatement(sql);
            st.setInt(1, user.getId());
            int rowsAffected = st.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Deleting user failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(user user) {
        String sql = "UPDATE user SET email = ?, password = ?, roles = ?, nom = ?, prenom = ?, telephone = ?, status = ?, avatar = ? WHERE id = ?";

        try {
            PreparedStatement st = cnx.prepareStatement(sql);
            st.setString(1, user.getEmail());
            st.setString(2, user.getPassword());

            // Format roles as JSON array if it's not already
            String rolesJson = user.getRoles().startsWith("[") ? user.getRoles() : "[\"" + user.getRoles() + "\"]";
            st.setString(3, rolesJson);

            st.setString(4, user.getNom());
            st.setString(5, user.getPrenom());
            st.setString(6, user.getTelephone());
            st.setString(7, user.getStatus());
            st.setString(8, user.getAvatar());
            st.setInt(9, user.getId());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<user> getAll() {
        String sql = "SELECT * FROM user";
        List<user> users = new ArrayList<>();

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return users;
    }


}