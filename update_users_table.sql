-- Script pour mettre à jour la table users avec les colonnes manquantes
USE memory_game;

-- Ajouter la colonne role si elle n'existe pas
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'user';

-- Mettre à jour les utilisateurs existants avec des rôles par défaut
UPDATE users SET role = 'admin' WHERE username = 'admin';
UPDATE users SET role = 'user' WHERE role IS NULL OR role = '';

-- Ajouter des mots de passe par défaut pour les tests (en production, utiliser des hash sécurisés)
UPDATE users SET password_hash = 'admin123' WHERE username = 'admin' AND password_hash = 'hashed_password_here';
UPDATE users SET password_hash = 'test123' WHERE username = 'testuser' AND password_hash = 'hashed_password_here';

-- Afficher la structure mise à jour
DESCRIBE users;