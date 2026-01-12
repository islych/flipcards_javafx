package com.myapp.controllers;

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
import javafx.scene.layout.VBox;

/**
 * Contrôleur pour la vue d'inscription
 */
public class RegisterController {
    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button btnRegister;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnBackToLogin;

    @FXML
    private Label messageLabel;

    @FXML
    private Label usernameValidation;

    @FXML
    private Label emailValidation;

    @FXML
    private Label passwordValidation;

    @FXML
    private VBox validationBox;

    @FXML
    private ImageView logoView;

    private final AuthenticationService authService = new AuthenticationService();

    @FXML
    public void initialize() {
        loadLogo();
        setupValidation();
        setupEnterKeyHandlers();
        
        // Focus initial sur le prénom
        Platform.runLater(() -> firstNameField.requestFocus());
    }

    @FXML
    private void onRegister(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Désactiver le bouton pendant l'inscription
        btnRegister.setDisable(true);
        btnRegister.setText("Inscription...");

        // Inscription en arrière-plan
        new Thread(() -> {
            try {
                boolean success = authService.register(firstName, lastName, username, 
                                                     email.isEmpty() ? null : email, password);
                
                Platform.runLater(() -> {
                    btnRegister.setDisable(false);
                    btnRegister.setText("S'inscrire");
                    
                    if (success) {
                        showMessage("Inscription réussie ! Vous pouvez maintenant vous connecter.", false);
                        
                        // Rediriger vers la connexion après 2 secondes
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                Platform.runLater(() -> SceneManager.show("login"));
                            } catch (InterruptedException ignored) {}
                        }).start();
                    } else {
                        showMessage("Erreur lors de l'inscription. Veuillez réessayer.", true);
                    }
                });
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> {
                    btnRegister.setDisable(false);
                    btnRegister.setText("S'inscrire");
                    showMessage(e.getMessage(), true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnRegister.setDisable(false);
                    btnRegister.setText("S'inscrire");
                    showMessage("Erreur technique. Veuillez réessayer.", true);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    @FXML
    private void onCancel(ActionEvent event) {
        clearForm();
    }

    @FXML
    private void onBackToLogin(ActionEvent event) {
        SceneManager.show("login");
    }

    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        // Validation prénom
        if (firstNameField.getText() == null || firstNameField.getText().trim().length() < 2) {
            errors.append("Le prénom doit contenir au moins 2 caractères.\n");
            isValid = false;
        }

        // Validation nom
        if (lastNameField.getText() == null || lastNameField.getText().trim().length() < 2) {
            errors.append("Le nom doit contenir au moins 2 caractères.\n");
            isValid = false;
        }

        // Validation nom d'utilisateur
        String username = usernameField.getText();
        if (username == null || username.trim().length() < 3) {
            errors.append("Le nom d'utilisateur doit contenir au moins 3 caractères.\n");
            isValid = false;
        } else if (!username.trim().matches("^[a-zA-Z0-9_.-]+$")) {
            errors.append("Le nom d'utilisateur ne peut contenir que des lettres, chiffres, _, . et -\n");
            isValid = false;
        }

        // Validation email (optionnel)
        String email = emailField.getText();
        if (email != null && !email.trim().isEmpty()) {
            if (!email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                errors.append("Format d'email invalide.\n");
                isValid = false;
            }
        }

        // Validation mot de passe
        String password = passwordField.getText();
        if (password == null || password.length() < 6) {
            errors.append("Le mot de passe doit contenir au moins 6 caractères.\n");
            isValid = false;
        }

        // Validation confirmation mot de passe
        String confirmPassword = confirmPasswordField.getText();
        if (!password.equals(confirmPassword)) {
            errors.append("Les mots de passe ne correspondent pas.\n");
            isValid = false;
        }

        if (!isValid) {
            showMessage(errors.toString().trim(), true);
        }

        return isValid;
    }

    private void setupValidation() {
        // Validation en temps réel du nom d'utilisateur
        usernameField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.trim().isEmpty()) {
                new Thread(() -> {
                    boolean available = authService.isUsernameAvailable(newText.trim());
                    Platform.runLater(() -> {
                        if (available) {
                            usernameValidation.setText("✓ Nom d'utilisateur disponible");
                            usernameValidation.setStyle("-fx-text-fill: green;");
                        } else {
                            usernameValidation.setText("✗ Nom d'utilisateur déjà pris");
                            usernameValidation.setStyle("-fx-text-fill: red;");
                        }
                        usernameValidation.setVisible(true);
                    });
                }).start();
            } else {
                usernameValidation.setVisible(false);
            }
        });

        // Validation en temps réel de l'email
        emailField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.trim().isEmpty()) {
                if (newText.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    new Thread(() -> {
                        boolean available = authService.isEmailAvailable(newText.trim());
                        Platform.runLater(() -> {
                            if (available) {
                                emailValidation.setText("✓ Email disponible");
                                emailValidation.setStyle("-fx-text-fill: green;");
                            } else {
                                emailValidation.setText("✗ Email déjà utilisé");
                                emailValidation.setStyle("-fx-text-fill: red;");
                            }
                            emailValidation.setVisible(true);
                        });
                    }).start();
                } else {
                    emailValidation.setText("✗ Format d'email invalide");
                    emailValidation.setStyle("-fx-text-fill: red;");
                    emailValidation.setVisible(true);
                }
            } else {
                emailValidation.setVisible(false);
            }
        });

        // Validation en temps réel du mot de passe
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            updatePasswordValidation();
        });

        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            updatePasswordValidation();
        });
    }

    private void updatePasswordValidation() {
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (password != null && !password.isEmpty()) {
            if (password.length() >= 6) {
                if (confirm != null && !confirm.isEmpty()) {
                    if (password.equals(confirm)) {
                        passwordValidation.setText("✓ Mots de passe identiques");
                        passwordValidation.setStyle("-fx-text-fill: green;");
                    } else {
                        passwordValidation.setText("✗ Mots de passe différents");
                        passwordValidation.setStyle("-fx-text-fill: red;");
                    }
                } else {
                    passwordValidation.setText("✓ Mot de passe valide");
                    passwordValidation.setStyle("-fx-text-fill: green;");
                }
            } else {
                passwordValidation.setText("✗ Minimum 6 caractères");
                passwordValidation.setStyle("-fx-text-fill: red;");
            }
            passwordValidation.setVisible(true);
        } else {
            passwordValidation.setVisible(false);
        }
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        messageLabel.setVisible(true);
        
        // Masquer le message après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> {
                    if (messageLabel.getText().equals(message)) {
                        messageLabel.setVisible(false);
                    }
                });
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        messageLabel.setVisible(false);
        usernameValidation.setVisible(false);
        emailValidation.setVisible(false);
        passwordValidation.setVisible(false);
        firstNameField.requestFocus();
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
        firstNameField.setOnAction(e -> lastNameField.requestFocus());
        lastNameField.setOnAction(e -> usernameField.requestFocus());
        usernameField.setOnAction(e -> emailField.requestFocus());
        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> confirmPasswordField.requestFocus());
        confirmPasswordField.setOnAction(e -> onRegister(null));
    }
}