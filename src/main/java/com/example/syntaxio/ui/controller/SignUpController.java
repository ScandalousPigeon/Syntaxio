package com.example.syntaxio.ui.controller;

import com.example.syntaxio.database.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

import static com.example.syntaxio.ui.util.ScreenManager.switchScreen;

public class SignUpController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckBox;
    @FXML private Label errorLabel;
    @FXML private Hyperlink signInButton;
    @FXML private Button createAccountButton;

    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();

        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    @FXML
    private void handleCreateAccount(ActionEvent event) throws IOException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            return;
        }

        if (password.length() < 8) {
            showError("Password must be at least 8 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            confirmPasswordField.clear();
            return;
        }

        if (!termsCheckBox.isSelected()) {
            showError("Please agree to the Terms of Service");
            return;
        }

        boolean created = sessionManager.signup(username, password);

        if (created) {
            // immediately log the user in after signup
            sessionManager.login(username, password);

            switchScreen(event, "/com/example/syntaxio/main-menu.fxml", 1200, 1150);
        } else {
            showError("Username already exists");
            passwordField.clear();
            confirmPasswordField.clear();
        }
    }

    @FXML
    private void handleSignIn(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/syntaxio/login-screen.fxml", 350, 650);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}