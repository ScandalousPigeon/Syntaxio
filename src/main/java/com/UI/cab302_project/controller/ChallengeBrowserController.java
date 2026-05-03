package com.UI.cab302_project.controller;

import com.Database.SqliteChallengeDAO;
import com.Database.SqliteSolutionDAO;
import com.Database.SessionManager;
import com.Model.Challenge;
import com.Model.Solution;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.UI.cab302_project.util.ScreenManager.switchScreen;

public class ChallengeBrowserController {
    @FXML private FlowPane challengesContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label loadingLabel;
    @FXML private Label resultsCountLabel;

    private SqliteChallengeDAO challengeDAO;
    private SessionManager sessionManager;
    private ObservableList<Challenge> allChallenges;
    private FilteredList<Challenge> filteredChallenges;
    private List<String> completedChallengeIds;

    @FXML
    public void initialize() {
        challengeDAO = new SqliteChallengeDAO();
        sessionManager = SessionManager.getInstance();

        difficultyFilter.setItems(FXCollections.observableArrayList("All", "EASY", "MEDIUM", "HARD"));
        difficultyFilter.setValue("All");

        statusFilter.setItems(FXCollections.observableArrayList("All", "Not Started", "Completed"));
        statusFilter.setValue("All");

        if (sessionManager.getCurrentUser() != null) {
            SqliteSolutionDAO solutionDAO = new SqliteSolutionDAO();
            completedChallengeIds = solutionDAO
                .getSolutionsByUserId(sessionManager.getCurrentUser().getId())
                .stream()
                .filter(Solution::isPassed)
                .map(Solution::getChallengeId)
                .distinct()
                .collect(Collectors.toList());
        } else {
            completedChallengeIds = List.of();
        }

        loadChallenges();

        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        difficultyFilter.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, old, newVal) -> applyFilters());
    }

    private void loadChallenges() {
        loadingLabel.setVisible(true);
        challengesContainer.getChildren().clear();

        new Thread(() -> {
            List<Challenge> challenges = challengeDAO.getAllChallenges();
            Platform.runLater(() -> {
                allChallenges = FXCollections.observableArrayList(challenges);
                filteredChallenges = new FilteredList<>(allChallenges, p -> true);
                applyFilters();
                loadingLabel.setVisible(false);
            });
        }).start();
    }

    private void applyFilters() {
        if (filteredChallenges == null) return;

        String searchText = searchField.getText().toLowerCase();
        String difficulty = difficultyFilter.getValue();
        String status = statusFilter.getValue();

        Predicate<Challenge> predicate = challenge -> {
            if (!searchText.isEmpty()) {
                if (!challenge.getTitle().toLowerCase().contains(searchText) &&
                    !challenge.getDescription().toLowerCase().contains(searchText)) {
                    return false;
                }
            }

            if (!difficulty.equals("All") && !challenge.getDifficulty().equals(difficulty)) {
                return false;
            }

            boolean isCompleted = completedChallengeIds.contains(challenge.getId());
            if (status.equals("Completed") && !isCompleted) return false;
            if (status.equals("Not Started") && isCompleted) return false;

            return true;
        };

        filteredChallenges.setPredicate(predicate);
        displayChallenges();
    }

    private void displayChallenges() {
        challengesContainer.getChildren().clear();

        if (filteredChallenges.isEmpty()) {
            Label noResults = new Label("No challenges found. Try adjusting your filters.");
            noResults.setStyle("-fx-text-fill: #c8d6e5;");
            challengesContainer.getChildren().add(noResults);
            resultsCountLabel.setText("0 challenges");
            return;
        }

        resultsCountLabel.setText(filteredChallenges.size() + " challenges");

        for (Challenge challenge : filteredChallenges) {
            VBox challengeCard = createChallengeCard(challenge);
            challengesContainer.getChildren().add(challengeCard);
        }
    }

    private VBox createChallengeCard(Challenge challenge) {
        boolean isCompleted = completedChallengeIds.contains(challenge.getId());

        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10; -fx-padding: 15;");
        card.setPrefWidth(350);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(challenge.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label difficultyBadge = new Label(challenge.getDifficulty());
        difficultyBadge.setStyle("-fx-background-color: " + challenge.getDifficultyColor() +
                                 "; -fx-text-fill: #1a1a2e; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusIcon = new Label(isCompleted ? "✓ Completed" : "○ Not Started");
        statusIcon.setStyle("-fx-text-fill: " + (isCompleted ? "#2ecc71" : "#f39c12") + "; -fx-font-size: 12px;");

        header.getChildren().addAll(titleLabel, difficultyBadge, spacer, statusIcon);

        String shortDesc = challenge.getDescription().length() > 100 ?
                           challenge.getDescription().substring(0, 100) + "..." :
                           challenge.getDescription();
        Label descLabel = new Label(shortDesc);
        descLabel.setStyle("-fx-text-fill: #c8d6e5; -fx-wrap-text: true;");
        descLabel.setMaxWidth(320);

        HBox statsRow = new HBox(15);
        Label testCountLabel = new Label("📋 " + challenge.getTestCases().size() + " tests");
        testCountLabel.setStyle("-fx-text-fill: #4ecdc4; -fx-font-size: 12px;");

        Label difficultyPointsLabel = new Label(getDifficultyPoints(challenge.getDifficulty()));
        difficultyPointsLabel.setStyle("-fx-text-fill: #ffbe76; -fx-font-size: 12px;");

        statsRow.getChildren().addAll(testCountLabel, difficultyPointsLabel);

        Button startButton = new Button(isCompleted ? "Review Again" : "Start Challenge");
        startButton.setStyle("-fx-background-color: #4ecdc4; -fx-background-radius: 5; -fx-padding: 5 15; -fx-font-weight: bold;");
        startButton.setOnAction(e -> startChallenge(e, challenge.getId()));

        HBox buttonRow = new HBox();
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        buttonRow.getChildren().add(startButton);

        card.getChildren().addAll(header, descLabel, statsRow, buttonRow);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #1a2a4e; -fx-background-radius: 10; -fx-padding: 15;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10; -fx-padding: 15;"));

        return card;
    }

    private String getDifficultyPoints(String difficulty) {
        switch (difficulty) {
            case "EASY": return "⭐ 10 points";
            case "MEDIUM": return "⭐⭐ 25 points";
            case "HARD": return "⭐⭐⭐ 50 points";
            default: return "0 points";
        }
    }

    private void startChallenge(ActionEvent event, String challengeId) {
        try {
            CodingChallengeController.setCurrentChallengeId(challengeId);
            switchScreen(event, "/com/example/cab302_project/coding-challenge.fxml", 1200, 800);
        } catch (IOException e) {
            System.err.println("Error loading challenge screen: " + e.getMessage());
        }
    }

    @FXML
    private void onBackToDashboard(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/cab302_project/dashboard.fxml", 1200, 800);
    }
}
