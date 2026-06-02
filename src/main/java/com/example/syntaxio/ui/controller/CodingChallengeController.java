package com.example.syntaxio.ui.controller;

import com.example.syntaxio.database.SessionManager;
import com.example.syntaxio.database.SqliteChallengeDAO;
import com.example.syntaxio.database.SqliteSolutionDAO;
import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.Solution;
import com.example.syntaxio.model.TestCase;
import com.example.syntaxio.runner.CodeExecutor;
import com.example.syntaxio.ui.util.ScreenManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class CodingChallengeController {

    static final String MAIN_MENU_FXML = "/com/example/syntaxio/main-menu.fxml";
    static final double MAIN_MENU_WIDTH = 1200;
    static final double MAIN_MENU_HEIGHT = 1150;
    static final String DASHBOARD_FXML = "/com/example/syntaxio/dashboard.fxml";
    static final double DASHBOARD_WIDTH = 1200;
    static final double DASHBOARD_HEIGHT = 800;

    private static String currentChallengeId = "ch-001";

    public static void setCurrentChallengeId(String id) {
        currentChallengeId = id;
    }

    @FunctionalInterface
    interface ScreenSwitcher {
        void switchScreen(ActionEvent event, String fxmlPath, double width, double height) throws IOException;
    }

    @FXML private Label titleLabel;
    @FXML private Label difficultyLabel;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea codeEditor;
    @FXML private TextArea outputArea;
    @FXML private Button runButton;
    @FXML private Button submitButton;
    @FXML private Button backButton;
    @FXML private VBox testResultsContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label timeIndicator;

    private SqliteChallengeDAO challengeDAO;
    private SqliteSolutionDAO solutionDAO;
    private SessionManager sessionManager;
    private Challenge currentChallenge;
    private ScreenSwitcher screenSwitcher = ScreenManager::switchScreen;
    private Timeline stopwatch;
    private int elapsedSeconds;

    void setScreenSwitcher(ScreenSwitcher screenSwitcher) {
        this.screenSwitcher = screenSwitcher;
    }

    @FXML
    public void initialize() {
        challengeDAO = new SqliteChallengeDAO();
        solutionDAO = new SqliteSolutionDAO();
        sessionManager = SessionManager.getInstance();

        loadingIndicator.setVisible(false);

        loadChallenge(currentChallengeId);
    }

    public void loadChallenge(String challengeId) {
        currentChallenge = challengeDAO.getChallengeById(challengeId);
        if (currentChallenge != null) {
            displayChallenge();
            startStopwatch();
        } else {
            showError("Challenge not found!");
        }
    }

    private void displayChallenge() {
        titleLabel.setText(currentChallenge.getTitle());
        difficultyLabel.setText(currentChallenge.getDifficulty());
        difficultyLabel.setStyle("-fx-text-fill: " + currentChallenge.getDifficultyColor() + ";");
        descriptionArea.setText(currentChallenge.getDescription());
        codeEditor.setText(currentChallenge.getStarterCode());

        testResultsContainer.getChildren().clear();
        outputArea.clear();
    }

    private void startStopwatch() {
        stopStopwatch();
        elapsedSeconds = 0;
        updateTimerLabel();

        stopwatch = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            elapsedSeconds++;
            updateTimerLabel();
        }));
        stopwatch.setCycleCount(Animation.INDEFINITE);
        stopwatch.play();
    }

    private void stopStopwatch() {
        if (stopwatch != null) {
            stopwatch.stop();
            stopwatch = null;
        }
    }

    private void updateTimerLabel() {
        timeIndicator.setText(formatElapsedTime(elapsedSeconds));
    }

    static String formatElapsedTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @FXML
    private void onRun() {
        String userCode = codeEditor.getText();

        if (userCode.trim().isEmpty()) {
            outputArea.setText("Please write some code first!");
            return;
        }

        loadingIndicator.setVisible(true);
        runButton.setDisable(true);

        new Thread(() -> {
            List<TestCase> results = CodeExecutor.executeTests(userCode, currentChallenge.getTestCases());

            Platform.runLater(() -> {
                displayTestResults(results);
                loadingIndicator.setVisible(false);
                runButton.setDisable(false);
            });
        }).start();
    }

    private void displayTestResults(List<TestCase> results) {
        testResultsContainer.getChildren().clear();

        int passedCount = 0;
        StringBuilder output = new StringBuilder();

        for (TestCase test : results) {
            VBox resultCard = new VBox(5);
            resultCard.setStyle("-fx-background-color: " + (test.isPassed() ? "#2ecc7133" : "#e74c3c33") +
                               "; -fx-padding: 10; -fx-background-radius: 5;");

            Label statusLabel = new Label(test.isPassed() ? "✓ PASSED" : "✗ FAILED");
            statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                                (test.isPassed() ? "#2ecc71" : "#e74c3c") + ";");

            Label descriptionLabel = new Label("Test: " + test.getDescription());
            Label expectedLabel = new Label("Expected: " + test.getExpectedOutput());
            Label actualLabel = new Label("Actual: " + test.getActualOutput());

            resultCard.getChildren().addAll(statusLabel, descriptionLabel, expectedLabel, actualLabel);
            testResultsContainer.getChildren().add(resultCard);

            if (test.isPassed()) passedCount++;

            output.append(test.isPassed() ? "✓ " : "✗ ").append(test.getDescription())
                  .append(" | Expected: ").append(test.getExpectedOutput())
                  .append(" | Got: ").append(test.getActualOutput()).append("\n");
        }

        outputArea.setText("Results: " + passedCount + "/" + results.size() + " tests passed\n\n" + output);
        submitButton.setDisable(passedCount != results.size());
    }

    @FXML
    private void onSubmit(ActionEvent event) throws IOException {
        if (currentChallenge == null) return;

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Submit Solution");
        confirmDialog.setHeaderText("Submit your solution?");
        confirmDialog.setContentText("Once submitted, you can't edit this solution. You can still try the challenge again later.");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Solution solution = new Solution(
                currentChallenge.getId(),
                codeEditor.getText(),
                true,
                0
            );

            int userId = sessionManager.getCurrentUser().getId();
            boolean saved = solutionDAO.addSolution(userId, solution);

            if (saved) {
                sessionManager.getCurrentUser().incrementChallengesCompleted();
                sessionManager.getUserDAO().updateUser(sessionManager.getCurrentUser());

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success!");
                successAlert.setHeaderText("Solution Submitted!");
                successAlert.setContentText("Great work! Your solution has been saved.");
                successAlert.showAndWait();

                stopStopwatch();
                screenSwitcher.switchScreen(event, DASHBOARD_FXML, DASHBOARD_WIDTH, DASHBOARD_HEIGHT);
            } else {
                showError("Failed to save solution. Please try again.");
            }
        }
    }

    @FXML
    private void onBack(ActionEvent event) throws IOException {
        stopStopwatch();
        screenSwitcher.switchScreen(event, MAIN_MENU_FXML, MAIN_MENU_WIDTH, MAIN_MENU_HEIGHT);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
