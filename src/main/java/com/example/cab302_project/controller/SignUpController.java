package com.example.cab302_project.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;

import java.io.IOException;

import static com.example.cab302_project.util.ScreenManager.switchScreen;

public class SignUpController {
    private Hyperlink signInButton;
    private Button createAccountButton;

    @FXML
    private void handleSignIn(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/cab302_project/login-screen.fxml", 350, 650);
    }


}
