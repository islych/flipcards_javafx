@echo off
echo ========================================
echo SUPPRESSION DES MODIFICATIONS MULTIJOUEUR
echo ========================================

echo.
echo 1. Suppression des fichiers multijoueur...

REM Supprimer les nouveaux modèles
del /f /q "src\main\java\com\myapp\models\GameSession.java" 2>nul
del /f /q "src\main\java\com\myapp\models\GamePlayer.java" 2>nul
del /f /q "src\main\java\com\myapp\models\GameMove.java" 2>nul

REM Supprimer les fichiers réseau
rmdir /s /q "src\main\java\com\myapp\network" 2>nul

REM Supprimer les nouveaux services
del /f /q "src\main\java\com\myapp\services\MultiplayerGameService.java" 2>nul

REM Supprimer les nouveaux DAO
del /f /q "src\main\java\com\myapp\dao\GameSessionDAO.java" 2>nul
del /f /q "src\main\java\com\myapp\dao\GamePlayerDAO.java" 2>nul
del /f /q "src\main\java\com\myapp\dao\GameMoveDAO.java" 2>nul

REM Supprimer les nouveaux contrôleurs
del /f /q "src\main\java\com\myapp\controllers\MultiplayerLobbyController.java" 2>nul

REM Supprimer les nouvelles vues
del /f /q "src\main\resources\views\multiplayer-lobby.fxml" 2>nul

REM Supprimer les fichiers de documentation
del /f /q "create_multiplayer_tables.sql" 2>nul
del /f /q "MULTIPLAYER_SETUP.md" 2>nul

echo.
echo 2. Nettoyage de la base de données...

REM Supprimer les tables multijoueur de la base de données (ignorer les erreurs)
mysql -u root -p2004 -e "DROP TABLE IF EXISTS game_moves; DROP TABLE IF EXISTS game_players; DROP TABLE IF EXISTS game_sessions; DROP VIEW IF EXISTS multiplayer_session_stats; DROP VIEW IF EXISTS multiplayer_leaderboard; ALTER TABLE scores DROP COLUMN IF EXISTS session_id;" memory_game 2>nul || echo Nettoyage de la base de donnees termine (certaines erreurs sont normales)

echo.
echo 3. Restauration des fichiers modifiés...

REM Note: Les fichiers modifiés devront être restaurés manuellement
echo ATTENTION: Les fichiers suivants ont été modifiés et doivent être restaurés manuellement:
echo - src\main\java\com\myapp\controllers\HomeController.java
echo - src\main\resources\views\home.fxml
echo - src\main\java\com\myapp\utils\SceneManager.java
echo - src\main\resources\styles\app.css
echo - src\main\java\com\myapp\models\User.java
echo - src\main\java\com\myapp\models\Theme.java
echo - src\main\java\com\myapp\models\GameSession.java (si existait)
echo - src\main\java\com\myapp\models\GamePlayer.java (si existait)

echo.
echo 4. Nettoyage du projet...
mvn clean

echo.
echo ========================================
echo SUPPRESSION TERMINEE
echo ========================================
echo.
echo PROCHAINES ETAPES MANUELLES:
echo 1. Restaurer les fichiers modifiés depuis votre sauvegarde
echo 2. Ou utiliser Git pour revenir à l'état précédent
echo 3. Recompiler avec: mvn clean compile
echo.
pause