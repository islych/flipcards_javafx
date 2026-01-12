@echo off
echo Lancement du jeu Memory avec les nouvelles ameliorations...
echo - Background purple1.jpg optimise (image complete visible)
echo - Cartes avec cardback.png
echo - Interface amelioree avec transparences
mvn clean compile
if %errorlevel% neq 0 (
    echo Erreur de compilation!
    pause
    exit /b 1
)
echo Compilation reussie, lancement du jeu...
mvn javafx:run
pause