package com.example.syntaxio.ui.controller;

import com.example.syntaxio.database.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import static com.example.syntaxio.ui.util.ScreenManager.switchScreen;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button logInButton;
    @FXML private Label errorLabel;

    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        errorLabel.setVisible(false);
        logInButton.setDefaultButton(true);
        usernameField.setOnAction(event -> logInButton.fire());
        passwordField.setOnAction(event -> logInButton.fire());
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
            switchScreen(event, "/com/example/syntaxio/main-menu.fxml", 1200, 1150);
        } else {
            showError("Invalid username or password");
            passwordField.clear();
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/syntaxio/sign-up.fxml", 350, 700);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
