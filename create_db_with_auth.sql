-- SQL script to create database and tables for Memory Game with Authentication
CREATE DATABASE IF NOT EXISTS memory_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE memory_game;

-- 1. Créer la table users avec authentification
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS themes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS scores (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  theme_id INT,
  attempts INT,
  time_seconds INT,
  played_at DATETIME,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (theme_id) REFERENCES themes(id) ON DELETE SET NULL
);

-- Insert default themes
INSERT INTO themes (name) VALUES ('Images'), ('Colors'), ('Animals'), ('Numbers')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert default admin user (username: admin, password: admin123)
-- Note: Les mots de passe sont hachés avec l'algorithme PasswordUtils
INSERT INTO users (first_name, last_name, username, password_hash, email, is_active) 
VALUES ('Admin', 'System', 'admin', 'YWRtaW4xMjM=', 'admin@memorygame.com', true)
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Insert test user (username: test, password: test123)
INSERT INTO users (first_name, last_name, username, password_hash, email, is_active) 
VALUES ('Test', 'User', 'test', 'dGVzdDEyMw==', 'test@memorygame.com', true)
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Insert guest user for compatibility
INSERT INTO users (first_name, last_name, username, password_hash, email, is_active) 
VALUES ('Invité', 'Anonyme', 'guest', 'Z3Vlc3Q=', null, true)
ON DUPLICATE KEY UPDATE username = VALUES(username);