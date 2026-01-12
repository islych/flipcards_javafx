-- Script SQL pour créer la base de données avec authentification
DROP DATABASE IF EXISTS memory_game;
CREATE DATABASE memory_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE memory_game;

-- 1. Créer la table themes avec support d'images
CREATE TABLE themes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  image_path VARCHAR(255),
  description TEXT,
  is_active BOOLEAN DEFAULT TRUE,
  created_by INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Créer la table users avec authentification
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) UNIQUE,
    password_hash VARCHAR(255),
    email VARCHAR(100) UNIQUE,
    role VARCHAR(20) DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. Créer la table scores
CREATE TABLE scores (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  theme_id INT,
  attempts INT NOT NULL,
  time_seconds INT NOT NULL,
  played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (theme_id) REFERENCES themes(id) ON DELETE SET NULL
);

-- 4. Insérer les thèmes par défaut avec images
INSERT INTO themes (name, image_path, description, created_by) VALUES 
('Images', '/images/themes/images.png', 'General image matching game', 1), 
('Colors', '/images/themes/colors.png', 'Match beautiful colors', 1), 
('Animals', '/images/themes/animals.png', 'Cute animal matching', 1), 
('Numbers', '/images/themes/numbers.png', 'Classic number matching', 1);

-- 5. Insérer des utilisateurs de test
INSERT INTO users (first_name, last_name, username, password_hash, email, role, is_active) VALUES 
('Admin', 'System', 'admin', '04Kl9hHLs2t2KkrVozVzWTVM7ia0wQAZPxD6drTq7TOrV1Kks6XbnHbU0/LPmn+j', 'admin@memorygame.com', 'ADMIN', TRUE),
('Test', 'User', 'testuser', 'wHubjddKN1UaghSLkNpQMWPnMmJVUKq/xX50vy4Cc4HviTGo2hxdh6PqjtymxBmw', 'test@memorygame.com', 'USER', TRUE),
('Joueur', 'Anonyme', NULL, NULL, NULL, 'USER', TRUE);

-- 6. Créer des index
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_scores_user_id ON scores(user_id);
CREATE INDEX idx_scores_theme_id ON scores(theme_id);
CREATE INDEX idx_scores_played_at ON scores(played_at);
CREATE INDEX idx_themes_active ON themes(is_active);

-- 7. Ajouter la contrainte de clé étrangère pour created_by après insertion des utilisateurs
ALTER TABLE themes ADD CONSTRAINT fk_themes_created_by 
FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;