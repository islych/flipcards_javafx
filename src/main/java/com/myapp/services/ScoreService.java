package com.myapp.services;

import com.myapp.dao.ScoreDAO;
import com.myapp.models.Score;

import java.util.List;

/**
 * Service pour la gestion des scores
 * Respecte les relations UML Score-User-Theme
 */
public class ScoreService {
    private final ScoreDAO dao = new ScoreDAO();

    public boolean saveScore(Score s) { 
        return dao.insert(s); 
    }

    public List<Score> listScoresBy(String orderBy) { 
        return dao.findAll(orderBy); 
    }

    public boolean deleteScore(int id) { 
        return dao.delete(id); 
    }

    public Score getScore(int id) {
        return dao.findById(id);
    }

    public List<Score> getScoresByUser(int userId) {
        return dao.findByUserId(userId);
    }

    public List<Score> getScoresByTheme(int themeId) {
        return dao.findByThemeId(themeId);
    }
}
