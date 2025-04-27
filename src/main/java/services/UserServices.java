package services;

import Models.user;
import interfaces.UserCrud;
import utils.MaConnexion;
import utils.PasswordUtils;
import utils.UserStatusService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserServices implements UserCrud<user> {

    Connection cnx = MaConnexion.getInstance().getConn();

    // Add this new method to find user by email
    public user getUserByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement st = cnx.prepareStatement(sql)) {
            st.setString(1, email);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
        }
        return null;
    }

    public user authenticateSymfonyUser(String email, String plainPassword) throws SecurityException {
        String sql = "SELECT * FROM user WHERE email = ?";
        try {
            PreparedStatement st = cnx.prepareStatement(sql);
            st.setString(1, email);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                // First check account status
                String status = rs.getString("status");
                if ("Blocked".equalsIgnoreCase(status)) {
                    throw new SecurityException("This account has been blocked. Please contact administrator.");
                }

                // Skip password check if password is empty (Google users)
                String storedPassword = rs.getString("password");
                if (storedPassword == null || storedPassword.isEmpty()) {
                    return extractUserFromResultSet(rs);
                }

                boolean match = PasswordUtils.checkSymfonyPassword(plainPassword, storedPassword);
                if (match) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            throw new SecurityException("Database error during authentication");
        }
        throw new SecurityException("Invalid credentials");
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
            PreparedStatement st = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setString(1, user.getEmail());
            st.setString(2, user.getPassword() != null ? user.getPassword() : ""); // Handle null password
            String rolesJson = user.getRoles().startsWith("[") ? user.getRoles() : "[\"" + user.getRoles() + "\"]";
            st.setString(3, rolesJson);
            st.setString(4, user.getNom());
            st.setString(5, user.getPrenom());
            st.setString(6, user.getTelephone() != null ? user.getTelephone() : "");
            st.setString(7, user.getStatus());
            st.setString(8, user.getAvatar());

            int affectedRows = st.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = st.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void update(user user) {
        // Separate update for password and non-password fields
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            updateWithPassword(user);
        } else {
            updateWithoutPassword(user);
        }
    }

    private void updateWithPassword(user user) {
        String sql = "UPDATE user SET email = ?, password = ?, roles = ?, nom = ?, prenom = ?, telephone = ?, status = ?, avatar = ? WHERE id = ?";
        try {
            PreparedStatement st = cnx.prepareStatement(sql);
            st.setString(1, user.getEmail());
            st.setString(2, user.getPassword());
            String rolesJson = user.getRoles().startsWith("[") ? user.getRoles() : "[\"" + user.getRoles() + "\"]";
            st.setString(3, rolesJson);
            st.setString(4, user.getNom());
            st.setString(5, user.getPrenom());
            st.setString(6, user.getTelephone() != null ? user.getTelephone() : "");
            st.setString(7, user.getStatus());
            st.setString(8, user.getAvatar());
            st.setInt(9, user.getId());

            int rowsAffected = st.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user with password: " + e.getMessage(), e);
        }
    }

    private void updateWithoutPassword(user user) {
        String sql = "UPDATE user SET email = ?, roles = ?, nom = ?, prenom = ?, telephone = ?, status = ?, avatar = ? WHERE id = ?";
        try {
            PreparedStatement st = cnx.prepareStatement(sql);
            st.setString(1, user.getEmail());
            String rolesJson = user.getRoles().startsWith("[") ? user.getRoles() : "[\"" + user.getRoles() + "\"]";
            st.setString(2, rolesJson);
            st.setString(3, user.getNom());
            st.setString(4, user.getPrenom());
            st.setString(5, user.getTelephone() != null ? user.getTelephone() : "");
            st.setString(6, user.getStatus());
            st.setString(7, user.getAvatar());
            st.setInt(8, user.getId());

            int rowsAffected = st.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user without password: " + e.getMessage(), e);
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

    public boolean updateUserFaceToken(int userId, String faceToken) {
        String query = "UPDATE user SET face_token = ? WHERE id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, faceToken);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<user> getAllUsersWithFaceTokens() {
        List<user> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE face_token IS NOT NULL";
        try (PreparedStatement statement = cnx.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                user u = new user();
                u.setId(resultSet.getInt("id"));
                u.setEmail(resultSet.getString("email"));
                u.setRoles(resultSet.getString("roles")); // Ensure roles are loaded
                u.setFace_token(resultSet.getString("face_token"));
                u.setNom(resultSet.getString("nom"));
                u.setPrenom(resultSet.getString("prenom"));
                u.setTelephone(resultSet.getString("telephone"));
                u.setStatus(resultSet.getString("status"));
                u.setAvatar(resultSet.getString("avatar"));
                users.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}