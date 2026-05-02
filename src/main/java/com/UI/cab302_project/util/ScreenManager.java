package com.UI.cab302_project.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class ScreenManager {
    // helper class to switch between screens
    public static void switchScreen(ActionEvent event, String fxmlPath, double width, double height) throws IOException {
        Parent root = FXMLLoader.load(ScreenManager.class.getResource(fxmlPath));

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.getScene().setRoot(root);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.centerOnScreen();
    }

    public static void switchScreen(MouseEvent event, String fxmlPath, double width, double height) throws IOException {
        Parent root = FXMLLoader.load(ScreenManager.class.getResource(fxmlPath));

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.getScene().setRoot(root);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.centerOnScreen();
    }
}
