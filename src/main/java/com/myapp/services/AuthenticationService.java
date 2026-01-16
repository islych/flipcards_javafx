package com.myapp.services;

import com.myapp.dao.UserAuthDAO;
import com.myapp.models.LoginResult;
import com.myapp.models.User;
import com.myapp.utils.PasswordUtils;

import java.time.LocalDateTime;

/**
 * Service d'authentification pour gérer les connexions et inscriptions
 */
public class AuthenticationService {
    private final UserAuthDAO userAuthDAO;
    private User currentUser;

    public AuthenticationService() {
        this.userAuthDAO = new UserAuthDAO();
    }

    /**
     * Authentifie un utilisateur avec nom d'utilisateur et mot de passe
     * @return LoginResult indiquant le résultat de la tentative de connexion
     */
    public LoginResult login(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return LoginResult.EMPTY_FIELDS;
        }

        User user = userAuthDAO.findByUsername(username.trim());
        if (user == null) {
            return LoginResult.USER_NOT_FOUND;
        }

        // Vérifier si le compte est désactivé AVANT de vérifier le mot de passe
        if (!user.isActive()) {
            return LoginResult.ACCOUNT_DISABLED;
        }

        // Vérifier le mot de passe
        if (PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            this.currentUser = user;
            user.updateLastLogin();
            userAuthDAO.updateLastLogin(user.getId());
            return LoginResult.SUCCESS;
        }

        return LoginResult.INVALID_CREDENTIALS;
    }

    /**
     * Méthode de compatibilité qui retourne un boolean (pour le code existant)
     * @deprecated Utilisez login(String, String) qui retourne LoginResult
     */
    @Deprecated
    public boolean loginBoolean(String username, String password) {
        return login(username, password) == LoginResult.SUCCESS;
    }

    /**
     * Inscrit un nouvel utilisateur
     */
    public boolean register(String firstName, String lastName, String username, 
                          String email, String password) {
        // Validation des données
        if (!isValidRegistrationData(firstName, lastName, username, email, password)) {
            return false;
        }

        // Vérifier si le nom d'utilisateur existe déjà
        if (userAuthDAO.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Vérifier si l'email existe déjà
        if (email != null && !email.trim().isEmpty() && userAuthDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Créer le nouvel utilisateur
        User newUser = new User(firstName.trim(), lastName.trim(), username.trim(), 
                               email != null ? email.trim() : null);
        newUser.setPasswordHash(PasswordUtils.hashPassword(password));

        return userAuthDAO.insert(newUser);
    }

    /**
     * Déconnecte l'utilisateur actuel
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Retourne l'utilisateur actuellement connecté
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Change le mot de passe de l'utilisateur actuel
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!isLoggedIn()) {
            return false;
        }

        if (!PasswordUtils.verifyPassword(oldPassword, currentUser.getPasswordHash())) {
            return false;
        }

        if (!isValidPassword(newPassword)) {
            return false;
        }

        String newHash = PasswordUtils.hashPassword(newPassword);
        return userAuthDAO.updatePassword(currentUser.getId(), newHash);
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur (pour admin)
     */
    public boolean resetPassword(String username, String newPassword) {
        User user = userAuthDAO.findByUsername(username);
        if (user == null) {
            return false;
        }

        if (!isValidPassword(newPassword)) {
            return false;
        }

        String newHash = PasswordUtils.hashPassword(newPassword);
        return userAuthDAO.updatePassword(user.getId(), newHash);
    }

    /**
     * Valide les données d'inscription
     */
    private boolean isValidRegistrationData(String firstName, String lastName, 
                                          String username, String email, String password) {
        return isValidName(firstName) && 
               isValidName(lastName) && 
               isValidUsername(username) && 
               isValidEmail(email) && 
               isValidPassword(password);
    }

    /**
     * Valide un nom (prénom ou nom de famille)
     */
    private boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2 && name.trim().length() <= 50;
    }

    /**
     * Valide un nom d'utilisateur
     */
    private boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String trimmed = username.trim();
        return trimmed.length() >= 3 && trimmed.length() <= 50 && 
               trimmed.matches("^[a-zA-Z0-9_.-]+$");
    }

    /**
     * Valide une adresse email (optionnelle)
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email optionnel
        }
        return email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Valide un mot de passe
     */
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6 && password.length() <= 100;
    }

    /**
     * Vérifie si un nom d'utilisateur est disponible
     */
    public boolean isUsernameAvailable(String username) {
        if (!isValidUsername(username)) {
            return false;
        }
        return userAuthDAO.findByUsername(username.trim()) == null;
    }

    /**
     * Vérifie si une adresse email est disponible
     */
    public boolean isEmailAvailable(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        if (!isValidEmail(email)) {
            return false;
        }
        return userAuthDAO.findByEmail(email.trim()) == null;
    }
}