package com.myapp.controllers;

import com.myapp.models.Card;
import com.myapp.models.Score;
import com.myapp.models.Theme;
import com.myapp.services.GameService;
import com.myapp.services.ScoreService;
import com.myapp.services.ThemeService;
import com.myapp.utils.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import javafx.event.ActionEvent;
import com.myapp.models.User;
import com.myapp.services.UserService;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class GameController {
    @FXML
    private GridPane grid;

    @FXML
    private Label lblAttempts;

    @FXML
    private Label lblTime;

    @FXML
    private Button btnBack;
    @FXML
    private Button btnNew;
    @FXML private Label lblPlayer;
    @FXML private Label lblTheme;
    @FXML private Button btnSaveAndQuit;
    private final GameService gameService = new GameService();
    private final ThemeService themeService = new ThemeService();
    private final ScoreService scoreService = new ScoreService();
    private final UserService userService = new UserService();
    private User currentUser;
    private Theme currentTheme;
    private Button[] buttons;
    private int currentThemeId;
    private String currentThemeName;
    private int gridRows;
    private int gridCols;
    private Image cardBackImage; // Nouvelle variable
    @FXML
    public void initialize() {
        loadCardBackImage();
        
        // Récupérer l'utilisateur depuis les propriétés système
        String userIdStr = System.getProperty("userId");
        String isGuestStr = System.getProperty("isGuest");
        boolean isGuest = "true".equals(isGuestStr);
        
        if (!isGuest && userIdStr != null) {
            try {
                int userId = Integer.parseInt(userIdStr);
                currentUser = userService.getUser(userId);
                if (currentUser == null) {
                    System.err.println("Utilisateur non trouvé avec l'ID: " + userId);
                    // Rediriger vers la connexion
                    Platform.runLater(() -> SceneManager.show("login"));
                    return;
                }
            } catch (NumberFormatException e) {
                System.err.println("ID utilisateur invalide: " + userIdStr);
                Platform.runLater(() -> SceneManager.show("login"));
                return;
            }
        } else if (isGuest) {
            // Mode invité
            currentUser = new User("Invité", "Anonyme");
            currentUser.setId(-1);
        } else {
            System.err.println("Aucun utilisateur connecté");
            Platform.runLater(() -> SceneManager.show("login"));
            return;
        }
        
        // Lire le thème sélectionné et créer l'objet Theme
        currentThemeId = Integer.parseInt(System.getProperty("selectedThemeId", "4"));
        currentTheme = themeService.getTheme(currentThemeId);
        if (currentTheme == null) {
            // Créer un thème par défaut si non trouvé
            currentTheme = new Theme(currentThemeId, "Numbers");
        }
        currentThemeName = currentTheme.getName();

        // Lire la taille de grille sélectionnée (4x4, 5x5, 6x6)
        String gridSizeStr = System.getProperty("gridSize", "4x4");
        parseGridSize(gridSizeStr);

        // Calculer le nombre de paires basé sur la taille de grille : 4x4=8, 5x5=12, 6x6=18
        int pairCount = (gridRows * gridCols) / 2;

        // Construire les valeurs basées sur le thème
        List<String> values;
        if ("Colors".equalsIgnoreCase(currentThemeName)) {
            values = buildColorValues(pairCount);
        } else if ("Animals".equalsIgnoreCase(currentThemeName) ||
                "Images".equalsIgnoreCase(currentThemeName)) {
            values = buildAnimalValues(pairCount);
        } else {
            values = GameService.buildNumericValues(pairCount);
        }
        
        // Démarrer le jeu avec le thème
        gameService.startNewGame(values, currentTheme);
        setupGrid();

        // Démarrer la mise à jour du timer
        Thread timer = new Thread(() -> {
            while (!gameService.isFinished()) {
                Platform.runLater(() -> lblTime.setText("Time: " + gameService.getElapsedSeconds() + "s"));
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }, "game-timer");
        timer.setDaemon(true);
        timer.start();
    }

    private void parseGridSize(String gridSizeStr) {
        String[] parts = gridSizeStr.split("x");
        gridRows = Integer.parseInt(parts[0]);
        gridCols = Integer.parseInt(parts[1]);
    }

    private List<String> buildColorValues(int pairCount) {
        // Liste COMPLÈTE de couleurs avec leurs codes hex
        Map<String, String> colorMap = Map.ofEntries(
                Map.entry("red", "#FF6B6B"),
                Map.entry("green", "#51CF66"),
                Map.entry("blue", "#4ECDC4"),
                Map.entry("yellow", "#FFE66D"),
                Map.entry("orange", "#FFA500"),
                Map.entry("purple", "#800080"),
                Map.entry("pink", "#FFC0CB"),
                Map.entry("cyan", "#00FFFF"),
                Map.entry("lime", "#00FF00"),
                Map.entry("magenta", "#FF00FF"),
                Map.entry("brown", "#A52A2A"),
                Map.entry("gray", "#808080"),
                Map.entry("maroon", "#800000"),
                Map.entry("navy", "#000080"),
                Map.entry("teal", "#008080"),
                Map.entry("violet", "#EE82EE")
        );

        // Convertir en liste
        List<String> availableColors = new ArrayList<>(colorMap.keySet());

        // S'assurer qu'on a assez de couleurs uniques
        if (pairCount > availableColors.size()) {
            System.err.println("Pas assez de couleurs uniques! Répétition nécessaire.");
        }

        List<String> result = new ArrayList<>();
        for (int i = 0; i < pairCount; i++) {
            result.add(availableColors.get(i % availableColors.size()));
        }
        return result;
    }

//    private List<String> buildAnimalValues(int pairCount) {
//        List<String> animals = List.of("cat","dog","cow","hamester","lion","tiger","bear","elephant","monkey","rabbit");
//        List<String> result = new ArrayList<>();
//        for (int i = 0; i < pairCount; i++) {
//            result.add(animals.get(i % animals.size()));
//        }
//        return result;
//    }

    private List<String> buildAnimalValues(int pairCount) {
        // Liste alignée sur vos fichiers image (.png)
        List<String> animals = List.of("bear", "cat", "cow", "deer", "dog",
                "elephant", "fox", "giraffe", "goat",
                "hamster", "lion", "penguin", "pig",
                "rabbit", "sheep", "tiger", "wolf");
        List<String> result = new ArrayList<>();
        for (int i = 0; i < pairCount; i++) {
            result.add(animals.get(i % animals.size()));
        }
        return result;
    }

    private void setupGrid() {
        grid.getChildren().clear();
        List<Card> deck = gameService.getDeck();
        int size = deck.size();
        
        // Utiliser les dimensions de grille sélectionnées
        int rows = gridRows;
        int cols = gridCols;
        buttons = new Button[size];
        
        // Configurer l'espacement optimal - FORCER l'espacement dès le début
        double spacing = 25; // Espacement constant entre les cartes
        
        // IMPORTANT: Appliquer l'espacement AVANT d'ajouter les cartes
        grid.setHgap(spacing);
        grid.setVgap(spacing);
        grid.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Forcer la mise à jour du layout pour appliquer l'espacement immédiatement
        grid.autosize();
        grid.requestLayout();
        
        // Créer et placer les cartes
        int index = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (index >= size) break;
                Button b = createCardButton(index);
                
                // Ajouter une marge supplémentaire à chaque carte pour garantir l'espacement
                GridPane.setMargin(b, new Insets(5, 5, 5, 5));
                
                grid.add(b, c, r);
                buttons[index] = b;
                index++;
            }
        }
        
        // Forcer une nouvelle mise à jour du layout après ajout des cartes
        grid.autosize();
        grid.requestLayout();
        
        // Réinitialiser les labels
        lblAttempts.setText("Attempts: 0");
        lblTime.setText("Time: 0s");
    }

