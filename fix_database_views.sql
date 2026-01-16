-- Script pour corriger les problèmes de vues dans la base de données memory_game
USE memory_game;

-- Désactiver temporairement les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Supprimer les vues problématiques si elles existent
DROP VIEW IF EXISTS multiplayer_leaderboard;
DROP VIEW IF EXISTS multiplayer_session_stats;

-- Supprimer les tables multijoueur si elles existent (dans le bon ordre)
DROP TABLE IF EXISTS game_moves;
DROP TABLE IF EXISTS game_players; 
DROP TABLE IF EXISTS game_sessions;

-- Réactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;

-- Vérifier et recréer les vues utiles qui devraient exister
DROP VIEW IF EXISTS user_scores;
DROP VIEW IF EXISTS best_scores;

-- Recréer la vue user_scores (seulement si les tables existent)
CREATE OR REPLACE VIEW user_scores AS
SELECT 
    s.id,
    u.first_name,
    u.last_name,
    COALESCE(u.username, 'Anonyme') as username,
    t.name as theme_name,
    s.attempts,
    s.time_seconds,
    s.played_at
FROM scores s
INNER JOIN users u ON s.user_id = u.id
INNER JOIN themes t ON s.theme_id = t.id
WHERE u.is_active = TRUE
ORDER BY s.played_at DESC;

-- Recréer la vue best_scores
CREATE OR REPLACE VIEW best_scores AS
SELECT 
    u.first_name,
    u.last_name,
    COALESCE(u.username, 'Anonyme') as username,
    t.name as theme_name,
    MIN(s.attempts) as best_attempts,
    MIN(s.time_seconds) as best_time
FROM scores s
INNER JOIN users u ON s.user_id = u.id
INNER JOIN themes t ON s.theme_id = t.id
WHERE u.is_active = TRUE
GROUP BY u.id, t.id
ORDER BY best_attempts ASC, best_time ASC;

-- Afficher les tables et vues existantes pour vérification
SHOW TABLES;