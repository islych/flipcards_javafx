package com.myapp.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static Stage primaryStage;

    public static void initialize(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Memory Game");

        // 🔑 Activer le plein écran au démarrage
        primaryStage.setFullScreen(true);

        // 🔑 Permettre la sortie du plein écran avec F11 ou le gestionnaire de fenêtre
        primaryStage.setFullScreenExitKeyCombination(null); // Optionnel : désactive F11 si vous voulez gérer vous-même
        // OU laissez F11 actif (recommandé) → pas besoin de la ligne ci-dessus

        // 🔑 Rendre la fenêtre redimensionnable une fois sortie du plein écran
        primaryStage.setResizable(true);
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