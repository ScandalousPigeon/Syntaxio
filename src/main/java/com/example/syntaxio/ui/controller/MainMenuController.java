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
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import static com.example.syntaxio.ui.util.ScreenManager.switchScreen;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MainMenuController {

    private static final double MIN_MESSAGE_WIDTH = 220.0;
    private static final double MAX_MESSAGE_WIDTH = 900.0;
    private static final double MESSAGE_WIDTH_RATIO = 0.78;
    private static final int SUGGESTED_PUZZLE_LIMIT = 6;
    private static final int SUGGESTED_PUZZLE_COLUMNS = 2;
    private static final int SUGGESTED_DESCRIPTION_LIMIT = 90;
    private static final double SUGGESTED_CARD_HEIGHT = 100.0;
    private static final int XP_PER_COMPLETED_CHALLENGE = 100;
    private static final int XP_PER_LEVEL = 1000;
    private static final DateTimeFormatter UPDATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter CHAT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    @FXML
    private VBox popoutMenu;

    @FXML
    private StackPane rootPane;

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
    private Text assistantStatusText;

    @FXML
    private GridPane suggestedPuzzlesGrid;

    @FXML
    private Label profileLevelBadge;

    @FXML
    private Text profileUsernameText;

    @FXML
    private Text profileStreakText;

    @FXML
    private Text profileXpText;

    @FXML
    private ProgressBar profileXpProgressBar;

    @FXML
    private Text menuLevelText;

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
    private SessionManager sessionManager;
    private SqliteChallengeDAO challengeDAO;
    private boolean menuOpen = false;
    private TranslateTransition currentAnimation;
    private SqliteInProgressChallengeDAO inProgressChallengeDAO;
    private String currentInProgressChallengeId;

    @FXML
    private void initialize() {
        sessionManager = SessionManager.getInstance();
        challengeDAO = new SqliteChallengeDAO();
        inProgressChallengeDAO = new SqliteInProgressChallengeDAO();

        //dimOverlay.setVisible(false);
        loadProfileCard();
        Platform.runLater(this::maximizeMainMenuWindow);

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

        configureChatControls();
        addMessageBubble(
                "Hi! I'm your AI coding assistant. Ask me about Java, algorithms, data structures, or what to practice next.",
                false
        );

        loadSuggestedPuzzles();
        renderInProgressCard();
    }

    private void loadProfileCard() {
        sessionManager.refreshCurrentUser();
        renderProfileCard(sessionManager.getCurrentUser());
    }

    private void renderProfileCard(User user) {
        if (user == null) {
            setText(profileUsernameText, "Guest");
            setText(profileStreakText, "0 Day Streak");
            setText(profileXpText, "0 / " + XP_PER_LEVEL + " XP");
            setText(menuLevelText, "Explorer Level 1");
            setLabelText(profileLevelBadge, "1");
            setProgress(profileXpProgressBar, 0);
            return;
        }

        int completedChallenges = Math.max(0, user.getTotalChallengesCompleted());
        int totalXp = completedChallenges * XP_PER_COMPLETED_CHALLENGE;
        int level = calculateLevel(totalXp);
        int nextLevelXp = level * XP_PER_LEVEL;
        int streak = Math.max(0, user.getCurrentActivityStreak());

        setText(profileUsernameText, user.getUsername());
        setText(profileStreakText, String.format(Locale.ENGLISH,
                "%d Day%s Streak",
                streak,
                streak == 1 ? "" : "s"
        ));
        setText(profileXpText, totalXp + " / " + nextLevelXp + " XP");
        setText(menuLevelText, "Explorer Level " + level);
        setLabelText(profileLevelBadge, String.valueOf(level));
        setProgress(profileXpProgressBar, totalXp / (double) nextLevelXp);
    }

    private int calculateLevel(int totalXp) {
        return Math.max(1, totalXp / XP_PER_LEVEL + 1);
    }

    private void setText(Text text, String value) {
        if (text != null) {
            text.setText(value);
        }
    }

    private void setLabelText(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    private void setProgress(ProgressBar progressBar, double value) {
        if (progressBar != null) {
            progressBar.setProgress(Math.max(0, Math.min(1, value)));
        }
    }

    private void maximizeMainMenuWindow() {
        if (rootPane == null
                || rootPane.getScene() == null
                || !(rootPane.getScene().getWindow() instanceof Stage stage)) {
            return;
        }

        stage.setMaximized(true);
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
        setText(assistantStatusText, "Thinking...");
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
            setText(assistantStatusText, "Online");
            setChatControlsDisabled(false);
            scrollToLatestMessage();
        });

        replyTask.setOnFailed(event -> {
            pendingBubble.setText("I couldn't reach Ollama yet. It may still be starting, or "
                    + OllamaClient.DEFAULT_MODEL
                    + " may still be downloading. Make sure Ollama is installed if this keeps happening.");
            setText(assistantStatusText, "Offline");
            setChatControlsDisabled(false);
            scrollToLatestMessage();
        });

        Thread replyThread = new Thread(replyTask, "main-menu-assistant-reply");
        replyThread.setDaemon(true);
        replyThread.start();
    }

    private void configureChatControls() {
        if (chatInput == null || sendButton == null) {
            return;
        }

        chatInput.setOnAction(event -> handleSendMessage());
        chatInput.textProperty().addListener((obs, oldValue, newValue) -> updateSendButtonState());
        updateSendButtonState();
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

        Label metaLabel = new Label((fromUser ? "You" : "Syntaxio") + " • "
                + LocalTime.now().format(CHAT_TIME_FORMATTER));
        metaLabel.getStyleClass().add(fromUser ? "user-message-meta" : "bot-message-meta");

        VBox messageGroup = new VBox(4);
        messageGroup.getChildren().addAll(row, metaLabel);
        messageGroup.getStyleClass().add(fromUser ? "user-message-group" : "bot-message-group");

        chatContent.getChildren().add(messageGroup);
        scrollToLatestMessage();

        return bubble;
    }

    private void setChatControlsDisabled(boolean disabled) {
        chatInput.setDisable(disabled);
        sendButton.setDisable(disabled || chatInput.getText().trim().isEmpty());
    }

    private void updateSendButtonState() {
        if (sendButton != null && chatInput != null) {
            sendButton.setDisable(chatInput.getText().trim().isEmpty());
        }
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
