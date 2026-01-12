# Flux de Données - Architecture Memory Game

## Vue d'ensemble du flux

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐    ┌─────────────┐    ┌──────────────┐
│   CONFIG    │    │ CONTROLLER   │    │   SERVICE   │    │     DAO     │    │   DATABASE   │
│             │    │              │    │             │    │             │    │              │
│ Connection  │◄──►│ UI Logic     │◄──►│ Business    │◄──►│ Data Access │◄──►│    MySQL     │
│ Properties  │    │ Validation   │    │ Logic       │    │ SQL Queries │    │   Tables     │
│             │    │ Navigation   │    │ Rules       │    │ Mapping     │    │              │
└─────────────┘    └──────────────┘    └─────────────┘    └─────────────┘    └──────────────┘
       ▲                   ▲                   ▲                   ▲
       │                   │                   │                   │
       └───────────────────┼───────────────────┼───────────────────┘
                           │                   │
                    ┌──────▼──────┐    ┌──────▼──────┐
                    │    MODEL    │    │   UTILS     │
                    │             │    │             │
                    │ Data Objects│    │ Helpers     │
                    │ Entities    │    │ Utilities   │
                    │ Relations   │    │             │
                    └─────────────┘    └─────────────┘
```

---

## 1. FLUX DÉTAILLÉ PAR COUCHE

### 🔧 **CONFIG (Configuration)**
**Rôle** : Fournit les paramètres de connexion et configuration système

```java
// MySQLConnection.java
public class MySQLConnection {
    private static final String HOST = "localhost";
    private static final String PORT = 3306;
    private static final String DATABASE = "memory_game";
    private static final String USER = "root";
    private static final String PASSWORD = "2004";
    
