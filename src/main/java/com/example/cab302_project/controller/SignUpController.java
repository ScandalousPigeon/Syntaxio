package com.example.cab302_project.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

import java.io.IOException;

public class SignUpController {
    private Hyperlink signInButton;
    private Button createAccountButton;

    @FXML
    private void handleSignIn(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/cab302_project/login-screen.fxml");
    }

    private void switchScreen(ActionEvent event, String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.getScene().setRoot(root);
    }
}
