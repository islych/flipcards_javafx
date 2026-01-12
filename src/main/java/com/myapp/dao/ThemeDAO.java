package com.myapp.dao;

import com.myapp.config.MySQLConnection;
import com.myapp.models.Theme;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThemeDAO {

    public List<Theme> findAll() {
        List<Theme> list = new ArrayList<>();
        String sql = "SELECT id, name FROM themes";
        try (Connection c = MySQLConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Theme(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Theme findById(int id) {
        String sql = "SELECT id, name FROM themes WHERE id = ?";
        try (Connection c = MySQLConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Theme(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(Theme t) {
        String sql = "INSERT INTO themes (name) VALUES (?)";
        try (Connection c = MySQLConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getName());
            int affected = ps.executeUpdate();
            if (affected == 1) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) t.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Theme t) {
        String sql = "UPDATE themes SET name = ? WHERE id = ?";
        try (Connection c = MySQLConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setInt(2, t.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM themes WHERE id = ?";
        try (Connection c = MySQLConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
