package com.UI.cab302_project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SyntaxioApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SyntaxioApp.class.getResource("/com/example/cab302_project/login-screen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 350, 650);
        stage.setTitle("Syntaxio");
        stage.setScene(scene);
        stage.setMinHeight(650);
        stage.setMinWidth(350);
        stage.show();
    }
}
