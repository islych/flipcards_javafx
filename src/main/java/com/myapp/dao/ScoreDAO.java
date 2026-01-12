package com.myapp.dao;

import com.myapp.config.MySQLConnection;
import com.myapp.models.Score;
import com.myapp.models.Theme;
import com.myapp.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des scores en base de données
 * Respecte les relations UML Score-User-Theme
 */
public class ScoreDAO {

    public boolean insert(Score s) {
        String sql = "INSERT INTO scores (user_id, theme_id, attempts, time_seconds, played_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getUser().getId());
            if (s.getTheme() != null) {
                ps.setInt(2, s.getTheme().getId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, s.getAttempts());
            ps.setInt(4, s.getTimeSeconds());
            ps.setTimestamp(5, Timestamp.valueOf(s.getPlayedAt()));

            int affected = ps.executeUpdate();
            if (affected == 1) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) s.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Score> findAll(String orderBy) {
        List<Score> list = new ArrayList<>();
        String sql = "SELECT s.id, s.attempts, s.time_seconds, s.played_at, " +
                "u.id as user_id, u.first_name, u.last_name, " +
                "t.id as theme_id, t.name as theme_name " +
                "FROM scores s " +
                "INNER JOIN users u ON s.user_id = u.id " +
                "INNER JOIN themes t ON s.theme_id = t.id";

        if ("score".equalsIgnoreCase(orderBy))
            sql += " ORDER BY s.attempts ASC, s.time_seconds ASC";
        else
            sql += " ORDER BY s.played_at DESC";

        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(createScoreFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Score findById(int id) {
        String sql = "SELECT s.id, s.attempts, s.time_seconds, s.played_at, " +
                "u.id as user_id, u.first_name, u.last_name, " +
                "t.id as theme_id, t.name as theme_name " +
                "FROM scores s " +
                "INNER JOIN users u ON s.user_id = u.id " +
                "INNER JOIN themes t ON s.theme_id = t.id " +
                "WHERE s.id = ?";

        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return createScoreFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Score> findByUserId(int userId) {
        List<Score> list = new ArrayList<>();
        String sql = "SELECT s.id, s.attempts, s.time_seconds, s.played_at, " +
                "u.id as user_id, u.first_name, u.last_name, " +
                "t.id as theme_id, t.name as theme_name " +
                "FROM scores s " +
                "INNER JOIN users u ON s.user_id = u.id " +
                "INNER JOIN themes t ON s.theme_id = t.id " +
                "WHERE s.user_id = ? " +
                "ORDER BY s.played_at DESC";

        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(createScoreFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Score> findByThemeId(int themeId) {
        List<Score> list = new ArrayList<>();
        String sql = "SELECT s.id, s.attempts, s.time_seconds, s.played_at, " +
                "u.id as user_id, u.first_name, u.last_name, " +
                "t.id as theme_id, t.name as theme_name " +
                "FROM scores s " +
                "INNER JOIN users u ON s.user_id = u.id " +
                "INNER JOIN themes t ON s.theme_id = t.id " +
                "WHERE s.theme_id = ? " +
                "ORDER BY s.played_at DESC";

        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, themeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(createScoreFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM scores WHERE id = ?";
        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Score score) {
        String sql = "UPDATE scores SET user_id = ?, theme_id = ?, attempts = ?, time_seconds = ?, played_at = ? WHERE id = ?";
        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, score.getUser().getId());
            ps.setInt(2, score.getTheme().getId());
            ps.setInt(3, score.getAttempts());
            ps.setInt(4, score.getTimeSeconds());
            ps.setTimestamp(5, Timestamp.valueOf(score.getPlayedAt()));
            ps.setInt(6, score.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Méthode utilitaire pour créer un objet Score à partir d'un ResultSet
     * Maintient les relations bidirectionnelles selon le diagramme UML
     */
    private Score createScoreFromResultSet(ResultSet rs) throws SQLException {
        // Créer les objets associés
        User user = new User(
                rs.getInt("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name")
        );

        Theme theme = new Theme(
                rs.getInt("theme_id"),
                rs.getString("theme_name")
        );

        // Créer le score avec les relations
        Score score = new Score(
                rs.getInt("id"),
                user,
                theme,
                rs.getInt("attempts"),
                rs.getInt("time_seconds"),
                rs.getTimestamp("played_at").toLocalDateTime()
        );

        return score;
    }
}
