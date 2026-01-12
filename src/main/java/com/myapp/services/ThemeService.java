package com.myapp.services;

import com.myapp.dao.ThemeDAO;
import com.myapp.models.Theme;

import java.util.List;

public class ThemeService {
    private final ThemeDAO dao = new ThemeDAO();

    public List<Theme> getAllThemes() {
        return dao.findAll();
    }

    public Theme getTheme(int id) { return dao.findById(id); }

    public boolean addTheme(Theme t) { return dao.insert(t); }

    public boolean updateTheme(Theme t) { return dao.update(t); }

    public boolean deleteTheme(int id) { return dao.delete(id); }
}
