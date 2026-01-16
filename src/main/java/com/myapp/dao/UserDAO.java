package com.myapp.dao;

import com.myapp.config.MySQLConnection;
import com.myapp.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, username, password_hash, email, role, is_active, last_login, created_at, updated_at FROM users";
        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                );
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash")); // IMPORTANT: Charger le hash
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                
                // Gérer les timestamps nullable
                Timestamp lastLogin = rs.getTimestamp("last_login");
                if (lastLogin != null) {
                    user.setLastLogin(lastLogin.toLocalDateTime());
                }
                
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    user.setCreatedAt(createdAt.toLocalDateTime());
                }
                
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    user.setUpdatedAt(updatedAt.toLocalDateTime());
                }
                
                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public User findById(int id) {
        String sql = "SELECT id, first_name, last_name, username, password_hash, email, role, is_active, last_login, created_at, updated_at FROM users WHERE id = ?";
        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                );
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash")); // IMPORTANT: Charger le hash
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                
                // Gérer les timestamps nullable
                Timestamp lastLogin = rs.getTimestamp("last_login");
                if (lastLogin != null) {
                    user.setLastLogin(lastLogin.toLocalDateTime());
                }
                
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    user.setCreatedAt(createdAt.toLocalDateTime());
                }
                
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    user.setUpdatedAt(updatedAt.toLocalDateTime());
                }
                
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(User user) {
        String sql = "INSERT INTO users (first_name, last_name) VALUES (?, ?)";
        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            int affected = ps.executeUpdate();
            if (affected == 1) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) user.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setInt(4, user.getId());
            int affected = ps.executeUpdate();
            return affected == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}