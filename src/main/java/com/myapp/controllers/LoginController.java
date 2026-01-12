package com.myapp.controllers;

import com.myapp.models.User;
import com.myapp.services.AuthenticationService;
import com.myapp.utils.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Contrôleur pour la vue de connexion
 */
public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnRegister;

    @FXML
    private Button btnForgotPassword;

    @FXML
    private Button btnGuest;

    @FXML
    private Label errorLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private ImageView logoView;

    private final AuthenticationService authService = new AuthenticationService();

    @FXML
    public void initialize() {
        // Charger le logo si disponible
        loadLogo();
        
        // Configurer les événements Enter
        setupEnterKeyHandlers();
        
        // Focus initial sur le champ nom d'utilisateur
        Platform.runLater(() -> usernameField.requestFocus());
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validation basique
        if (username == null || username.trim().isEmpty()) {
            showError("Veuillez entrer votre nom d'utilisateur.");
            usernameField.requestFocus();
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            showError("Veuillez entrer votre mot de passe.");
            passwordField.requestFocus();
            return;
        }

        // Désactiver le bouton pendant la connexion
        btnLogin.setDisable(true);
        btnLogin.setText("Connexion...");

        // Authentification en arrière-plan
        new Thread(() -> {
            try {
                boolean success = authService.login(username.trim(), password);
                
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    btnLogin.setText("Se connecter");
                    
                    if (success) {
                        User currentUser = authService.getCurrentUser();
                        System.out.println("Connexion réussie pour: " + currentUser.getFullName());
                        
                        // Stocker l'utilisateur connecté pour les autres contrôleurs
                        System.setProperty("currentUserId", String.valueOf(currentUser.getId()));
                        System.setProperty("currentUserName", currentUser.getFullName());
                        
                        // IMPORTANT: Effacer le mode invité lors de la connexion
                        System.clearProperty("isGuest");
                        
                        // Rediriger vers l'écran d'accueil
                        SceneManager.show("home");
                    } else {
                        showError("Nom d'utilisateur ou mot de passe incorrect.");
                        passwordField.clear();
                        usernameField.requestFocus();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    btnLogin.setText("Se connecter");
                    showError("Erreur de connexion. Veuillez réessayer.");
                    e.printStackTrace();
                });
            }
        }).start();
    }

    @FXML
    private void onShowRegister(ActionEvent event) {
        SceneManager.show("register");
    }

    @FXML
    private void onForgotPassword(ActionEvent event) {
        // TODO: Implémenter la récupération de mot de passe
        showError("Fonctionnalité non encore implémentée. Contactez l'administrateur.");
    }

    @FXML
    private void onGuestMode(ActionEvent event) {
        // Créer un utilisateur invité temporaire
        User guestUser = new User("Invité", "Anonyme");
        guestUser.setId(-1); // ID spécial pour l'invité
        
        // Stocker les informations de l'invité
        System.setProperty("currentUserId", "-1");
        System.setProperty("currentUserName", "Invité Anonyme");
        System.setProperty("isGuest", "true");
        
        System.out.println("Mode invité activé");
        SceneManager.show("home");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        
        // Masquer le message après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> {
                    if (errorLabel.getText().equals(message)) {
                        errorLabel.setVisible(false);
                    }
                });
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void loadLogo() {
        try {
            var res = getClass().getResource("/images/logo.png");
            if (res != null) {
                logoView.setImage(new Image(res.toExternalForm()));
            } else {
                logoView.setVisible(false);
            }
        } catch (Exception e) {
            logoView.setVisible(false);
        }
    }

    private void setupEnterKeyHandlers() {
        // Permettre la connexion avec Enter
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> onLogin(null));
    }
}