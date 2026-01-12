-- SQL script to create database and tables for Memory Game with Authentication
CREATE DATABASE IF NOT EXISTS memory_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE memory_game;

-- 1. Créer la table themes
CREATE TABLE IF NOT EXISTS themes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

-- 2. Créer la table users avec authentification
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) UNIQUE,
    password_hash VARCHAR(255),
    email VARCHAR(100) UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. Créer la table scores avec les nouvelles relations
CREATE TABLE IF NOT EXISTS scores (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  theme_id INT NOT NULL,
  attempts INT NOT NULL,
  time_seconds INT NOT NULL,
  played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (theme_id) REFERENCES themes(id) ON DELETE SET NULL
);

-- Insert default themes
INSERT INTO themes (name) VALUES ('Images'), ('Colors'), ('Animals'), ('Numbers')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert default users (for testing)
INSERT INTO users (first_name, last_name, username, password_hash, email, is_active) VALUES 
('Admin', 'System', 'admin', 'hashed_password_here', 'admin@memorygame.com', TRUE),
('Test', 'User', 'testuser', 'hashed_password_here', 'test@memorygame.com', TRUE),
('Joueur', 'Anonyme', NULL, NULL, NULL, TRUE)
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name);

-- Créer des index pour améliorer les performances
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_scores_user_id ON scores(user_id);
CREATE INDEX idx_scores_theme_id ON scores(theme_id);
CREATE INDEX idx_scores_played_at ON scores(played_at);

-- Vues utiles pour les requêtes
CREATE OR REPLACE VIEW user_scores AS
SELECT 
    s.id,
    u.first_name,
    u.last_name,
    u.username,
    t.name as theme_name,
    s.attempts,
    s.time_seconds,
    s.played_at
FROM scores s
INNER JOIN users u ON s.user_id = u.id
INNER JOIN themes t ON s.theme_id = t.id
WHERE u.is_active = TRUE
ORDER BY s.played_at DESC;

CREATE OR REPLACE VIEW best_scores AS
SELECT 
    u.first_name,
    u.last_name,
    t.name as theme_name,
    MIN(s.attempts) as best_attempts,
    MIN(s.time_seconds) as best_time
FROM scores s
INNER JOIN users u ON s.user_id = u.id
INNER JOIN themes t ON s.theme_id = t.id
WHERE u.is_active = TRUE
GROUP BY u.id, t.id
ORDER BY best_attempts ASC, best_time ASC;