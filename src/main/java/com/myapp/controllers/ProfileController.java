package com.myapp.controllers;

import com.myapp.models.User;
import com.myapp.services.UserService;
import com.myapp.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.application.Platform;

import java.time.format.DateTimeFormatter;

public class ProfileController {
    @FXML
    private Label firstNameLabel;
    
    @FXML
    private Label lastNameLabel;
    
    @FXML
    private Label usernameLabel;
    
    @FXML
    private Label emailLabel;
    
    @FXML
    private Label roleLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label createdAtLabel;
    
    @FXML
    private Label lastLoginLabel;
    
    @FXML
    private Button btnBack;
    
    private final UserService userService = new UserService();
    private User currentUser;
    
    @FXML
    public void initialize() {
        loadCurrentUser();
        if (currentUser != null) {
            displayUserInfo();
        } else {
            Platform.runLater(() -> SceneManager.show("home"));
        }
    }
    
    private void loadCurrentUser() {
        String currentUserIdStr = System.getProperty("currentUserId");
        String isGuestStr = System.getProperty("isGuest");
        
        if ("true".equals(isGuestStr)) {
            // Les invités ne peuvent pas accéder au profil
            Platform.runLater(() -> SceneManager.show("home"));
            return;
        }
        
        if (currentUserIdStr != null) {
            try {
                int userId = Integer.parseInt(currentUserIdStr);
                currentUser = userService.getUser(userId);
            } catch (NumberFormatException e) {
                System.err.println("ID utilisateur invalide: " + currentUserIdStr);
            }
        }
    }
    
    private void displayUserInfo() {
        // Afficher les informations de base
        firstNameLabel.setText(currentUser.getFirstName() != null ? currentUser.getFirstName() : "Non défini");
        lastNameLabel.setText(currentUser.getLastName() != null ? currentUser.getLastName() : "Non défini");
        usernameLabel.setText(currentUser.getUsername() != null ? currentUser.getUsername() : "Non défini");
        emailLabel.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "Non défini");
        roleLabel.setText(currentUser.getRole() != null ? currentUser.getRole() : "USER");
        
        // Afficher le statut
        statusLabel.setText(currentUser.isActive() ? "Actif" : "Inactif");
        
        // Formater et afficher les dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        
        if (currentUser.getCreatedAt() != null) {
            createdAtLabel.setText(currentUser.getCreatedAt().format(formatter));
        } else {
            createdAtLabel.setText("Non disponible");
        }
        
        if (currentUser.getLastLogin() != null) {
            lastLoginLabel.setText(currentUser.getLastLogin().format(formatter));
        } else {
            lastLoginLabel.setText("Jamais connecté");
        }
    }
    
    @FXML
    private void onBack() {
        SceneManager.show("home");
    }
}