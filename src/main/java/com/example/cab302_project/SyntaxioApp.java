package com.example.cab302_project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SyntaxioApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SyntaxioApp.class.getResource("login-screen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Syntaxio");
        stage.setScene(scene);
        stage.setMinHeight(700);
        stage.setMinWidth(400);
        stage.show();
    }
}
