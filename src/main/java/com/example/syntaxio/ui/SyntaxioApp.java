package com.example.syntaxio.ui;

import com.example.syntaxio.ai.client.OllamaRuntimeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SyntaxioApp extends Application {
    private final OllamaRuntimeManager ollamaRuntimeManager = new OllamaRuntimeManager();

    @Override
    public void init() {
        ollamaRuntimeManager.startInBackground();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SyntaxioApp.class.getResource("/com/example/syntaxio/login-screen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 350, 650);
        stage.setTitle("Syntaxio");
        stage.setScene(scene);
        stage.setMinHeight(650);
        stage.setMinWidth(350);
        stage.show();
    }

    @Override
    public void stop() {
        ollamaRuntimeManager.stopManagedProcesses();
    }
}
