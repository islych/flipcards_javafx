-- Script pour tester la connexion d'un utilisateur désactivé
USE memory_game;

-- Créer un utilisateur de test
INSERT INTO users (first_name, last_name, username, password_hash, email, role, is_active, created_at) 
VALUES ('Test', 'Désactivé', 'testdesactive', 'dGVzdA==', 'test.desactive@test.com', 'USER', FALSE, NOW())
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Vérifier que l'utilisateur existe et est désactivé
SELECT id, first_name, last_name, username, is_active, role 
FROM users 
WHERE username = 'testdesactive';

-- Pour réactiver l'utilisateur plus tard (si besoin)
-- UPDATE users SET is_active = TRUE WHERE username = 'testdesactive';

-- Pour supprimer l'utilisateur de test
-- DELETE FROM users WHERE username = 'testdesactive';