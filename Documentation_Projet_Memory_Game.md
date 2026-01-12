# Documentation Complète - Projet Memory Game

## Table des Matières
1. [Vue d'ensemble du projet](#vue-densemble-du-projet)
2. [Architecture générale](#architecture-générale)
3. [Structure des couches (MVC)](#structure-des-couches-mvc)
4. [Modèles de données](#modèles-de-données)
5. [Couche d'accès aux données (DAO)](#couche-daccès-aux-données-dao)
6. [Services métier](#services-métier)
7. [Contrôleurs et vues](#contrôleurs-et-vues)
8. [Base de données](#base-de-données)
9. [Système d'authentification](#système-dauthentification)
10. [Logique du jeu](#logique-du-jeu)
11. [Flux de données](#flux-de-données)
12. [Classes utilitaires](#classes-utilitaires)
13. [Configuration et déploiement](#configuration-et-déploiement)

---

## Vue d'ensemble du projet

### Description
Le **Memory Game** est une application JavaFX complète qui implémente le jeu de mémoire classique avec un système d'authentification, de gestion des utilisateurs et de sauvegarde des scores. L'application suit une architecture MVC (Model-View-Controller) avec une séparation claire des responsabilités.

### Technologies utilisées
- **JavaFX 21** : Interface utilisateur
- **MySQL 8.1** : Base de données
- **Java 23** : Langage de programmation
- **Maven** : Gestionnaire de dépendances
- **JUnit 5** : Tests unitaires

### Fonctionnalités principales
- Jeu de mémoire avec 4 thèmes (Images, Couleurs, Animaux, Nombres)
- Système d'authentification complet (connexion, inscription, mode invité)
- Gestion des utilisateurs avec rôles (USER, ADMIN)
- Sauvegarde et affichage des scores
- Interface d'administration pour la gestion des utilisateurs
- Animations et effets visuels

---

## Architecture générale

### Point d'entrée
```java
// Main.java - Point d'entrée de l'application
public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.initialize(stage);
        
        String currentUserId = System.getProperty("currentUserId");
        if (currentUserId != null && !currentUserId.equals("-1")) {
            SceneManager.show("home");
        } else {
            SceneManager.show("login");
        }
    }
}
```

### Structure des packages
```
com.myapp/
├── Main.java                    # Point d'entrée
├── config/                      # Configuration
│   └── MySQLConnection.java     # Connexion base de données
├── controllers/                 # Contrôleurs MVC
│   ├── LoginController.java
│   ├── RegisterController.java
│   ├── HomeController.java
│   ├── GameController.java
│   ├── ScoreboardController.java
│   └── UserManagementController.java
├── dao/                         # Couche d'accès aux données
│   ├── UserDAO.java
│   ├── UserAuthDAO.java
│   ├── ScoreDAO.java
│   └── ThemeDAO.java
├── models/                      # Modèles de données
│   ├── User.java
│   ├── Card.java
│   ├── Score.java
│   └── Theme.java
├── services/                    # Services métier
│   ├── AuthenticationService.java
│   ├── GameService.java
│   ├── UserService.java
│   ├── ScoreService.java
│   └── ThemeService.java
├── utils/                       # Classes utilitaires
│   ├── SceneManager.java
│   ├── PasswordUtils.java
│   └── UserSession.java
└── validation/                  # Validation des données
```

---

## Structure des couches (MVC)

### Modèle (Model)
Les modèles représentent les entités métier et leurs relations :

```java
// Exemple : User.java
public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private String passwordHash;
    private String email;
    private boolean isActive;
    private String role;
    private LocalDateTime lastLogin;
    private List<Score> scores;
    
    // Relations bidirectionnelles avec Score
    public void addScore(Score score) {
        if (score != null && !this.scores.contains(score)) {
            this.scores.add(score);
            score.setUser(this);
        }
    }
}
```

### Vue (View)
Les vues sont définies en FXML avec des contrôleurs associés :

```xml
<!-- login.fxml -->
<VBox xmlns="http://javafx.com/javafx/11.0.1" xmlns:fx="http://javafx.com/fxml/1" 
      fx:controller="com.myapp.controllers.LoginController">
    <TextField fx:id="usernameField" promptText="Nom d'utilisateur" />
    <PasswordField fx:id="passwordField" promptText="Mot de passe" />
    <Button fx:id="btnLogin" onAction="#onLogin" text="Se connecter" />
</VBox>
```

### Contrôleur (Controller)
Les contrôleurs gèrent les interactions utilisateur et coordonnent les services :

```java
// LoginController.java
@FXML
private void onLogin(ActionEvent event) {
    String username = usernameField.getText();
    String password = passwordField.getText();
    
    // Validation et authentification
    if (authService.login(username.trim(), password)) {
        User currentUser = authService.getCurrentUser();
        System.setProperty("currentUserId", String.valueOf(currentUser.getId()));
        SceneManager.show("home");
    } else {
        showError("Nom d'utilisateur ou mot de passe incorrect.");
    }
}
```

---

## Modèles de données

### User (Utilisateur)
```java
public class User {
    private int id;                    // Identifiant unique
    private String firstName;          // Prénom
    private String lastName;           // Nom de famille
    private String username;           // Nom d'utilisateur (unique)
    private String passwordHash;       // Mot de passe haché
    private String email;              // Email (unique, optionnel)
    private boolean isActive;          // Compte actif/inactif
    private String role;               // Rôle (USER, ADMIN)
    private LocalDateTime lastLogin;   // Dernière connexion
    private LocalDateTime createdAt;   // Date de création
    private List<Score> scores;        // Scores associés
    
    // Méthodes utilitaires
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
    public String getFullName() { return firstName + " " + lastName; }
}
```

### Card (Carte)
```java
public class Card {
    private int id;           // Identifiant unique
    private String value;     // Valeur de la carte
    private boolean matched;  // Carte appariée ou non
    private Theme theme;      // Thème associé
    
    // Relation bidirectionnelle avec Theme
    public void setTheme(Theme theme) {
        if (this.theme != null && this.theme.getCards().contains(this)) {
            this.theme.getCards().remove(this);
        }
        this.theme = theme;
        if (theme != null && !theme.getCards().contains(this)) {
            theme.getCards().add(this);
        }
    }
}
```

### Score (Score)
```java
public class Score {
    private int id;                    // Identifiant unique
    private User user;                 // Utilisateur
    private Theme theme;               // Thème du jeu
    private int attempts;              // Nombre de tentatives
    private int timeSeconds;           // Temps en secondes
    private LocalDateTime playedAt;    // Date/heure de la partie
    
    // Relations bidirectionnelles
    public void setUser(User user) {
        if (this.user != null && this.user.getScores().contains(this)) {
            this.user.getScores().remove(this);
        }
        this.user = user;
        if (user != null && !user.getScores().contains(this)) {
            user.getScores().add(this);
        }
    }
}
```

### Theme (Thème)
```java
public class Theme {
    private int id;              // Identifiant unique
    private String name;         // Nom du thème
    private List<Score> scores;  // Scores associés
    private List<Card> cards;    // Cartes associées
    
    // Gestion des relations
    public void addCard(Card card) {
        if (card != null && !this.cards.contains(card)) {
            this.cards.add(card);
            card.setTheme(this);
        }
    }
}
```

---

## Couche d'accès aux données (DAO)

### MySQLConnection
```java
public class MySQLConnection {
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DATABASE = "memory_game";
    private static final String USER = "root";
    private static final String PASSWORD = "2004";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

### UserAuthDAO
```java
public class UserAuthDAO {
    // Authentification par nom d'utilisateur
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Insertion d'un nouvel utilisateur
    public boolean insert(User user) {
        String sql = "INSERT INTO users (first_name, last_name, username, " +
                    "password_hash, email, role, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getRole() != null ? user.getRole() : "USER");
            ps.setBoolean(7, user.isActive());
            ps.setTimestamp(8, Timestamp.valueOf(user.getCreatedAt()));
            
            int affected = ps.executeUpdate();
            if (affected == 1) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
```

### ScoreDAO
```java
public class ScoreDAO {
    // Insertion d'un score avec relations
    public boolean insert(Score s) {
        String sql = "INSERT INTO scores (user_id, theme_id, attempts, time_seconds, played_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = MySQLConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getUser().getId());
            ps.setInt(2, s.getTheme().getId());
            ps.setInt(3, s.getAttempts());
            ps.setInt(4, s.getTimeSeconds());
            ps.setTimestamp(5, Timestamp.valueOf(s.getPlayedAt()));
            
            int affected = ps.executeUpdate();
            if (affected == 1) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) s.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Récupération avec jointures
    public List<Score> findAll(String orderBy) {
        List<Score> list = new ArrayList<>();
        String sql = "SELECT s.id, s.attempts, s.time_seconds, s.played_at, " +
                "u.id as user_id, u.first_name, u.last_name, " +
                "t.id as theme_id, t.name as theme_name " +
                "FROM scores s " +
                "INNER JOIN users u ON s.user_id = u.id " +
                "INNER JOIN themes t ON s.theme_id = t.id";
        
        if ("score".equalsIgnoreCase(orderBy))
            sql += " ORDER BY s.attempts ASC, s.time_seconds ASC";
        else
            sql += " ORDER BY s.played_at DESC";
        
        // Exécution de la requête et création des objets
        // ...
        return list;
    }
}
```

---

## Services métier

### AuthenticationService
```java
public class AuthenticationService {
    private final UserAuthDAO userAuthDAO;
    private User currentUser;
    
    // Connexion utilisateur
    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return false;
        }
        
        User user = userAuthDAO.findByUsername(username.trim());
        if (user == null || !user.isActive()) {
            return false;
        }
        
        if (PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            this.currentUser = user;
            user.updateLastLogin();
            userAuthDAO.updateLastLogin(user.getId());
            return true;
        }
        
        return false;
    }
    
    // Inscription utilisateur
    public boolean register(String firstName, String lastName, String username, 
                          String email, String password) {
        // Validation des données
        if (!isValidRegistrationData(firstName, lastName, username, email, password)) {
            return false;
        }
        
        // Vérifications d'unicité
        if (userAuthDAO.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (email != null && !email.trim().isEmpty() && userAuthDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Création et sauvegarde
        User newUser = new User(firstName.trim(), lastName.trim(), username.trim(), 
                               email != null ? email.trim() : null);
        newUser.setPasswordHash(PasswordUtils.hashPassword(password));
        
        return userAuthDAO.insert(newUser);
    }
}
```

### GameService
```java
public class GameService {
    private List<Card> deck = new ArrayList<>();
    private Card firstSelected = null;
    private int attempts = 0;
    private long startTime = 0L;
    private Theme currentTheme;
    
    // Démarrage d'une nouvelle partie
    public void startNewGame(List<String> values, Theme theme) {
        this.currentTheme = theme;
        deck.clear();
        List<String> pairedValues = new ArrayList<>();
        
        // Créer les paires
        for (String v : values) {
            pairedValues.add(v); // Première carte de la paire
            pairedValues.add(v); // Deuxième carte de la paire
        }
        
        // Mélanger toutes les cartes
        Collections.shuffle(pairedValues);
        
        // Création des cartes avec association au thème
        int id = 0;
        for (String v : pairedValues) {
            Card card = new Card(id++, v, theme);
            deck.add(card);
        }
        
        attempts = 0;
        firstSelected = null;
        startTime = System.currentTimeMillis();
    }
    
    // Logique de retournement de carte
    public int flipCard(int cardIndex) {
        if (cardIndex < 0 || cardIndex >= deck.size()) return 0;
        Card c = deck.get(cardIndex);
        if (c.isMatched()) return 0; // déjà appariée
        
        if (firstSelected == null) {
            firstSelected = c;
            return 0; // Première sélection
        } else if (firstSelected.getId() == c.getId()) {
            return 0; // Même carte cliquée deux fois
        } else {
            attempts++;
            if (firstSelected.getValue().equals(c.getValue())) {
                // Correspondance trouvée
                firstSelected.setMatched(true);
                c.setMatched(true);
                firstSelected = null;
                return 1;
            } else {
                // Pas de correspondance
                firstSelected = null;
                return -1;
            }
        }
    }
    
    // Vérification de fin de partie
    public boolean isFinished() {
        return deck.stream().allMatch(Card::isMatched);
    }
}
```

---

## Contrôleurs et vues

### GameController
```java
public class GameController {
    @FXML private GridPane grid;
    @FXML private Label lblAttempts;
    @FXML private Label lblTime;
    
    private final GameService gameService = new GameService();
    private Button[] buttons;
    private User currentUser;
    private Theme currentTheme;
    
    @FXML
    public void initialize() {
        // Récupération de l'utilisateur et du thème
        loadCurrentUser();
        loadSelectedTheme();
        
        // Configuration du jeu
        setupGame();
        setupGrid();
        startTimer();
    }
    
    private void onCardClicked(int idx) {
        List<Card> deck = gameService.getDeck();
        Card c = deck.get(idx);
        Button b = buttons[idx];
        
        if (c.isMatched()) return;
        
        // Animation de retournement
        b.setDisable(true);
        flipReveal(b, () -> {
            // Affichage du contenu selon le thème
            if ("Colors".equalsIgnoreCase(currentTheme.getName())) {
                b.setStyle("-fx-base: " + getColorHex(c.getValue()));
                Rectangle rect = createColorRectangle(c.getValue());
                b.setGraphic(rect);
            } else if ("Animals".equalsIgnoreCase(currentTheme.getName())) {
                ImageView iv = loadAnimalImage(c.getValue());
                if (iv != null) b.setGraphic(iv);
                else b.setText(c.getValue());
            } else {
                b.setText(c.getValue());
            }
        });
        
        // Logique de jeu
        int result = gameService.flipCard(idx);
        if (result == 1) {
            // Paire trouvée
            lblAttempts.setText("Attempts: " + gameService.getAttempts());
            if (gameService.isFinished()) onGameFinished();
            b.setDisable(false);
        } else if (result == -1) {
            // Pas de correspondance - masquer après délai
            lblAttempts.setText("Attempts: " + gameService.getAttempts());
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
            b.setDisable(false);
        }
    }
    
    private void onGameFinished() {
        // Sauvegarde du score
        Score score = new Score(currentUser, currentTheme, 
                               gameService.getAttempts(), 
                               gameService.getElapsedSeconds());
        scoreService.saveScore(score);
        
        // Redirection vers le tableau des scores
        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> SceneManager.show("scoreboard"));
        }).start();
    }
}
```

### UserManagementController (Admin)
```java
public class UserManagementController {
    @FXML private TableView<User> usersTable;
    @FXML private ComboBox<String> roleFilterCombo;
    
    private final UserService userService = new UserService();
    private final UserAuthDAO userAuthDAO = new UserAuthDAO();
    
    @FXML
    public void initialize() {
        // Vérification des droits admin
        if (!isCurrentUserAdmin()) {
            showError("Accès refusé. Seuls les administrateurs peuvent accéder à cette page.");
            onBack();
            return;
        }
        
        initializeColumns();
        initializeFilters();
        loadUsers();
    }
    
    @FXML
    private void onDeactivateUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur à désactiver.");
            return;
        }
        
        if (selectedUser.isAdmin()) {
            showError("Impossible de désactiver un administrateur.");
            return;
        }
        
        // Confirmation et désactivation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Désactiver l'utilisateur");
        confirmation.setContentText("Êtes-vous sûr de vouloir désactiver " + selectedUser.getFullName() + " ?");
        
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (userAuthDAO.deactivate(selectedUser.getId())) {
                showStatus("Utilisateur " + selectedUser.getFullName() + " désactivé avec succès.");
                loadUsers();
            } else {
                showError("Erreur lors de la désactivation de l'utilisateur.");
            }
        }
    }
}
```

---

## Base de données

### Schéma de la base de données
```sql
-- Création de la base de données
CREATE DATABASE IF NOT EXISTS memory_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE memory_game;

-- Table des thèmes
CREATE TABLE IF NOT EXISTS themes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

-- Table des utilisateurs avec authentification
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) UNIQUE,
    password_hash VARCHAR(255),
    email VARCHAR(100) UNIQUE,
    role VARCHAR(20) DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table des scores avec relations
CREATE TABLE IF NOT EXISTS scores (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  theme_id INT NOT NULL,
  attempts INT NOT NULL,
  time_seconds INT NOT NULL,
  played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (theme_id) REFERENCES themes(id) ON DELETE SET NULL
);

-- Index pour les performances
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_scores_user_id ON scores(user_id);
CREATE INDEX idx_scores_theme_id ON scores(theme_id);
CREATE INDEX idx_scores_played_at ON scores(played_at);

-- Vues utiles
CREATE OR REPLACE VIEW user_scores AS
SELECT 
    s.id,
    u.first_name,
    u.last_name,
    u.username,
    t.name as theme_name,
    s.attempts,
    s.time_seconds,
    s.played_at
FROM scores s
INNER JOIN users u ON s.user_id = u.id
INNER JOIN themes t ON s.theme_id = t.id
WHERE u.is_active = TRUE
ORDER BY s.played_at DESC;
```

### Relations entre tables
- **users** ↔ **scores** : Relation 1:N (un utilisateur peut avoir plusieurs scores)
- **themes** ↔ **scores** : Relation 1:N (un thème peut avoir plusieurs scores)
- **themes** ↔ **cards** : Relation 1:N (en mémoire uniquement)

---

## Système d'authentification

### Hachage des mots de passe
```java
public class PasswordUtils {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    // Hachage avec salt aléatoire
    public static String hashPassword(String password) {
        try {
            // Générer un salt aléatoire
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hacher le mot de passe avec le salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // Combiner salt et hash, puis encoder en Base64
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    // Vérification du mot de passe
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Décoder le hash stocké
            byte[] combined = Base64.getDecoder().decode(storedHash);
            
            // Extraire le salt et le hash original
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            
            byte[] originalHash = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, SALT_LENGTH, originalHash, 0, originalHash.length);
            
            // Hacher le mot de passe fourni avec le même salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] testHash = md.digest(password.getBytes());
            
            // Comparer les hashes
            return MessageDigest.isEqual(originalHash, testHash);
        } catch (Exception e) {
            return false;
        }
    }
}
```

### Gestion des sessions
```java
public class UserSession {
    private static UserSession instance;
    private User currentUser;
    private LocalDateTime loginTime;
    private boolean isLoggedIn;
    
    // Singleton pattern
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    // Connexion
    public void login(User user) {
        this.currentUser = user;
        this.loginTime = LocalDateTime.now();
        this.isLoggedIn = true;
        
        if (user != null) {
            user.updateLastLogin();
        }
    }
    
    // Validation de session (expire après 24h)
    public boolean isSessionValid() {
        if (!isLoggedIn || currentUser == null || loginTime == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long hours = java.time.Duration.between(loginTime, now).toHours();
        
        return hours < 24;
    }
}
```

### Flux d'authentification
1. **Connexion** :
   - Utilisateur saisit username/password
   - `AuthenticationService.login()` vérifie les credentials
   - `PasswordUtils.verifyPassword()` compare avec le hash stocké
   - Si succès : création de session, mise à jour `last_login`

2. **Inscription** :
   - Validation des données (format, unicité)
   - `PasswordUtils.hashPassword()` pour sécuriser le mot de passe
   - Insertion en base via `UserAuthDAO.insert()`

3. **Mode invité** :
   - Utilisateur temporaire avec ID = -1
   - Pas de sauvegarde des scores
   - Accès limité aux fonctionnalités

---

## Logique du jeu

### Initialisation d'une partie
```java
// Dans GameService.startNewGame()
public void startNewGame(List<String> values, Theme theme) {
    this.currentTheme = theme;
    deck.clear();
    List<String> pairedValues = new ArrayList<>();
    
    // Créer les paires (chaque valeur apparaît 2 fois)
    for (String v : values) {
        pairedValues.add(v); // Première carte
        pairedValues.add(v); // Deuxième carte
    }
    
    // Mélanger toutes les cartes
    Collections.shuffle(pairedValues);
    
    // Créer les objets Card avec association au thème
    int id = 0;
    for (String v : pairedValues) {
        Card card = new Card(id++, v, theme);
        deck.add(card);
    }
    
    attempts = 0;
    firstSelected = null;
    startTime = System.currentTimeMillis();
}
```

### Mécanisme de retournement
```java
public int flipCard(int cardIndex) {
    Card c = deck.get(cardIndex);
    if (c.isMatched()) return 0; // Déjà appariée
    
    if (firstSelected == null) {
        // Première sélection
        firstSelected = c;
        return 0;
    } else if (firstSelected.getId() == c.getId()) {
        // Même carte cliquée deux fois
        return 0;
    } else {
        // Deuxième sélection
        attempts++;
        if (firstSelected.getValue().equals(c.getValue())) {
            // Paire trouvée !
            firstSelected.setMatched(true);
            c.setMatched(true);
            firstSelected = null;
            return 1;
        } else {
            // Pas de correspondance
            firstSelected = null;
            return -1;
        }
    }
}
```

### Thèmes et contenu
1. **Numbers** : Valeurs numériques (1, 2, 3, ...)
2. **Colors** : 16 couleurs avec codes hex
3. **Animals** : 17 animaux avec images PNG
4. **Images** : Identique aux animaux

```java
// Génération de contenu par thème
private List<String> buildColorValues(int pairCount) {
    Map<String, String> colorMap = Map.ofEntries(
        Map.entry("red", "#FF6B6B"),
        Map.entry("green", "#51CF66"),
        Map.entry("blue", "#4ECDC4"),
        // ... autres couleurs
    );
    
    List<String> availableColors = new ArrayList<>(colorMap.keySet());
    List<String> result = new ArrayList<>();
    for (int i = 0; i < pairCount; i++) {
        result.add(availableColors.get(i % availableColors.size()));
    }
    return result;
}

private List<String> buildAnimalValues(int pairCount) {
    List<String> animals = List.of("bear", "cat", "cow", "deer", "dog",
            "elephant", "fox", "giraffe", "goat", "hamster", "lion", 
            "penguin", "pig", "rabbit", "sheep", "tiger", "wolf");
    
    List<String> result = new ArrayList<>();
    for (int i = 0; i < pairCount; i++) {
        result.add(animals.get(i % animals.size()));
    }
    return result;
}
```

### Animations
```java
// Animation de retournement (révélation)
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
        b.setGraphic(null);
        b.setText("");
        onHalfShown.run(); // Afficher le contenu
        rt2.play();
    });
    
    rt2.setOnFinished(e -> b.setDisable(false));
    rt1.play();
}
```

---

## Flux de données

### Flux principal d'une partie
```
1. HomeController
   ├─ Sélection thème/grille
   ├─ System.setProperty("selectedThemeId", ...)
   └─ SceneManager.show("game")

2. GameController.initialize()
   ├─ Lecture des propriétés système
   ├─ ThemeService.getTheme(id)
   ├─ GameService.startNewGame(values, theme)
   └─ setupGrid()

3. Interaction utilisateur
   ├─ onCardClicked(index)
   ├─ GameService.flipCard(index)
   ├─ Animation + logique de correspondance
   └─ Mise à jour UI

4. Fin de partie
   ├─ GameService.isFinished() = true
   ├─ Création Score(user, theme, attempts, time)
   ├─ ScoreService.saveScore() → ScoreDAO.insert()
   └─ SceneManager.show("scoreboard")
```

### Flux d'authentification
```
1. LoginController
   ├─ Saisie username/password
   ├─ AuthenticationService.login()
   ├─ UserAuthDAO.findByUsername()
   ├─ PasswordUtils.verifyPassword()
   ├─ System.setProperty("currentUserId", ...)
   └─ SceneManager.show("home")

2. Mode invité
   ├─ Création User temporaire (id = -1)
   ├─ System.setProperty("isGuest", "true")
   └─ Fonctionnalités limitées
```

### Flux de gestion des utilisateurs (Admin)
```
1. UserManagementController
   ├─ Vérification isCurrentUserAdmin()
   ├─ UserAuthDAO.findAll()
   └─ Affichage tableau

2. Actions admin
   ├─ Désactivation : UserAuthDAO.deactivate(id)
   ├─ Changement rôle : UserAuthDAO.update(user)
   └─ Rechargement des données
```

---

## Classes utilitaires

### SceneManager
```java
public class SceneManager {
    private static Stage primaryStage;
    
    public static void initialize(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Memory Game");
        primaryStage.setFullScreen(true);
        primaryStage.setResizable(true);
    }
    
    public static void show(String viewName) {
        try {
            String fxmlPath = "/views/" + viewName + ".fxml";
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            
            // Chargement du CSS
            var css = SceneManager.class.getResource("/styles/app.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML " + viewName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

### Patterns utilisés
1. **Singleton** : SceneManager, UserSession, MySQLConnection
2. **DAO Pattern** : Séparation accès données / logique métier
3. **MVC Pattern** : Séparation Model/View/Controller
4. **Observer Pattern** : Bindings JavaFX
5. **Factory Pattern** : Création d'objets Card et Score

---

## Configuration et déploiement

### Configuration Maven (pom.xml)
```xml
<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21</version>
    </dependency>
    
    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.1.0</version>
    </dependency>
    
    <!-- Tests -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.10.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Compilation Java 23 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <source>23</source>
                <target>23</target>
            </configuration>
        </plugin>
        
        <!-- JAR exécutable -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <transformers>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>com.myapp.Main</mainClass>
                            </transformer>
                        </transformers>
                        <finalName>cardgame-executable</finalName>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        
        <!-- JavaFX -->
        <plugin>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-maven-plugin</artifactId>
            <version>0.0.8</version>
            <configuration>
                <mainClass>com.myapp/com.myapp.Main</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Prérequis système
- **Java 23** ou supérieur
- **MySQL 8.1** ou supérieur
- **Maven 3.6** ou supérieur
- **JavaFX 21** (inclus dans les dépendances)

### Installation et lancement
```bash
# 1. Cloner le projet
git clone <repository-url>
cd memory-game

# 2. Configurer la base de données
mysql -u root -p < create_db_auth.sql

# 3. Compiler le projet
mvn clean compile

# 4. Lancer l'application
mvn javafx:run

# 5. Ou créer un JAR exécutable
mvn clean package
java -jar target/cardgame-executable.jar
```

### Structure des ressources
```
src/main/resources/
├── images/
│   ├── animals/          # Images des animaux (PNG)
│   ├── back.png         # Image de dos des cartes
│   ├── background.png   # Arrière-plan
│   └── logo.png         # Logo de l'application
├── styles/
│   └── app.css          # Feuille de style CSS
└── views/
    ├── login.fxml       # Vue de connexion
    ├── register.fxml    # Vue d'inscription
    ├── home.fxml        # Menu principal
    ├── game.fxml        # Interface de jeu
    ├── scoreboard.fxml  # Tableau des scores
    └── user-management.fxml # Gestion des utilisateurs
```

---

## Conclusion

Ce projet Memory Game représente une application JavaFX complète et bien structurée qui démontre :

### Points forts architecturaux
- **Séparation claire des responsabilités** avec le pattern MVC
- **Couche d'abstraction** avec les DAO pour l'accès aux données
- **Services métier** encapsulant la logique applicative
- **Gestion robuste de l'authentification** avec hachage sécurisé
- **Relations bidirectionnelles** entre les entités
- **Interface utilisateur riche** avec animations et effets

### Fonctionnalités avancées
- **Système de rôles** (USER/ADMIN) avec permissions
- **Mode invité** pour les utilisateurs non inscrits
- **Gestion complète des utilisateurs** (activation/désactivation)
- **Sauvegarde et historique** des scores
- **Interface d'administration** pour la gestion des utilisateurs
- **Validation en temps réel** lors de l'inscription

### Sécurité
- **Hachage SHA-256 avec salt** pour les mots de passe
- **Requêtes préparées** pour éviter les injections SQL
- **Validation des données** côté client et serveur
- **Gestion des sessions** avec expiration

Cette architecture modulaire et extensible permet d'ajouter facilement de nouvelles fonctionnalités tout en maintenant la cohérence et la maintenabilité du code.