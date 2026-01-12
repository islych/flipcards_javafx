-- SQL script to create database and tables for Memory Game
CREATE DATABASE IF NOT EXISTS memory_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE memory_game;

CREATE TABLE IF NOT EXISTS themes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS scores (
  id INT AUTO_INCREMENT PRIMARY KEY,
  player_name VARCHAR(50) NOT NULL,
  theme_id INT,
  attempts INT,
  time_seconds INT,
  played_at DATETIME,
  FOREIGN KEY (theme_id) REFERENCES themes(id) ON DELETE SET NULL
);

-- Insert default themes
INSERT INTO themes (name) VALUES ('Images'), ('Colors'), ('Animals'), ('Numbers')
ON DUPLICATE KEY UPDATE name = VALUES(name);


-- 1. Créer la table users
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Modifier la table scores
ALTER TABLE scores
    DROP COLUMN player_name,
    ADD COLUMN user_id INT NOT NULL,
    ADD FOREIGN KEY (user_id) REFERENCES users(id);

-- 3. Migrer les données existantes (si nécessaire)
-- Insérer un utilisateur par défaut pour les scores existants
INSERT INTO users (first_name, last_name) VALUES ('Joueur', 'Anonyme');

-- Mettre à jour les scores avec le premier utilisateur
UPDATE scores SET user_id = 1 WHERE user_id IS NULL;