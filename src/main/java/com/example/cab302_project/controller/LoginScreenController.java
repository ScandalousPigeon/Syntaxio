package com.example.cab302_project.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import com.example.cab302_project.util.ScreenManager;
import java.io.IOException;

import static com.example.cab302_project.util.ScreenManager.switchScreen;

public class LoginScreenController {
    @FXML
    private Hyperlink signUpButton;
    @FXML
    private Button logInButton;

    @FXML
    private void handleLogIn(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/cab302_project/main-menu.fxml", 1200, 800);
    }

    @FXML
    private void handleSignUp(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/cab302_project/sign-up.fxml", 350, 800);
    }


}
