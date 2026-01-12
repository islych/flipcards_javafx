package com.myapp.controllers;

import com.myapp.models.Theme;
import com.myapp.models.User;
import com.myapp.services.AuthenticationService;
import com.myapp.services.ThemeService;
import com.myapp.services.UserService;
import com.myapp.utils.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import java.util.List;
public class HomeController {
    @FXML
    private ComboBox<Theme> themeCombo;

    @FXML
    private ComboBox<String> gridSizeCombo;

    @FXML
    private Button btnStart;

    @FXML
    private Button btnScores;

    @FXML
    private TextField playerNameField;

    @FXML
    private ImageView logoView;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnProfile;

    @FXML
    private Button btnAdminPanel;

    private final ThemeService themeService = new ThemeService();
    private final UserService userService = new UserService();
    private final AuthenticationService authService = new AuthenticationService();
    
    // Éléments de sélection d'utilisateur (à masquer pour les utilisateurs authentifiés)
    @FXML 
    private ComboBox<User> userCombo;
    @FXML 
    private GridPane newUserForm;
    @FXML 
    private TextField firstNameField;
    @FXML 
    private TextField lastNameField;
    @FXML 
    private Button btnNewUser;
    @FXML 
    private Button btnSaveUser;
    @FXML 
    private Button btnCancelUser;
    @FXML 
    private Button btnManagePlayers;
    @FXML
    private HBox userSelectionRow;

    private User currentUser;
    private boolean isGuestMode;

    @FXML
    public void initialize() {
        System.out.println("=== DEBUG initialize ===");
        
        // Vérifier l'injection des boutons
        System.out.println("btnScores: " + (btnScores != null ? "OK" : "NULL"));
        System.out.println("btnAdminPanel: " + (btnAdminPanel != null ? "OK" : "NULL"));
        System.out.println("btnProfile: " + (btnProfile != null ? "OK" : "NULL"));
        System.out.println("btnLogout: " + (btnLogout != null ? "OK" : "NULL"));
        
        // Récupérer l'utilisateur actuel
        loadCurrentUser();
        
        // Initialiser les thèmes
        List<Theme> themes = themeService.getAllThemes();
        if (themes.isEmpty()) {
            // thèmes par défaut de secours
            themes.add(new Theme(1, "Images"));
            themes.add(new Theme(2, "Colors"));
            themes.add(new Theme(3, "Animals"));
            themes.add(new Theme(4, "Numbers"));
        }
        themeCombo.setItems(FXCollections.observableArrayList(themes));
        if (!themes.isEmpty()) themeCombo.getSelectionModel().select(0);

        // Initialiser les options de taille de grille
        gridSizeCombo.setItems(FXCollections.observableArrayList("4x4", "5x5", "6x6"));
        gridSizeCombo.getSelectionModel().select(0); // par défaut 4x4

        // Configurer l'interface selon le mode (authentifié ou invité)
        setupUserInterface();

        // Charger le logo si disponible
        loadLogo();
        
        System.out.println("=== END DEBUG initialize ===");
    }

    private void loadCurrentUser() {
        String currentUserIdStr = System.getProperty("currentUserId");
        String isGuestStr = System.getProperty("isGuest");
        
        isGuestMode = "true".equals(isGuestStr);
        
        if (currentUserIdStr != null && !currentUserIdStr.equals("-1") && !isGuestMode) {
            try {
                int userId = Integer.parseInt(currentUserIdStr);
                currentUser = userService.getUser(userId);
                if (currentUser == null) {
                    // Utilisateur non trouvé, rediriger vers la connexion
                    redirectToLogin();
                    return;
                }
            } catch (NumberFormatException e) {
                redirectToLogin();
                return;
            }
        } else if (isGuestMode) {
            // Mode invité
            currentUser = new User("Invité", "Anonyme");
            currentUser.setId(-1);
        } else {
            // Pas d'utilisateur connecté
            redirectToLogin();
            return;
        }
    }

    private void setupUserInterface() {
        System.out.println("=== DEBUG setupUserInterface ===");
        System.out.println("isGuestMode: " + isGuestMode);
        System.out.println("currentUser: " + (currentUser != null ? currentUser.getFullName() + " (ID: " + currentUser.getId() + ", Role: " + currentUser.getRole() + ")" : "null"));
        
        if (isGuestMode) {
            // Mode invité - interface simplifiée
            if (welcomeLabel != null) welcomeLabel.setText("Bienvenue, Invité !");
            if (btnLogout != null) btnLogout.setText("Se connecter");
            if (btnProfile != null) btnProfile.setVisible(false);
            if (btnAdminPanel != null) btnAdminPanel.setVisible(false);
            
            // Masquer complètement la sélection d'utilisateur en mode invité
            hideUserSelection();
            
        } else if (currentUser != null) {
            // Utilisateur authentifié
            if (welcomeLabel != null) welcomeLabel.setText("Bienvenue, " + currentUser.getFullName() + " !");
            if (btnLogout != null) btnLogout.setText("Se déconnecter");
            if (btnProfile != null) btnProfile.setVisible(true);
            
            // Afficher le panneau admin seulement pour les administrateurs
            boolean isAdmin = currentUser.isAdmin();
            System.out.println("User isAdmin: " + isAdmin);
            
            if (btnAdminPanel != null) {
                btnAdminPanel.setVisible(isAdmin);
                btnAdminPanel.setManaged(isAdmin);
                System.out.println("Admin panel button - visible: " + isAdmin + ", managed: " + isAdmin);
            } else {
                System.out.println("ERROR: btnAdminPanel is null!");
            }
            
            // S'assurer que les boutons essentiels sont toujours visibles pour les admins
            if (btnScores != null) {
                btnScores.setVisible(true);
                btnScores.setManaged(true);
                System.out.println("Scores button - visible: true, managed: true");
            } else {
                System.out.println("ERROR: btnScores is null!");
            }
            
            // MASQUER la sélection d'utilisateur car l'utilisateur est déjà connecté
            hideUserSelection();
            
            // S'assurer que les boutons essentiels sont visibles
            ensureAdminButtonsVisible();
        }
        System.out.println("=== END DEBUG setupUserInterface ===");
    }

