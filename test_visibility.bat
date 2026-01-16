@echo off
echo ========================================
echo   MEMORY GAME - TEST DE VISIBILITÉ
echo ========================================
echo.
echo Améliorations appliquées :
echo - Cartes carrées avec contraste maximal
echo - Images parfaitement ajustées
echo - Bouton de fermeture rouge visible
echo - Zone de jeu avec bordure blanche
echo - Ombres portées intenses
echo.
echo Lancement du jeu...
echo.
mvn javafx:run
echo.
echo ========================================
echo Test terminé. Vérifiez que :
echo 1. Les cartes sont parfaitement visibles
echo 2. Le bouton X rouge est visible en haut
echo 3. Les images remplissent bien les cartes
echo 4. La zone de jeu a une bordure blanche
echo ========================================
pause