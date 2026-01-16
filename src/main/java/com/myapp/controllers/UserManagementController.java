package com.myapp.controllers;

import com.myapp.models.User;
import com.myapp.services.UserService;
import com.myapp.dao.UserAuthDAO;
import com.myapp.utils.SceneManager;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UserManagementController {
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colFirstName;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Boolean> colActive;
    @FXML private TableColumn<User, String> colLastLogin;
    
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private Button btnDeactivateUser;
    @FXML private Button btnActivateUser;
    @FXML private Button btnChangeRole;
    @FXML private Button btnDeleteUser;
    @FXML private Button btnRefresh;
    @FXML private Button btnBack;
    @FXML private Label statusLabel;

    private final UserService userService = new UserService();
    private final UserAuthDAO userAuthDAO = new UserAuthDAO();
    private ObservableList<User> allUsers;

    @FXML
    public void initialize() {
        // Vérifier que l'utilisateur actuel est admin
        if (!isCurrentUserAdmin()) {
            showError("Accès refusé. Seuls les administrateurs peuvent accéder à cette page.");
            onBack();
            return;
        }

        initializeColumns();
        initializeFilters();
        loadUsers();
    }

    private boolean isCurrentUserAdmin() {
        String currentUserIdStr = System.getProperty("currentUserId");
        if (currentUserIdStr != null && !currentUserIdStr.equals("-1")) {
            try {
                int userId = Integer.parseInt(currentUserIdStr);
                User currentUser = userService.getUser(userId);
                return currentUser != null && currentUser.isAdmin();
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private void initializeColumns() {
        colId.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colFirstName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFirstName()));
        colLastName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getLastName()));
        colUsername.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        colEmail.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail()));
        colRole.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRole()));
        colActive.setCellValueFactory(cellData -> 
            new SimpleBooleanProperty(cellData.getValue().isActive()).asObject());
        colLastLogin.setCellValueFactory(cellData -> {
            LocalDateTime lastLogin = cellData.getValue().getLastLogin();
            if (lastLogin != null) {
                return new SimpleStringProperty(lastLogin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            } else {
                return new SimpleStringProperty("Jamais");
            }
        });
    }

    private void initializeFilters() {
        roleFilterCombo.setItems(FXCollections.observableArrayList(
            "Tous", "USER", "ADMIN"
        ));
        roleFilterCombo.getSelectionModel().select(0);
        roleFilterCombo.setOnAction(e -> applyFilters());
    }

    private void loadUsers() {
        List<User> users = userAuthDAO.findAll();
        allUsers = FXCollections.observableArrayList(users);
        applyFilters();
        showStatus("Utilisateurs chargés: " + users.size());
    }

    private void applyFilters() {
        if (allUsers == null) return;

        String selectedRole = roleFilterCombo.getSelectionModel().getSelectedItem();
        List<User> filteredUsers = allUsers.stream()
            .filter(user -> {
                if ("Tous".equals(selectedRole)) return true;
                return selectedRole.equals(user.getRole());
            })
            .collect(Collectors.toList());

        usersTable.setItems(FXCollections.observableArrayList(filteredUsers));
    }

    @FXML
    private void onRefresh() {
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

        if (!selectedUser.isActive()) {
            showError("Cet utilisateur est déjà désactivé.");
            return;
        }

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

    @FXML
    private void onActivateUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur à activer.");
            return;
        }

        if (selectedUser.isActive()) {
            showError("Cet utilisateur est déjà actif.");
            return;
        }

        if (userAuthDAO.activate(selectedUser.getId())) {
            showStatus("Utilisateur " + selectedUser.getFullName() + " activé avec succès.");
            loadUsers();
        } else {
            showError("Erreur lors de l'activation de l'utilisateur.");
        }
    }

    @FXML
    private void onChangeRole() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur.");
            return;
        }

        // Ne pas permettre de changer son propre rôle
        String currentUserIdStr = System.getProperty("currentUserId");
        if (currentUserIdStr != null && selectedUser.getId() == Integer.parseInt(currentUserIdStr)) {
            showError("Vous ne pouvez pas modifier votre propre rôle.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(selectedUser.getRole(), "USER", "ADMIN");
        dialog.setTitle("Changer le rôle");
        dialog.setHeaderText("Changer le rôle de " + selectedUser.getFullName());
        dialog.setContentText("Nouveau rôle:");

        dialog.showAndWait().ifPresent(newRole -> {
            if (!newRole.equals(selectedUser.getRole())) {
                selectedUser.setRole(newRole);
                if (userAuthDAO.update(selectedUser)) {
                    showStatus("Rôle de " + selectedUser.getFullName() + " changé en " + newRole);
                    loadUsers();
                } else {
                    showError("Erreur lors du changement de rôle.");
                }
            }
        });
    }

    @FXML
    private void onBack() {
        SceneManager.show("home");
    }

    @FXML
    private void onDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur à supprimer.");
            return;
        }

        if (selectedUser.isAdmin()) {
            showError("Impossible de supprimer un administrateur.");
            return;
        }

        // Ne pas permettre de supprimer son propre compte
        String currentUserIdStr = System.getProperty("currentUserId");
        if (currentUserIdStr != null && selectedUser.getId() == Integer.parseInt(currentUserIdStr)) {
            showError("Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer définitivement l'utilisateur");
        confirmation.setContentText("⚠️ ATTENTION ⚠️\n\n" +
                "Êtes-vous sûr de vouloir supprimer DÉFINITIVEMENT " + selectedUser.getFullName() + " ?\n\n" +
                "Cette action est IRRÉVERSIBLE et supprimera :\n" +
                "• Le compte utilisateur\n" +
                "• TOUS ses scores\n" +
                "• Toutes ses données\n\n" +
                "L'utilisateur sera complètement effacé de la base de données.");

        // Personnaliser les boutons
        confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Button yesButton = (Button) confirmation.getDialogPane().lookupButton(ButtonType.YES);
        Button noButton = (Button) confirmation.getDialogPane().lookupButton(ButtonType.NO);
        yesButton.setText("Supprimer définitivement");
        noButton.setText("Annuler");

        if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            // Suppression définitive de la base de données
            if (userAuthDAO.deleteUser(selectedUser.getId())) {
                showStatus("✅ Utilisateur " + selectedUser.getFullName() + " supprimé définitivement de la base de données.");
                loadUsers();
            } else {
                showError("❌ Erreur lors de la suppression définitive de l'utilisateur. Vérifiez les contraintes de base de données.");
            }
        }
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #27ae60;");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}