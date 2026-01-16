@echo off
echo ========================================
echo MISE A JOUR DE LA BASE DE DONNEES
echo ========================================

echo.
echo Ajout de la colonne role...
mysql -u root -p -e "USE memory_game; ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'user';" 2>nul

echo.
echo Mise a jour des roles...
mysql -u root -p -e "USE memory_game; UPDATE users SET role = 'admin' WHERE username = 'admin';"

echo.
echo Mise a jour des mots de passe de test...
mysql -u root -p -e "USE memory_game; UPDATE users SET password_hash = 'admin123' WHERE username = 'admin';"
mysql -u root -p -e "USE memory_game; UPDATE users SET password_hash = 'test123' WHERE username = 'testuser';"

echo.
echo Verification de la structure...
mysql -u root -p -e "USE memory_game; DESCRIBE users;"

echo.
echo ========================================
echo MISE A JOUR TERMINEE
echo ========================================
pause