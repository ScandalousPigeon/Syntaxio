package com.example.syntaxio.ui.controller;

import com.example.syntaxio.ai.chat.MainMenuAssistant;
import com.example.syntaxio.ai.client.OllamaClient;
import com.example.syntaxio.database.SessionManager;
import com.example.syntaxio.database.SqliteChallengeDAO;
import com.example.syntaxio.model.Challenge;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import static com.example.syntaxio.ui.util.ScreenManager.switchScreen;

import java.io.IOException;
import java.util.List;

public class MainMenuController {

    private static final double MIN_MESSAGE_WIDTH = 220.0;
    private static final double MAX_MESSAGE_WIDTH = 900.0;
    private static final double MESSAGE_WIDTH_RATIO = 0.78;
    private static final int SUGGESTED_PUZZLE_LIMIT = 6;
    private static final int SUGGESTED_PUZZLE_COLUMNS = 2;
    private static final int SUGGESTED_DESCRIPTION_LIMIT = 90;
    private static final double SUGGESTED_CARD_HEIGHT = 100.0;

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

    @FXML
    private GridPane suggestedPuzzlesGrid;

    //@FXML
    //private Region dimOverlay;

    private final MainMenuAssistant assistant = new MainMenuAssistant(new OllamaClient());
    private SqliteChallengeDAO challengeDAO;
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

        loadSuggestedPuzzles();
    }

    private void loadSuggestedPuzzles() {
        if (suggestedPuzzlesGrid == null) {
            return;
        }

        challengeDAO = new SqliteChallengeDAO();
        List<Challenge> suggestedChallenges = challengeDAO.getAllChallenges()
                .stream()
                .limit(SUGGESTED_PUZZLE_LIMIT)
                .toList();

        displaySuggestedPuzzles(suggestedChallenges);
    }

    private void displaySuggestedPuzzles(List<Challenge> challenges) {
        suggestedPuzzlesGrid.getChildren().clear();

        if (challenges.isEmpty()) {
            VBox emptyCard = createEmptySuggestedPuzzleCard();
            GridPane.setColumnSpan(emptyCard, SUGGESTED_PUZZLE_COLUMNS);
            suggestedPuzzlesGrid.getChildren().add(emptyCard);
            return;
        }

        for (int i = 0; i < challenges.size(); i++) {
            VBox card = createSuggestedPuzzleCard(challenges.get(i));
            GridPane.setColumnIndex(card, i % SUGGESTED_PUZZLE_COLUMNS);
            GridPane.setRowIndex(card, i / SUGGESTED_PUZZLE_COLUMNS);
            GridPane.setHgrow(card, Priority.ALWAYS);
            suggestedPuzzlesGrid.getChildren().add(card);
        }
    }

    private VBox createSuggestedPuzzleCard(Challenge challenge) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("content-card", "suggested-puzzle-card");
        card.setAlignment(Pos.TOP_LEFT);
        card.setMinHeight(SUGGESTED_CARD_HEIGHT);
        card.setPrefHeight(SUGGESTED_CARD_HEIGHT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setOnMouseClicked(event -> startChallenge(event, challenge.getId()));

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(challenge.getTitle());
        titleLabel.getStyleClass().add("suggested-puzzle-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        String difficulty = challenge.getDifficulty() == null ? "UNKNOWN" : challenge.getDifficulty();
        Label difficultyLabel = new Label(difficulty);
        difficultyLabel.getStyleClass().add("suggested-puzzle-difficulty");
        difficultyLabel.setStyle("-fx-text-fill: " + getDifficultyColor(difficulty) + ";");

        header.getChildren().addAll(titleLabel, difficultyLabel);

        Label descriptionLabel = new Label(createDescriptionPreview(challenge.getDescription()));
        descriptionLabel.getStyleClass().add("suggested-puzzle-description");
        descriptionLabel.setWrapText(true);

        int testCount = challenge.getTestCases() == null ? 0 : challenge.getTestCases().size();
        Label metadataLabel = new Label(testCount + " tests");
        metadataLabel.getStyleClass().add("suggested-puzzle-meta");

        card.getChildren().addAll(header, descriptionLabel, metadataLabel);
        return card;
    }

    private VBox createEmptySuggestedPuzzleCard() {
        VBox card = new VBox();
        card.getStyleClass().addAll("content-card", "suggested-puzzle-empty-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinHeight(SUGGESTED_CARD_HEIGHT);
        card.setPrefHeight(SUGGESTED_CARD_HEIGHT);
        card.setMaxWidth(Double.MAX_VALUE);

        Label message = new Label("No puzzles are available yet.");
        message.getStyleClass().add("suggested-puzzle-description");
        message.setWrapText(true);
        card.getChildren().add(message);

        return card;
    }

    private String createDescriptionPreview(String description) {
        String preview = description == null ? "" : description.replaceAll("\\s+", " ").trim();
        if (preview.length() <= SUGGESTED_DESCRIPTION_LIMIT) {
            return preview;
        }

        return preview.substring(0, SUGGESTED_DESCRIPTION_LIMIT - 3) + "...";
    }

    private String getDifficultyColor(String difficulty) {
        return switch (difficulty) {
            case "EASY" -> "#4ecdc4";
            case "MEDIUM" -> "#f9ca24";
            case "HARD" -> "#ff6b6b";
            default -> "#cccccc";
        };
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
        bubble.setMinHeight(Region.USE_PREF_SIZE);
        bubble.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> {
                    double availableWidth = chatContent.getWidth() > 0
                            ? chatContent.getWidth()
                            : MAX_MESSAGE_WIDTH;
                    return Math.max(
                            MIN_MESSAGE_WIDTH,
                            Math.min(MAX_MESSAGE_WIDTH, availableWidth * MESSAGE_WIDTH_RATIO)
                    );
                },
                chatContent.widthProperty()
        ));
        bubble.getStyleClass().add(fromUser ? "user-bubble" : "bot-bubble");

        HBox row = new HBox(bubble);
        row.setMaxWidth(Double.MAX_VALUE);
        row.prefWidthProperty().bind(chatContent.widthProperty());
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

    private void startChallenge(MouseEvent event, String challengeId) {
        try {
            CodingChallengeController.setCurrentChallengeId(challengeId);
            switchScreen(event, "/com/example/syntaxio/coding-challenge.fxml", 1200, 800);
        } catch (IOException | IllegalArgumentException e) {
            showError("Could not open puzzle: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogOut(MouseEvent event) throws IOException {
        SessionManager.getInstance().logout();
        switchScreen(event, "/com/example/syntaxio/login-screen.fxml", 350, 650);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Suggested Puzzles");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