    private void hideUserSelection() {
        // Masquer tous les éléments de sélection d'utilisateur
        if (userCombo != null) {
            userCombo.setVisible(false);
            userCombo.setManaged(false);
        }
        if (btnNewUser != null) {
            btnNewUser.setVisible(false);
            btnNewUser.setManaged(false);
        }
        if (btnManagePlayers != null) {
            btnManagePlayers.setVisible(false);
            btnManagePlayers.setManaged(false);
        }
        if (newUserForm != null) {
            newUserForm.setVisible(false);
            newUserForm.setManaged(false);
        }
        if (userSelectionRow != null) {
            userSelectionRow.setVisible(false);
            userSelectionRow.setManaged(false);
        }
    }

    private void ensureAdminButtonsVisible() {
        // S'assurer que tous les boutons essentiels sont visibles
        if (btnStart != null) {
            btnStart.setVisible(true);
            btnStart.setManaged(true);
        }
        if (btnScores != null) {
            btnScores.setVisible(true);
            btnScores.setManaged(true);
        }
        if (btnLogout != null) {
            btnLogout.setVisible(true);
            btnLogout.setManaged(true);
        }
        System.out.println("Forced visibility for essential buttons");
    }

    @FXML
    private void onStart(ActionEvent event) {
        System.out.println("[HomeController] Start button clicked");
        
        // Vérifier qu'un thème est sélectionné
        Theme selectedTheme = themeCombo.getSelectionModel().getSelectedItem();
        if (selectedTheme == null) {
            System.err.println("Aucun thème sélectionné!");
            return;
        }

        // Stocker les données sélectionnées dans les propriétés système
        System.setProperty("selectedThemeId", String.valueOf(selectedTheme.getId()));
        
        String gridSize = gridSizeCombo.getSelectionModel().getSelectedItem();
        if (gridSize != null) {
            System.setProperty("gridSize", gridSize);
        }
        
        // Utiliser l'utilisateur actuel (connecté ou invité)
        if (currentUser != null) {
            System.setProperty("userId", String.valueOf(currentUser.getId()));
            System.setProperty("playerName", currentUser.getFullName());
        }

        // S'assurer que le changement de scène se produit sur le thread FX
        Platform.runLater(() -> SceneManager.show("game"));
    }

    @FXML
    private void onShowScores(ActionEvent event) {
        System.out.println("[HomeController] High Scores button clicked");
        Platform.runLater(() -> SceneManager.show("scoreboard"));
    }

    @FXML
    private void onLogout(ActionEvent event) {
        if (isGuestMode) {
            // En mode invité, aller à la connexion
            SceneManager.show("login");
        } else {
            // Déconnexion normale
            authService.logout();
            
            // Nettoyer les propriétés système
            System.clearProperty("currentUserId");
            System.clearProperty("currentUserName");
            System.clearProperty("isGuest");
            
            System.out.println("Déconnexion réussie");
            SceneManager.show("login");
        }
    }

    @FXML
    private void onProfile(ActionEvent event) {
        // TODO: Implémenter la vue de profil utilisateur
        System.out.println("Profil utilisateur - À implémenter");
    }

    @FXML
    private void onAdminPanel(ActionEvent event) {
        if (currentUser != null && currentUser.isAdmin()) {
            SceneManager.show("user-management");
        } else {
            System.err.println("Accès refusé - utilisateur non administrateur");
        }
    }

    // Méthodes pour la gestion des utilisateurs (pour compatibilité - ne devraient plus être utilisées)
    @FXML
    public void onNewUser(ActionEvent event) {
        // Cette méthode ne devrait plus être appelée car les éléments sont masqués
        System.out.println("onNewUser appelé - ne devrait pas arriver");
    }

    @FXML
    public void onSaveUser(ActionEvent event) {
        // Cette méthode ne devrait plus être appelée car les éléments sont masqués
        System.out.println("onSaveUser appelé - ne devrait pas arriver");
    }

    @FXML
    public void onCancelUser(ActionEvent event) {
        // Cette méthode ne devrait plus être appelée car les éléments sont masqués
        System.out.println("onCancelUser appelé - ne devrait pas arriver");
    }

    @FXML
    public void onManagePlayers(ActionEvent event) {
        // Cette méthode ne devrait plus être appelée car les éléments sont masqués
        System.out.println("onManagePlayers appelé - ne devrait pas arriver");
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

    private void redirectToLogin() {
        System.out.println("Redirection vers la connexion");
        Platform.runLater(() -> SceneManager.show("login"));
    }
}