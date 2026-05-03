package com.UI.cab302_project.controller;

import com.Database.SessionManager;
import com.Database.SqliteChallengeDAO;
import com.Database.SqliteSolutionDAO;
import com.Model.Challenge;
import com.Model.Solution;
import com.Model.TestCase;
import com.Runner.CodeExecutor;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

import static com.UI.cab302_project.util.ScreenManager.switchScreen;

public class CodingChallengeController {

    private static String currentChallengeId = "ch-001";

    public static void setCurrentChallengeId(String id) {
        currentChallengeId = id;
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

    private SqliteChallengeDAO challengeDAO;
    private SqliteSolutionDAO solutionDAO;
    private SessionManager sessionManager;
    private Challenge currentChallenge;

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

                switchScreen(event, "/com/example/cab302_project/dashboard.fxml", 1200, 800);
            } else {
                showError("Failed to save solution. Please try again.");
            }
        }
    }

    @FXML
    private void onBack(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/cab302_project/dashboard.fxml", 1200, 800);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
