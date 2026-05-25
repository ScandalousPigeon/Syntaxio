package com.example.syntaxio.ui.controller;

import com.example.syntaxio.ai.chat.MainMenuAssistant;
import com.example.syntaxio.ai.client.OllamaClient;
import com.example.syntaxio.database.SessionManager;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import static com.example.syntaxio.ui.util.ScreenManager.switchScreen;

import java.io.IOException;

public class MainMenuController {

    private static final double MAX_MESSAGE_WIDTH = 640.0;

    @FXML
    private VBox popoutMenu;

    @FXML
    private HBox dashboardButton;

    @FXML
    private HBox challengeBrowserButton;

    @FXML
    private HBox logoutButton;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private VBox chatContent;

    @FXML
    private TextField chatInput;

    @FXML
    private Button sendButton;

    //@FXML
    //private Region dimOverlay;

    private final MainMenuAssistant assistant = new MainMenuAssistant(new OllamaClient());
    private boolean menuOpen = false;
    private TranslateTransition currentAnimation;

    @FXML
    private void initialize() {
        //dimOverlay.setVisible(false);

        // hide it so it doesn't flash on screen
        popoutMenu.setVisible(false);

        // wait until JavaFX has calculated the menu width
        Platform.runLater(() -> {
            popoutMenu.setTranslateX(popoutMenu.getWidth());
            popoutMenu.setVisible(true);
        });

        // if the menu width changes while closed, keep it offscreen
        popoutMenu.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (!menuOpen) {
                popoutMenu.setTranslateX(newWidth.doubleValue());
            }
        });

        chatInput.setOnAction(event -> handleSendMessage());
        addMessageBubble(
                "Hi! I'm your AI coding assistant. Ask me anything about algorithms or data structures!",
                false
        );
    }

    @FXML
    private void handleSendMessage() {
        String userMessage = chatInput.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        chatInput.clear();
        addMessageBubble(userMessage, true);

        Label pendingBubble = addMessageBubble("Thinking...", false);
        setChatControlsDisabled(true);

        Task<String> replyTask = new Task<>() {
            @Override
            protected String call() {
                return assistant.reply(userMessage);
            }
        };

        replyTask.setOnSucceeded(event -> {
            String reply = replyTask.getValue();
            pendingBubble.setText(reply == null || reply.isBlank()
                    ? "I couldn't generate a response. Please try again."
                    : reply.trim());
            setChatControlsDisabled(false);
            scrollToLatestMessage();
        });

        replyTask.setOnFailed(event -> {
            pendingBubble.setText("I couldn't reach Ollama yet. It may still be starting, or "
                    + OllamaClient.DEFAULT_MODEL
                    + " may still be downloading. Make sure Ollama is installed if this keeps happening.");
            setChatControlsDisabled(false);
            scrollToLatestMessage();
        });

        Thread replyThread = new Thread(replyTask, "main-menu-assistant-reply");
        replyThread.setDaemon(true);
        replyThread.start();
    }

    private Label addMessageBubble(String message, boolean fromUser) {
        Label bubble = new Label(message);
        bubble.setWrapText(true);
        bubble.setMaxWidth(MAX_MESSAGE_WIDTH);
        bubble.getStyleClass().add(fromUser ? "user-bubble" : "bot-bubble");

        HBox row = new HBox(bubble);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getStyleClass().add(fromUser ? "user-message-row" : "bot-message-row");

        chatContent.getChildren().add(row);
        scrollToLatestMessage();

        return bubble;
    }

    private void setChatControlsDisabled(boolean disabled) {
        chatInput.setDisable(disabled);
        sendButton.setDisable(disabled);
    }

    private void scrollToLatestMessage() {
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    @FXML
    private void openMenu() {
        if (currentAnimation != null) currentAnimation.stop();
        menuOpen = true;
        currentAnimation = new TranslateTransition(Duration.millis(220), popoutMenu);
        currentAnimation.setToX(0);
        currentAnimation.play();
    }

    @FXML
    private void closeMenu() {
        if (currentAnimation != null) currentAnimation.stop();
        menuOpen = false;
        currentAnimation = new TranslateTransition(Duration.millis(220), popoutMenu);
        currentAnimation.setToX(popoutMenu.getWidth());
        currentAnimation.play();
    }

    @FXML
    private void handleDashboard(MouseEvent event) throws IOException {
        switchScreen(event, "/com/example/syntaxio/dashboard.fxml", 1200, 1000);
    }

    @FXML
    private void handleChallengeBrowser(MouseEvent event) throws IOException {
        switchScreen(event, "/com/example/syntaxio/coding-challenge-browser.fxml", 1200, 1000);
    }

    @FXML
    private void handleLogOut(MouseEvent event) throws IOException {
        SessionManager.getInstance().logout();
        switchScreen(event, "/com/example/syntaxio/login-screen.fxml", 350, 650);
    }
}
