package com.myapp.utils;

import com.myapp.models.User;

import java.time.LocalDateTime;

/**
 * Classe pour gérer les sessions utilisateur
 * Singleton pattern pour maintenir la session active
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;
    private LocalDateTime loginTime;
    private boolean isLoggedIn;

    private UserSession() {
        this.isLoggedIn = false;
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        this.loginTime = LocalDateTime.now();
        this.isLoggedIn = true;
        
        // Mettre à jour le dernier login de l'utilisateur
        if (user != null) {
            user.updateLastLogin();
        }
    }

    public void logout() {
        this.currentUser = null;
        this.loginTime = null;
        this.isLoggedIn = false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return isLoggedIn && currentUser != null;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public String getSessionDuration() {
        if (loginTime == null) return "0 min";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(loginTime, now).toMinutes();
        
        if (minutes < 60) {
            return minutes + " min";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + "h " + remainingMinutes + "min";
        }
    }

    public boolean isSessionValid() {
        if (!isLoggedIn || currentUser == null || loginTime == null) {
            return false;
        }
        
        // Session expire après 24 heures
        LocalDateTime now = LocalDateTime.now();
        long hours = java.time.Duration.between(loginTime, now).toHours();
        
        return hours < 24;
    }

    public void refreshSession() {
        if (isLoggedIn && currentUser != null) {
            this.loginTime = LocalDateTime.now();
        }
    }
}