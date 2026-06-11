package com.example.syntaxio.ui.controller;

import com.example.syntaxio.ai.chat.MainMenuAssistant;
import com.example.syntaxio.ai.client.OllamaClient;
import com.example.syntaxio.database.SessionManager;
import com.example.syntaxio.database.SqliteChallengeDAO;
import com.example.syntaxio.database.SqliteInProgressChallengeDAO;
import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.InProgressChallenge;
import com.example.syntaxio.model.User;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import static com.example.syntaxio.ui.util.ScreenManager.switchScreen;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MainMenuController {

    private static final double MIN_MESSAGE_WIDTH = 220.0;
    private static final double MAX_MESSAGE_WIDTH = 900.0;
    private static final double MESSAGE_WIDTH_RATIO = 0.78;
    private static final DateTimeFormatter UPDATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.ENGLISH);

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
    private VBox inProgressContent;

    @FXML
    private VBox inProgressEmptyState;

    @FXML
    private Label inProgressTitleLabel;

    @FXML
    private Label inProgressDifficultyLabel;

    @FXML
    private Label inProgressMetadataLabel;

    @FXML
    private Button continueInProgressButton;

    //@FXML
    //private Region dimOverlay;

    private final MainMenuAssistant assistant = new MainMenuAssistant(new OllamaClient());
    private boolean menuOpen = false;
    private TranslateTransition currentAnimation;
    private SessionManager sessionManager;
    private SqliteChallengeDAO challengeDAO;
    private SqliteInProgressChallengeDAO inProgressChallengeDAO;
    private String currentInProgressChallengeId;

    @FXML
    private void initialize() {
        sessionManager = SessionManager.getInstance();
        challengeDAO = new SqliteChallengeDAO();
        inProgressChallengeDAO = new SqliteInProgressChallengeDAO();

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
        renderInProgressCard();
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
    private void handleContinueInProgress(ActionEvent event) throws IOException {
        if (currentInProgressChallengeId == null || currentInProgressChallengeId.isBlank()) {
            switchScreen(event, "/com/example/syntaxio/coding-challenge-browser.fxml", 1200, 1000);
            return;
        }

        CodingChallengeController.setCurrentChallengeId(currentInProgressChallengeId);
        switchScreen(event, "/com/example/syntaxio/coding-challenge.fxml", 1200, 800);
    }

    @FXML
    private void handleBrowseChallenges(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/syntaxio/coding-challenge-browser.fxml", 1200, 1000);
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

    private void renderInProgressCard() {
        Optional<InProgressChallenge> inProgressChallenge = loadMostRecentInProgressChallenge();

        if (inProgressChallenge.isEmpty()) {
            currentInProgressChallengeId = null;
            setVisibleManaged(inProgressContent, false);
            setVisibleManaged(inProgressEmptyState, true);
            setText(inProgressDifficultyLabel, "");
            return;
        }

        InProgressChallenge progress = inProgressChallenge.get();
        Challenge challenge = challengeDAO.getChallengeById(progress.getChallengeId());
        if (challenge == null) {
            currentInProgressChallengeId = null;
            setVisibleManaged(inProgressContent, false);
            setVisibleManaged(inProgressEmptyState, true);
            setText(inProgressDifficultyLabel, "");
            return;
        }

        currentInProgressChallengeId = challenge.getId();
        setVisibleManaged(inProgressContent, true);
        setVisibleManaged(inProgressEmptyState, false);
        setText(inProgressTitleLabel, challenge.getTitle());
        setText(inProgressDifficultyLabel, challenge.getDifficulty());
        inProgressDifficultyLabel.setStyle("-fx-text-fill: " + challenge.getDifficultyColor() + ";");
        setText(inProgressMetadataLabel, "Last open "
                + progress.getUpdatedAt().format(UPDATED_AT_FORMATTER)
                + " - " + challenge.getTestCases().size()
                + " tests");
        if (continueInProgressButton != null) {
            continueInProgressButton.setDisable(false);
        }
    }

    private Optional<InProgressChallenge> loadMostRecentInProgressChallenge() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            return Optional.empty();
        }

        List<InProgressChallenge> inProgressChallenges =
                inProgressChallengeDAO.getInProgressForUser(currentUser.getId());
        return inProgressChallenges.stream().findFirst();
    }

    private void setVisibleManaged(Region region, boolean visible) {
        if (region != null) {
            region.setVisible(visible);
            region.setManaged(visible);
        }
    }

    private void setText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }
}