//    private Button createCardButton(int idx) {
//        Button b = new Button();
//        b.setPrefSize(100, 100);
//        b.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
//        b.getStyleClass().add("card-button");
//        b.setText("?");
//        b.setOnAction(evt -> onCardClicked(idx));
//        return b;
//    }

    private Button createCardButton(int idx) {
        Button b = new Button();
        
        // Taille adaptative selon la grille pour optimiser l'espace écran
        int cardWidth = 120;
        int cardHeight = 120; // Cartes carrées pour un meilleur rendu
        
        // Ajuster la taille selon la grille
        if (gridRows >= 6 || gridCols >= 6) {
            cardWidth = 100;
            cardHeight = 100;
        } else if (gridRows >= 5 || gridCols >= 5) {
            cardWidth = 110;
            cardHeight = 110;
        }
        
        b.setPrefSize(cardWidth, cardHeight);
        b.setMinSize(cardWidth, cardHeight);
        b.setMaxSize(cardWidth, cardHeight);
        
        // Ajouter les classes CSS pour une meilleure visibilité
        b.getStyleClass().addAll("card-button", "face-down");

        if (cardBackImage != null) {
            ImageView backView = new ImageView(cardBackImage);
            // L'image doit remplir COMPLÈTEMENT la carte comme une couverture
            backView.setFitWidth(cardWidth); // Remplir toute la largeur
            backView.setFitHeight(cardHeight); // Remplir toute la hauteur
            backView.setPreserveRatio(false); // IMPORTANT: Ne pas préserver le ratio pour remplir exactement
            backView.setSmooth(true); // Améliorer la qualité
            
            // Créer un clip rectangulaire avec coins arrondis pour l'image
            Rectangle clip = new Rectangle(cardWidth, cardHeight);
            clip.setArcWidth(24); // Coins arrondis comme la carte (12px radius * 2)
            clip.setArcHeight(24);
            backView.setClip(clip);
            
            // Configurer le bouton pour afficher seulement l'image
            b.setGraphic(backView);
            b.setText("");
            b.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
            
            // Forcer le bouton à ne pas avoir de padding
            b.setStyle("-fx-padding: 0; -fx-background-insets: 0; -fx-border-insets: 0;");
        } else {
            b.setText("?");
            b.setStyle("-fx-font-size: " + (cardWidth / 4) + "px; -fx-font-weight: bold;");
        }

        b.setOnAction(evt -> onCardClicked(idx));
        return b;
    }

    public void setGameData(User user, Theme theme) {
        this.currentUser = user;
        this.currentTheme = theme;
        lblPlayer.setText("Player: " + user.getFullName());
        lblTheme.setText("Theme: " + theme.getName());
    }

    public void onSaveAndQuit(ActionEvent event) {
        if (gameService != null && gameService.isFinished()) {
            // Créer et sauvegarder le score
            Score score = new Score(currentUser, currentTheme,
                    gameService.getAttempts(),
                    gameService.getElapsedSeconds());
            scoreService.saveScore(score);
        }
        SceneManager.show("home");
    }
    private void onCardClicked(int idx) {
        List<Card> deck = gameService.getDeck();
        Card c = deck.get(idx);
        Button b = buttons[idx];
        // DEBUG: Afficher la valeur de la carte
        System.out.println("Carte cliquée - Index: " + idx +
                ", Valeur: " + c.getValue() +
                ", Matché: " + c.isMatched());
        if (c.isMatched()) return;
        // disable while animating
        b.setDisable(true);
        // animate flip to reveal
        flipReveal(b, () -> {
            // set content after half flip
            if ("Colors".equalsIgnoreCase(currentThemeName)) {
                b.setStyle("-fx-base: " + getColorHex(c.getValue()) + "; -fx-text-fill: white; -fx-font-size: 0;");
                Rectangle rect = createColorRectangle(c.getValue());
                b.setGraphic(rect);
            } else if ("Animals".equalsIgnoreCase(currentThemeName) || "Images".equalsIgnoreCase(currentThemeName)) {
                b.setText("");
                ImageView iv = loadAnimalImage(c.getValue());
                if (iv != null) {
                    b.setGraphic(iv);
                    // Forcer le bouton à ne pas avoir de padding pour que l'image remplisse complètement
                    b.setStyle("-fx-padding: 0; -fx-background-insets: 0; -fx-border-insets: 0;");
                    b.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
                } else {
                    b.setText(c.getValue());
                }
            } else {
                b.setText(c.getValue());
            }
        });

        int result = gameService.flipCard(idx);
        if (result == 1) {
            // matched - leave revealed
            lblAttempts.setText("Attempts: " + gameService.getAttempts());
            
            // Marquer toutes les cartes appariées avec la classe CSS appropriée
            for (int i = 0; i < deck.size(); i++) {
                if (deck.get(i).isMatched()) {
                    Button matchedButton = buttons[i];
                    matchedButton.getStyleClass().removeAll("flipped", "face-down");
                    matchedButton.getStyleClass().add("matched");
                }
            }
            
            if (gameService.isFinished()) onGameFinished();
            b.setDisable(false);
        } else if (result == -1) {
            lblAttempts.setText("Attempts: " + gameService.getAttempts());
            // mismatch: hide non-matched cards after short pause with flip animation
            new Thread(() -> {
                try { Thread.sleep(700); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    for (int i = 0; i < deck.size(); i++) {
                        if (!deck.get(i).isMatched()) {
                            Button btn = buttons[i];
                            btn.setDisable(true);
                            flipHide(btn);
                        }
                    }
                });
            }).start();
        } else {
            // first selection, re-enable button
            b.setDisable(false);
        }
    }

    private void flipReveal(Button b, Runnable onHalfShown) {
        RotateTransition rt1 = new RotateTransition(Duration.millis(160), b);
        rt1.setAxis(Rotate.Y_AXIS);
        rt1.setFromAngle(0);
        rt1.setToAngle(90);
        RotateTransition rt2 = new RotateTransition(Duration.millis(160), b);
        rt2.setAxis(Rotate.Y_AXIS);
        rt2.setFromAngle(90);
        rt2.setToAngle(0);
        rt1.setOnFinished(e -> {
            // Enlever l'image de dos avant d'afficher le contenu
            b.setGraphic(null);
            b.setText("");
            // Changer la classe CSS pour carte retournée TOUT EN GARDANT LES COINS ARRONDIS
            b.getStyleClass().removeAll("face-down");
            b.getStyleClass().add("flipped");
            
            // IMPORTANT: Forcer le maintien des coins arrondis
            String currentStyle = b.getStyle();
            if (!currentStyle.contains("-fx-background-radius")) {
                b.setStyle(currentStyle + "; -fx-background-radius: 12px; -fx-border-radius: 12px;");
            }
            
            try { onHalfShown.run(); } catch (Exception ex) { }
            rt2.play();
        });
        rt2.setOnFinished(e -> b.setDisable(false));
        rt1.play();
    }

