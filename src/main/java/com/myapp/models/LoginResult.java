package com.myapp.models;

/**
 * Énumération pour les différents résultats de tentative de connexion
 */
public enum LoginResult {
    SUCCESS("Connexion réussie"),
    INVALID_CREDENTIALS("Nom d'utilisateur ou mot de passe incorrect"),
    ACCOUNT_DISABLED("Ce compte est désactivé. Contactez un administrateur."),
    USER_NOT_FOUND("Utilisateur non trouvé"),
    EMPTY_FIELDS("Veuillez remplir tous les champs");

    private final String message;

    LoginResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}