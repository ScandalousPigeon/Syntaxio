package com.UI.cab302_project.controller;

import com.Database.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import static com.UI.cab302_project.util.ScreenManager.switchScreen;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        if (sessionManager.login(username, password)) {
            switchScreen(event, "/com/example/cab302_project/main-menu.fxml", 1200, 1150);
        } else {
            showError("Invalid username or password");
            passwordField.clear();
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/cab302_project/sign-up.fxml", 350, 650);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