//    private void flipHide(Button b) {
//        RotateTransition rt1 = new RotateTransition(Duration.millis(160), b);
//        rt1.setAxis(Rotate.Y_AXIS);
//        rt1.setFromAngle(0);
//        rt1.setToAngle(90);
//        RotateTransition rt2 = new RotateTransition(Duration.millis(160), b);
//        rt2.setAxis(Rotate.Y_AXIS);
//        rt2.setFromAngle(90);
//        rt2.setToAngle(0);
//        rt1.setOnFinished(e -> {
//            // hide content
//            b.setText("?");
//            b.setGraphic(null);
//            b.setStyle("-fx-base: #f4f4f4;");
//            rt2.play();
//        });
//        rt2.setOnFinished(e -> b.setDisable(false));
//        rt1.play();
//    }

    private void flipHide(Button b) {
        RotateTransition rt1 = new RotateTransition(Duration.millis(160), b);
        rt1.setAxis(Rotate.Y_AXIS);
        rt1.setFromAngle(0);
        rt1.setToAngle(90);

        RotateTransition rt2 = new RotateTransition(Duration.millis(160), b);
        rt2.setAxis(Rotate.Y_AXIS);
        rt2.setFromAngle(90);
        rt2.setToAngle(0);

        rt1.setOnFinished(e -> {
            b.setText("");
            // Remettre la classe CSS pour carte face cachée
            b.getStyleClass().removeAll("flipped", "matched");
            b.getStyleClass().add("face-down");
            
            if (cardBackImage != null) {
                ImageView backView = new ImageView(cardBackImage);
                // Utiliser les mêmes dimensions que dans createCardButton
                int cardSize = 120; // Cartes carrées
                if (gridRows >= 6 || gridCols >= 6) {
                    cardSize = 100;
                } else if (gridRows >= 5 || gridCols >= 5) {
                    cardSize = 110;
                }
                
                backView.setFitWidth(cardSize);
                backView.setFitHeight(cardSize);
                backView.setPreserveRatio(false); // Forcer l'image à remplir exactement l'espace
                backView.setSmooth(true);
                
                // IMPORTANT: Maintenir les coins arrondis pour l'image de dos
                Rectangle clip = new Rectangle(cardSize, cardSize);
                clip.setArcWidth(24); // Coins arrondis comme la carte
                clip.setArcHeight(24);
                backView.setClip(clip);
                
                b.setGraphic(backView);
            } else {
                b.setText("?");
                b.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
            }

            // IMPORTANT: Forcer le maintien des coins arrondis même après reset
            String currentStyle = b.getStyle();
            if (!currentStyle.contains("-fx-background-radius")) {
                b.setStyle(currentStyle + "; -fx-background-radius: 12px; -fx-border-radius: 12px;");
            }

            rt2.play();
        });

        rt2.setOnFinished(e -> b.setDisable(false));
        rt1.play();
    }


    private Rectangle createColorRectangle(String colorName) {
        // Taille adaptée aux cartes carrées
        int rectSize = 110;
        if (gridRows >= 6 || gridCols >= 6) {
            rectSize = 90;
        } else if (gridRows >= 5 || gridCols >= 5) {
            rectSize = 100;
        }
        
        Rectangle rect = new Rectangle(rectSize, rectSize);
        rect.setFill(Color.web(getColorHex(colorName)));
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(4); // Bordure plus épaisse pour la visibilité
        
        // IMPORTANT: Ajouter des coins arrondis au rectangle de couleur
        rect.setArcWidth(24); // Coins arrondis comme la carte
        rect.setArcHeight(24);
        
        return rect;
    }
    private ImageView loadAnimalImage(String animalName) {
        try {
            // Chemin direct vers votre dossier d'images
            String imagePath = "/images/animals/" + animalName.toLowerCase() + ".png";
            var resource = getClass().getResource(imagePath);

            if (resource == null) {
                System.err.println("Image non trouvée : " + imagePath);
                return null;
            }

            Image img = new Image(resource.toExternalForm());
            ImageView iv = new ImageView(img);
            
            // Taille exacte de la carte pour remplir complètement comme cardback.png
            int cardWidth = 120;
            int cardHeight = 120;
            
            // Ajuster selon la grille (même logique que createCardButton)
            if (gridRows >= 6 || gridCols >= 6) {
                cardWidth = 100;
                cardHeight = 100;
            } else if (gridRows >= 5 || gridCols >= 5) {
                cardWidth = 110;
                cardHeight = 110;
            }
            
            // IMPORTANT: Remplir exactement la carte comme l'image de dos
            iv.setFitWidth(cardWidth);
            iv.setFitHeight(cardHeight);
            iv.setPreserveRatio(false); // Ne pas préserver le ratio pour remplir exactement
            iv.setSmooth(true);
            
            // Créer un clip rectangulaire avec coins arrondis pour l'image
            Rectangle clip = new Rectangle(cardWidth, cardHeight);
            clip.setArcWidth(24); // Coins arrondis comme la carte (12px radius * 2)
            clip.setArcHeight(24);
            iv.setClip(clip);
            
            return iv;
        } catch (Exception e) {
            System.err.println("Erreur de chargement: " + e.getMessage());
            return null;
        }
    }
    private String getColorHex(String colorName) {
        // Tableau complet de correspondances
        return switch (colorName.toLowerCase()) {
            case "red" -> "#FF6B6B";
            case "green" -> "#51CF66";
            case "blue" -> "#4ECDC4";
            case "yellow" -> "#FFE66D";
            case "orange" -> "#FFA500";
            case "purple" -> "#800080";
            case "pink" -> "#FFC0CB";
            case "cyan" -> "#00FFFF";
            case "lime" -> "#00FF00";
            case "magenta" -> "#FF00FF";
            case "brown" -> "#A52A2A";
            case "gray", "grey" -> "#808080";
            case "maroon" -> "#800000";
            case "navy" -> "#000080";
            case "teal" -> "#008080";
            case "violet" -> "#EE82EE";
            default -> {
                System.err.println("Couleur non reconnue: " + colorName);
                yield "#FFFFFF"; // Blanc pur par défaut
            }
        };
    }

    private void onGameFinished() {
        if (currentUser == null || currentTheme == null) {
            System.err.println("Erreur: Utilisateur ou thème manquant pour sauvegarder le score");
            return;
        }
        
        int time = gameService.getElapsedSeconds();
        int attempts = gameService.getAttempts();
        
        // Vérifier si l'utilisateur est un invité
        String isGuestStr = System.getProperty("isGuest");
        boolean isGuest = "true".equals(isGuestStr);
        
        if (!isGuest && currentUser.getId() != -1) {
            // Créer et sauvegarder le score seulement pour les utilisateurs authentifiés
            Score score = new Score(currentUser, currentTheme, attempts, time);
            boolean saved = scoreService.saveScore(score);
            
            if (saved) {
                System.out.println("Score sauvegardé: " + score);
            } else {
                System.err.println("Erreur lors de la sauvegarde du score");
            }
            
            // Afficher le tableau des scores après un court délai pour les utilisateurs authentifiés
            new Thread(() -> {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> SceneManager.show("scoreboard"));
            }).start();
        } else {
            // Pour les invités, afficher un message et retourner à l'accueil
            System.out.println("Jeu terminé en mode invité - pas de sauvegarde de score");
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> SceneManager.show("home"));
            }).start();
        }
    }

    @FXML
    private void onBack() {
        SceneManager.show("home");
    }

    @FXML
    private void onNewGame() {
        // Redémarrer avec le même thème et la même taille de grille
        int pairCount = (gridRows * gridCols) / 2;
        
        List<String> values;
        if ("Colors".equalsIgnoreCase(currentThemeName)) {
            values = buildColorValues(pairCount);
        } else if ("Animals".equalsIgnoreCase(currentThemeName) || "Images".equalsIgnoreCase(currentThemeName)) {
            values = buildAnimalValues(pairCount);
        } else {
            values = GameService.buildNumericValues(pairCount);
        }
        
        // Utiliser la nouvelle méthode avec le thème
        gameService.startNewGame(values, currentTheme);
        setupGrid();
    }

    private void loadCardBackImage() {
        try {
            String backPath = "/images/cardback.png";
            var resource = getClass().getResource(backPath);
            if (resource != null) {
                cardBackImage = new Image(resource.toExternalForm());
                System.out.println("Image de dos cardback.png chargée avec succès");
            } else {
                System.err.println("ATTENTION: Image de dos non trouvée à " + backPath);
                // Fallback: essayer l'ancienne image
                backPath = "/images/c.png";
                resource = getClass().getResource(backPath);
                if (resource != null) {
                    cardBackImage = new Image(resource.toExternalForm());
                    System.out.println("Fallback: Image c.png chargée");
                } else {
                    cardBackImage = createDefaultBackImage();
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image de dos: " + e.getMessage());
            cardBackImage = createDefaultBackImage();
        }
    }
    private Image createDefaultBackImage() {
        // Créer une image de dos par défaut (carré bleu avec point d'interrogation)
        System.out.println("Utilisation de l'image de dos par défaut");
        // Note: Vous pouvez créer une image programmatiquement ou utiliser une ressource par défaut
        return null; // Retourner null pour utiliser le fallback texte
    }
}
