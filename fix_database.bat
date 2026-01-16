@echo off
echo ========================================
echo CORRECTION DES VUES DE BASE DE DONNEES
echo ========================================

echo.
echo Execution du script de correction...

mysql -u root -p2004 memory_game < fix_database_views.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo CORRECTION TERMINEE AVEC SUCCES
    echo ========================================
    echo.
    echo Les vues problematiques ont ete supprimees.
    echo Les vues utiles ont ete recreees.
    echo.
) else (
    echo.
    echo ========================================
    echo ERREUR LORS DE LA CORRECTION
    echo ========================================
    echo.
    echo Verifiez que MySQL est demarre et que
    echo les parametres de connexion sont corrects.
    echo.
)

pause