    // ✅ POINT D'ENTRÉE : Fournit la connexion à toutes les couches DAO
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

**Flux sortant** : CONFIG → DAO
- Fournit les connexions database à tous les DAO

---

### 🎮 **CONTROLLER (Contrôleur)**
**Rôle** : Gère les interactions utilisateur, coordonne les services, met à jour l'UI

```java
// LoginController.java - EXEMPLE COMPLET DE FLUX
public class LoginController {
    // ⬇️ DÉPENDANCES : Controller utilise les Services
    private final AuthenticationService authService = new AuthenticationService();
    
    @FXML
    private void onLogin(ActionEvent event) {
        // 1️⃣ RÉCUPÉRATION DONNÉES UI
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // 2️⃣ VALIDATION LOCALE (Controller)
        if (username == null || username.trim().isEmpty()) {
            showError("Veuillez entrer votre nom d'utilisateur.");
            return;
        }
        
        // 3️⃣ APPEL SERVICE : Controller → Service
        boolean success = authService.login(username.trim(), password);
        
        if (success) {
            // 4️⃣ RÉCUPÉRATION MODÈLE : Service → Controller
            User currentUser = authService.getCurrentUser();
            
            // 5️⃣ MISE À JOUR ÉTAT SYSTÈME
            System.setProperty("currentUserId", String.valueOf(currentUser.getId()));
            System.setProperty("currentUserName", currentUser.getFullName());
            
            // 6️⃣ NAVIGATION : Controller → Controller
            SceneManager.show("home");
        } else {
            // 7️⃣ GESTION ERREUR : Mise à jour UI
            showError("Nom d'utilisateur ou mot de passe incorrect.");
        }
    }
}
```

**Flux entrant** : UI → Controller
**Flux sortant** : Controller → Service, Controller → Utils (SceneManager)

---

### 🏢 **SERVICE (Service Métier)**
**Rôle** : Contient la logique métier, orchestre les DAO, valide les règles business

```java
// AuthenticationService.java - EXEMPLE COMPLET DE FLUX
public class AuthenticationService {
    // ⬇️ DÉPENDANCES : Service utilise les DAO
    private final UserAuthDAO userAuthDAO;
    private User currentUser; // ⬇️ ÉTAT : Service maintient des modèles
    
    public boolean login(String username, String password) {
        // 1️⃣ RÉCEPTION : Controller → Service
        
        // 2️⃣ VALIDATION MÉTIER (Service)
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return false;
        }
        
        // 3️⃣ APPEL DAO : Service → DAO
        User user = userAuthDAO.findByUsername(username.trim());
        
        // 4️⃣ VÉRIFICATION MODÈLE : DAO → Service (via Model)
        if (user == null || !user.isActive()) {
            return false;
        }
        
        // 5️⃣ APPEL UTILS : Service → Utils
        if (PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            // 6️⃣ MISE À JOUR ÉTAT : Service maintient le modèle
            this.currentUser = user;
            user.updateLastLogin();
            
            // 7️⃣ PERSISTANCE : Service → DAO
            userAuthDAO.updateLastLogin(user.getId());
            return true;
        }
        
        return false;
    }
    
    // 8️⃣ EXPOSITION MODÈLE : Service → Controller
    public User getCurrentUser() {
        return currentUser;
    }
}
```

**Flux entrant** : Controller → Service
**Flux sortant** : Service → DAO, Service → Utils, Service → Model

---

### 💾 **DAO (Data Access Object)**
**Rôle** : Accès aux données, mapping SQL ↔ Objets, gestion des requêtes

```java
// UserAuthDAO.java - EXEMPLE COMPLET DE FLUX
public class UserAuthDAO {
    
    public User findByUsername(String username) {
        // 1️⃣ RÉCEPTION : Service → DAO
        
        // 2️⃣ REQUÊTE SQL : DAO → Database (via Config)
        String sql = "SELECT id, first_name, last_name, username, password_hash, email, " +
                    "role, is_active, last_login, created_at, updated_at FROM users WHERE username = ?";
        
        try (Connection c = MySQLConnection.getConnection(); // ⬅️ CONFIG → DAO
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            // 3️⃣ PARAMÈTRES SÉCURISÉS
            ps.setString(1, username);
            
            // 4️⃣ EXÉCUTION : DAO → Database
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // 5️⃣ MAPPING : Database → Model (via DAO)
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // 6️⃣ RETOUR : DAO → Service (Model ou null)
        return null;
    }
    
    // 🔄 MAPPING DATABASE → MODEL
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(); // ⬅️ DAO crée le MODEL
        user.setId(rs.getInt("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));
        
        // Gestion des timestamps
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(lastLogin.toLocalDateTime());
        }
        
        return user; // ⬅️ MODEL complet retourné au Service
    }
    
    public boolean insert(User user) {
        // 1️⃣ RÉCEPTION : Service → DAO (avec Model)
        
        String sql = "INSERT INTO users (first_name, last_name, username, password_hash, " +
                    "email, role, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection c = MySQLConnection.getConnection(); // ⬅️ CONFIG → DAO
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // 2️⃣ MAPPING : Model → Database (via DAO)
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getRole() != null ? user.getRole() : "USER");
            ps.setBoolean(7, user.isActive());
            ps.setTimestamp(8, Timestamp.valueOf(user.getCreatedAt()));
            
            // 3️⃣ EXÉCUTION : DAO → Database
            int affected = ps.executeUpdate();
            
            if (affected == 1) {
                // 4️⃣ RÉCUPÉRATION ID : Database → DAO
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    // 5️⃣ MISE À JOUR MODEL : DAO → Model
                    user.setId(keys.getInt(1));
                }
                return true; // ⬅️ Succès retourné au Service
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false; // ⬅️ Échec retourné au Service
    }
}
```

**Flux entrant** : Service → DAO
**Flux sortant** : DAO → Database (via Config), DAO → Model

---

### 📊 **MODEL (Modèle de données)**
**Rôle** : Représente les entités métier, maintient les relations, encapsule les données

```java
// User.java - EXEMPLE COMPLET DE FLUX
public class User {
    // 🏗️ PROPRIÉTÉS : Stockage des données
    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private String passwordHash;
    private String email;
    private boolean isActive;
    private String role;
    private LocalDateTime lastLogin;
    private List<Score> scores; // ⬅️ RELATION avec autre Model
    
    // 🔄 RELATIONS BIDIRECTIONNELLES
    public void addScore(Score score) {
        if (score != null && !this.scores.contains(score)) {
            this.scores.add(score);
            score.setUser(this); // ⬅️ Model → Model (relation)
        }
    }
    
    // 🧮 LOGIQUE MÉTIER SIMPLE
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
    
    // 📤 EXPOSITION : Model → Service/Controller
    // Getters et Setters...
}
```

**Flux entrant** : DAO → Model, Service → Model
**Flux sortant** : Model → DAO, Model → Service, Model ↔ Model (relations)

---

## 2. FLUX COMPLET D'UN CAS D'USAGE

### 🎯 **CAS D'USAGE : Connexion utilisateur**

```
1. UI EVENT
   │
   ▼
2. LoginController.onLogin()
   │ ┌─ Validation locale
   │ └─ authService.login(username, password)
   │
   ▼
3. AuthenticationService.login()
   │ ┌─ Validation métier
   │ ├─ userAuthDAO.findByUsername(username)
   │ │
   │ ▼
4. UserAuthDAO.findByUsername()
   │ ┌─ MySQLConnection.getConnection() ◄─── CONFIG
   │ ├─ SQL Query execution
   │ ├─ createUserFromResultSet() ────────► MODEL (User)
   │ └─ return User
   │
   ▼
5. AuthenticationService (suite)
   │ ┌─ PasswordUtils.verifyPassword() ◄─── UTILS
   │ ├─ user.updateLastLogin() ──────────► MODEL
   │ ├─ userAuthDAO.updateLastLogin()
   │ └─ this.currentUser = user ─────────► MODEL (état)
   │
   ▼
6. LoginController (suite)
   │ ┌─ authService.getCurrentUser() ◄──── MODEL
   │ ├─ System.setProperty() ────────────► CONFIG (système)
   │ └─ SceneManager.show("home") ───────► UTILS
```

### 🎮 **CAS D'USAGE : Démarrage d'une partie**

```
1. HomeController.onStart()
   │ ┌─ themeCombo.getSelectedItem() ◄──── UI
   │ ├─ System.setProperty("selectedThemeId") ► CONFIG
   │ └─ SceneManager.show("game") ────────► UTILS
   │
   ▼
2. GameController.initialize()
   │ ┌─ System.getProperty("selectedThemeId") ◄─ CONFIG
   │ ├─ themeService.getTheme(id)
   │ │
   │ ▼
3. ThemeService.getTheme()
   │ └─ themeDAO.findById(id)
   │
   ▼
4. ThemeDAO.findById()
   │ ┌─ MySQLConnection.getConnection() ◄─── CONFIG
   │ ├─ SQL Query
   │ └─ return Theme ─────────────────────► MODEL
   │
   ▼
5. GameController (suite)
   │ ├─ gameService.startNewGame(values, theme)
   │ │
   │ ▼
6. GameService.startNewGame()
   │ ┌─ Collections.shuffle(pairedValues)
   │ ├─ new Card(id, value, theme) ──────► MODEL (création)
   │ └─ deck.add(card) ──────────────────► MODEL (collection)
   │
   ▼
7. GameController (suite)
   │ └─ setupGrid() ─────────────────────► UI (mise à jour)
```

### 💾 **CAS D'USAGE : Sauvegarde d'un score**

```
1. GameController.onGameFinished()
   │ ├─ new Score(user, theme, attempts, time) ► MODEL (création)
   │ └─ scoreService.saveScore(score)
   │
   ▼
2. ScoreService.saveScore()
   │ └─ scoreDAO.insert(score)
   │
   ▼
3. ScoreDAO.insert()
   │ ┌─ MySQLConnection.getConnection() ◄─── CONFIG
   │ ├─ score.getUser().getId() ◄─────────── MODEL
   │ ├─ score.getTheme().getId() ◄────────── MODEL
   │ ├─ score.getAttempts() ◄────────────── MODEL
   │ ├─ SQL INSERT execution
   │ └─ score.setId(generatedId) ─────────► MODEL (mise à jour)
   │
   ▼
4. GameController (suite)
   │ └─ SceneManager.show("scoreboard") ──► UTILS
```

---

## 3. FLUX DE DONNÉES PAR TYPE

### 📥 **FLUX ENTRANT (Vers l'application)**

```
DATABASE ──SQL──► DAO ──Model──► SERVICE ──Model──► CONTROLLER ──UI──► USER
```

**Exemple** : Affichage des scores
1. **Database** : `SELECT * FROM scores JOIN users JOIN themes`
2. **DAO** : `ScoreDAO.findAll()` → mapping vers objets `Score`
3. **Service** : `ScoreService.listScoresBy()` → logique de tri
4. **Controller** : `ScoreboardController.loadScores()` → mise à jour TableView
5. **UI** : Affichage dans l'interface utilisateur

### 📤 **FLUX SORTANT (Depuis l'application)**

```
USER ──UI──► CONTROLLER ──Model──► SERVICE ──Model──► DAO ──SQL──► DATABASE
```

**Exemple** : Inscription utilisateur
1. **User** : Saisie dans les champs du formulaire
2. **Controller** : `RegisterController.onRegister()` → validation UI
3. **Service** : `AuthenticationService.register()` → validation métier
4. **DAO** : `UserAuthDAO.insert()` → requête SQL INSERT
5. **Database** : Persistance des données

### 🔄 **FLUX BIDIRECTIONNEL (Relations)**

```
MODEL ←──Relations──→ MODEL
  ↕                    ↕
SERVICE ←──Logic──→ SERVICE
  ↕                    ↕
DAO ←──Queries──→ DAO
```

**Exemple** : Relations User ↔ Score
```java
// Dans ScoreDAO.createScoreFromResultSet()
User user = new User(rs.getInt("user_id"), rs.getString("first_name"), rs.getString("last_name"));
Theme theme = new Theme(rs.getInt("theme_id"), rs.getString("theme_name"));
Score score = new Score(rs.getInt("id"), user, theme, rs.getInt("attempts"), rs.getInt("time_seconds"));

// Les relations bidirectionnelles sont automatiquement établies
// user.getScores() contient maintenant ce score
// theme.getScores() contient maintenant ce score
```

---

## 4. GESTION DES ERREURS DANS LE FLUX

### 🚨 **Propagation des erreurs**

```
DATABASE ──SQLException──► DAO ──boolean/null──► SERVICE ──Exception/boolean──► CONTROLLER ──UI Error──► USER
```

**Exemple** : Gestion d'erreur de connexion
```java
// DAO Level
public User findByUsername(String username) {
    try (Connection c = MySQLConnection.getConnection()) {
        // ... requête SQL
    } catch (SQLException e) {
        e.printStackTrace(); // Log de l'erreur
        return null; // ◄─── Erreur transformée en null
    }
}

// Service Level
public boolean login(String username, String password) {
    User user = userAuthDAO.findByUsername(username);
    if (user == null) { // ◄─── Gestion du null du DAO
        return false; // ◄─── Erreur transformée en boolean
    }
    // ...
}

// Controller Level
if (!authService.login(username, password)) { // ◄─── Gestion du boolean du Service
    showError("Nom d'utilisateur ou mot de passe incorrect."); // ◄─── Erreur affichée à l'utilisateur
}
```

---

## 5. OPTIMISATIONS DU FLUX

### ⚡ **Mise en cache (Service Level)**
```java
public class ThemeService {
    private List<Theme> cachedThemes = null; // Cache au niveau Service
    
    public List<Theme> getAllThemes() {
        if (cachedThemes == null) {
            cachedThemes = dao.findAll(); // ◄─── Appel DAO seulement si nécessaire
        }
        return cachedThemes;
    }
}
```

### 🔄 **Lazy Loading (Model Level)**
```java
public class User {
    private List<Score> scores;
    private boolean scoresLoaded = false;
    
    public List<Score> getScores() {
        if (!scoresLoaded) {
            // Chargement à la demande via Service
            this.scores = scoreService.getScoresByUser(this.id);
            this.scoresLoaded = true;
        }
        return scores;
    }
}
```

### 🎯 **Connection Pooling (Config Level)**
```java
public class MySQLConnection {
    private static HikariDataSource dataSource;
    
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(10); // ◄─── Pool de connexions
        dataSource = new HikariDataSource(config);
    }
    
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection(); // ◄─── Connexion depuis le pool
    }
}
```

---

## 6. FLUX ASYNCHRONE

### 🔄 **Opérations en arrière-plan**
```java
// LoginController - Authentification asynchrone
@FXML
private void onLogin(ActionEvent event) {
    btnLogin.setDisable(true);
    btnLogin.setText("Connexion...");
    
    // ◄─── FLUX ASYNCHRONE : UI Thread → Background Thread
    new Thread(() -> {
        try {
            boolean success = authService.login(username, password); // ◄─── Service appelé en arrière-plan
            
            // ◄─── RETOUR AU UI THREAD : Background Thread → UI Thread
            Platform.runLater(() -> {
                btnLogin.setDisable(false);
                btnLogin.setText("Se connecter");
                
                if (success) {
                    User currentUser = authService.getCurrentUser();
                    System.setProperty("currentUserId", String.valueOf(currentUser.getId()));
                    SceneManager.show("home");
                } else {
                    showError("Nom d'utilisateur ou mot de passe incorrect.");
                }
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                btnLogin.setDisable(false);
                btnLogin.setText("Se connecter");
                showError("Erreur technique. Veuillez réessayer.");
            });
        }
    }).start();
}
```

---

## 7. RÉSUMÉ DES RESPONSABILITÉS

| Couche | Responsabilités | Flux Entrant | Flux Sortant |
|--------|----------------|--------------|--------------|
| **CONFIG** | Configuration, Connexions | - | Connexions DB → DAO |
| **CONTROLLER** | UI, Validation, Navigation | Events UI | Appels Service, Navigation |
| **SERVICE** | Logique métier, Orchestration | Appels Controller | Appels DAO, Retour Models |
| **DAO** | Accès données, Mapping SQL | Appels Service | Requêtes DB, Models |
| **MODEL** | Entités, Relations, État | Création DAO/Service | Données vers toutes couches |
| **UTILS** | Helpers, Utilitaires | Appels diverses couches | Services utilitaires |

### 🎯 **Règles de flux**
1. **Jamais de saut de couche** : Controller ne peut pas appeler DAO directement
2. **Unidirectionnalité** : Les dépendances vont toujours vers le bas
3. **Séparation des responsabilités** : Chaque couche a un rôle précis
4. **Gestion centralisée des erreurs** : Chaque couche transforme et propage les erreurs
5. **Models partagés** : Les objets Model circulent entre toutes les couches

Cette architecture garantit la maintenabilité, la testabilité et l'évolutivité de l'application.