package com.myapp;

import com.myapp.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

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

    public static void main(String[] args) {
        launch(args);
    }
}