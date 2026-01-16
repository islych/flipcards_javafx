package com.myapp.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static Stage primaryStage;

    public static void initialize(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Memory Game");

        // Configuration de la fenêtre en mode normal (pas plein écran)
        primaryStage.setFullScreen(false);
        
        // Définir une taille par défaut pour la fenêtre
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        
        // Centrer la fenêtre sur l'écran
        primaryStage.centerOnScreen();

        // Rendre la fenêtre redimensionnable
        primaryStage.setResizable(true);
        
        // Optionnel : permettre le plein écran avec F11 si l'utilisateur le souhaite
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("F11"));
    }

    public static void show(String viewName) {
        try {
            String fxmlPath = "/views/" + viewName + ".fxml";
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            if (loader.getLocation() == null) {
                System.err.println("FXML not found: " + fxmlPath);
                return;
            }
            Parent root = loader.load();

            // 🎨 Créer une scène qui couvre tout l'écran (le plein écran gère la taille)
            Scene scene = new Scene(root);

            // Charger le CSS
            try {
                var css = SceneManager.class.getResource("/styles/app.css");
                if (css != null) {
                    scene.getStylesheets().add(css.toExternalForm());
                }
            } catch (Exception ex) {
                System.err.println("Could not load stylesheet: " + ex.getMessage());
            }

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML " + viewName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}