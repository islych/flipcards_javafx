package com.myapp.controllers;

import com.myapp.models.Score;
import com.myapp.models.User;
import com.myapp.models.Theme;
import com.myapp.services.ScoreService;
import com.myapp.services.ThemeService;
import com.myapp.services.UserService;
import com.myapp.utils.SceneManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScoreboardController {
    @FXML
    private TableView<Score> table;

    @FXML
    private TableColumn<Score, String> colPlayer;

    @FXML
    private TableColumn<Score, Integer> colAttempts;

    @FXML
    private TableColumn<Score, Integer> colTime;

    @FXML
    private TableColumn<Score, String> colDate;

    @FXML
    private Button btnBack;

    private final ScoreService scoreService = new ScoreService();
    private final ThemeService themeService = new ThemeService();
    private final UserService userService = new UserService();
    
    @FXML 
    private ComboBox<String> sortCombo;
    @FXML 
    private ComboBox<User> playerFilterCombo;
    @FXML 
    private ComboBox<Theme> themeFilterCombo;
    @FXML 
    private Button btnDelete;
    @FXML 
    private TableColumn<Score, String> colTheme;
    @FXML
    private Label displayModeLabel;

    private ObservableList<Score> allScores;

    @FXML
    public void initialize() {
        initializeColumns();
        initializeFilters();
        checkAdminPermissions();
        loadScores("date");
    }

    private void checkAdminPermissions() {
        // Vérifier si l'utilisateur actuel est un administrateur
        String currentUserIdStr = System.getProperty("currentUserId");
        boolean isAdmin = false;
        
        if (currentUserIdStr != null && !currentUserIdStr.equals("-1")) {
            try {
                int userId = Integer.parseInt(currentUserIdStr);
                User currentUser = userService.getUser(userId);
                if (currentUser != null) {
                    isAdmin = currentUser.isAdmin();
                }
            } catch (NumberFormatException e) {
                // Utilisateur invalide, pas d'admin
            }
        }
        
        // Masquer le bouton de suppression si l'utilisateur n'est pas admin
        if (btnDelete != null) {
            btnDelete.setVisible(isAdmin);
            btnDelete.setManaged(isAdmin);
        }
        
        // Les admins peuvent voir tous les scores, même sans avoir joué
        if (isAdmin) {
            System.out.println("Mode administrateur activé - accès complet aux scores");
        }
        
        System.out.println("Permissions admin: " + isAdmin);
    }

    private void initializeColumns() {
        colPlayer.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUser().getFullName()));
        colTheme.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTheme().getName()));
        colAttempts.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getAttempts()).asObject());
        colTime.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTimeSeconds()).asObject());
        colDate.setCellValueFactory(cellData -> {
            LocalDateTime playedAt = cellData.getValue().getPlayedAt();
            if (playedAt != null) {
                return new SimpleStringProperty(playedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            } else {
                return new SimpleStringProperty("");
            }
        });
    }

    private void initializeFilters() {
        // Initialiser le ComboBox de tri
        if (sortCombo != null) {
            sortCombo.setItems(FXCollections.observableArrayList(
                "Date (récent d'abord)", 
                "Date (ancien d'abord)", 
                "Meilleur score", 
                "Temps le plus rapide"
            ));
            sortCombo.getSelectionModel().select(0);
            sortCombo.setOnAction(e -> applyFilters());
        }

        // Charger les utilisateurs pour le filtre
        if (playerFilterCombo != null) {
            List<User> users = userService.getAllUsers();
            users.add(0, createAllUsersOption());
            playerFilterCombo.setItems(FXCollections.observableArrayList(users));
            playerFilterCombo.getSelectionModel().select(0);
            playerFilterCombo.setOnAction(e -> applyFilters());
        }

        // Charger les thèmes pour le filtre
        if (themeFilterCombo != null) {
            List<Theme> themes = themeService.getAllThemes();
            themes.add(0, createAllThemesOption());
            themeFilterCombo.setItems(FXCollections.observableArrayList(themes));
            themeFilterCombo.getSelectionModel().select(0);
            themeFilterCombo.setOnAction(e -> applyFilters());
        }
    }

    private User createAllUsersOption() {
        User allUsers = new User("Tous les joueurs", "(meilleur score seulement)");
        allUsers.setId(-1);
        return allUsers;
    }

    private Theme createAllThemesOption() {
        Theme allThemes = new Theme(-1, "Tous les thèmes");
        return allThemes;
    }

    private void loadScores(String orderBy) {
        List<Score> scores = scoreService.listScoresBy(orderBy);
        allScores = FXCollections.observableArrayList(scores);
        System.out.println("Loaded " + scores.size() + " scores from database.");
        if (scores.isEmpty()) {
            System.out.println("WARNING: No scores found in database. Check DB connection or insert test data.");
        }
        applyFilters();
    }

    private void applyFilters() {
        if (allScores == null) return;

        List<Score> filteredScores;
        
        // Vérifier si un utilisateur spécifique est sélectionné
        User selectedUser = playerFilterCombo != null ? playerFilterCombo.getSelectionModel().getSelectedItem() : null;
        boolean isSpecificUserSelected = selectedUser != null && selectedUser.getId() != -1;
        
        if (isSpecificUserSelected) {
            // Si un utilisateur spécifique est sélectionné, afficher TOUS ses scores
            filteredScores = allScores.stream()
                .filter(score -> score.getUser().getId() == selectedUser.getId())
                .filter(this::matchesThemeFilter)
                .collect(Collectors.toList());
            System.out.println("Affichage de tous les scores pour: " + selectedUser.getFullName() + " (" + filteredScores.size() + " scores)");
            
            // Mettre à jour le label d'affichage
            if (displayModeLabel != null) {
                displayModeLabel.setText("📋 Affichage : Tous les scores de " + selectedUser.getFullName() + " (" + filteredScores.size() + " scores)");
            }
        } else {
            // Si "Tous les joueurs" est sélectionné, afficher seulement le MEILLEUR score par utilisateur
            filteredScores = getBestScorePerUser(allScores.stream()
                .filter(this::matchesThemeFilter)
                .collect(Collectors.toList()));
            System.out.println("Affichage du meilleur score par utilisateur (" + filteredScores.size() + " utilisateurs)");
            
            // Mettre à jour le label d'affichage
            if (displayModeLabel != null) {
                displayModeLabel.setText("📊 Affichage : Meilleur score par utilisateur (" + filteredScores.size() + " utilisateurs)");
            }
        }

        // Appliquer le tri
        if (sortCombo != null) {
            String sortOption = sortCombo.getSelectionModel().getSelectedItem();
            if (sortOption != null) {
                switch (sortOption) {
                    case "Date (récent d'abord)":
                        filteredScores.sort((s1, s2) -> s2.getPlayedAt().compareTo(s1.getPlayedAt()));
                        break;
                    case "Date (ancien d'abord)":
                        filteredScores.sort((s1, s2) -> s1.getPlayedAt().compareTo(s2.getPlayedAt()));
                        break;
                    case "Meilleur score":
                        filteredScores.sort((s1, s2) -> {
                            int attemptsCompare = Integer.compare(s1.getAttempts(), s2.getAttempts());
                            if (attemptsCompare == 0) {
                                return Integer.compare(s1.getTimeSeconds(), s2.getTimeSeconds());
                            }
                            return attemptsCompare;
                        });
                        break;
                    case "Temps le plus rapide":
                        filteredScores.sort((s1, s2) -> Integer.compare(s1.getTimeSeconds(), s2.getTimeSeconds()));
                        break;
                }
            }
        }

        table.setItems(FXCollections.observableArrayList(filteredScores));
    }

    /**
     * Retourne le meilleur score pour chaque utilisateur
     * Le meilleur score = moins d'essais, puis moins de temps en cas d'égalité
     */
    private List<Score> getBestScorePerUser(List<Score> scores) {
        return scores.stream()
            .collect(Collectors.groupingBy(
                score -> score.getUser().getId(),
                Collectors.minBy((s1, s2) -> {
                    // Comparer d'abord par nombre d'essais (moins = mieux)
                    int attemptsCompare = Integer.compare(s1.getAttempts(), s2.getAttempts());
                    if (attemptsCompare != 0) {
                        return attemptsCompare;
                    }
                    // En cas d'égalité d'essais, comparer par temps (moins = mieux)
                    return Integer.compare(s1.getTimeSeconds(), s2.getTimeSeconds());
                })
            ))
            .values()
            .stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    private boolean matchesThemeFilter(Score score) {
        if (themeFilterCombo == null) return true;
        
        Theme selectedTheme = themeFilterCombo.getSelectionModel().getSelectedItem();
        if (selectedTheme == null || selectedTheme.getId() == -1) {
            return true; // "Tous les thèmes" sélectionné
        }
        return score.getTheme().getId() == selectedTheme.getId();
    }

    @FXML
    private void onBack() { 
        SceneManager.show("home"); 
    }

    @FXML
    private void onDeleteScore() {
        Score selectedScore = table.getSelectionModel().getSelectedItem();
        if (selectedScore != null) {
            if (scoreService.deleteScore(selectedScore.getId())) {
                // Recharger les scores après suppression
                loadScores("date");
                System.out.println("Score supprimé avec succès");
            } else {
                System.err.println("Erreur lors de la suppression du score");
            }
        } else {
            System.out.println("Aucun score sélectionné pour la suppression");
        }
    }
}