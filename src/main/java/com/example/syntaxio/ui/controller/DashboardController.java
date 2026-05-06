package com.example.syntaxio.ui.controller;

import com.example.syntaxio.database.SessionManager;
import com.example.syntaxio.database.SqliteChallengeDAO;
import com.example.syntaxio.database.SqliteSolutionDAO;
import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.Solution;
import com.example.syntaxio.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.syntaxio.ui.util.ScreenManager.switchScreen;

public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label usernameLabel;
    @FXML private Label statsLabel;
    @FXML private Label joinDateLabel;
    @FXML private Label hintsUsedLabel;
    @FXML private Label completionRateLabel;

    @FXML private PieChart progressChart;
    @FXML private BarChart<String, Number> activityChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private VBox recentActivityContainer;
    @FXML private VBox quickActionsContainer;
    @FXML private ProgressBar overallProgressBar;
    @FXML private Label progressPercentageLabel;

    private SessionManager sessionManager;
    private SqliteChallengeDAO challengeDAO;
    private SqliteSolutionDAO solutionDAO;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        challengeDAO = new SqliteChallengeDAO();
        solutionDAO = new SqliteSolutionDAO();

        setupUI();
        loadDashboardData();
        setupQuickActions();
    }

    private void setupUI() {
        User currentUser = sessionManager.getCurrentUser();

        welcomeLabel.setText("Welcome back, " + currentUser.getUsername() + "!");
        usernameLabel.setText(currentUser.getUsername());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        joinDateLabel.setText("Member since: " + currentUser.getCreatedAt().format(formatter));

        progressChart.setTitle("Challenge Progress");
        progressChart.setLegendVisible(true);
        progressChart.setLabelsVisible(true);

        activityChart.setTitle("Weekly Activity");
        activityChart.setLegendVisible(false);
        activityChart.setAnimated(false);
        xAxis.setLabel("Day");
        yAxis.setLabel("Solutions Submitted");
    }

    private void loadDashboardData() {
        new Thread(() -> {
            User user = sessionManager.getCurrentUser();
            List<Challenge> allChallenges = challengeDAO.getAllChallenges();
            List<Solution> userSolutions = solutionDAO.getSolutionsByUserId(user.getId());

            int completedCount = user.getTotalChallengesCompleted();
            int totalChallenges = allChallenges.size();
            double completionRate = totalChallenges > 0 ? (double) completedCount / totalChallenges * 100 : 0;
            int totalHints = user.getTotalHintsUsed();

            Platform.runLater(() -> {
                statsLabel.setText(completedCount + " / " + totalChallenges + " Challenges Completed");
                hintsUsedLabel.setText(totalHints + " hints used");
                completionRateLabel.setText(String.format("%.1f%% completion rate", completionRate));

                overallProgressBar.setProgress(completionRate / 100);
                progressPercentageLabel.setText(String.format("%.0f%%", completionRate));

                updatePieChart(completedCount, totalChallenges - completedCount);
                updateActivityChart(userSolutions);
                updateRecentActivity(userSolutions);
            });
        }).start();
    }

    private void updatePieChart(int completed, int remaining) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Completed (" + completed + ")", completed),
            new PieChart.Data("Remaining (" + remaining + ")", remaining)
        );

        progressChart.setData(pieChartData);

        // Nodes only exist after the chart has been rendered
        Platform.runLater(() -> pieChartData.forEach(data -> {
            if (data.getName().startsWith("Completed")) {
                data.getNode().setStyle("-fx-pie-color: #2ecc71;");
            } else {
                data.getNode().setStyle("-fx-pie-color: #e74c3c;");
            }
        }));
    }

    private void updateActivityChart(List<Solution> solutions) {
        Map<String, Long> activityByDay = solutions.stream()
            .collect(Collectors.groupingBy(
                s -> s.getSubmittedAt().getDayOfWeek().toString(),
                Collectors.counting()
            ));

        List<String> days = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Solutions");

        for (String day : days) {
            long count = activityByDay.getOrDefault(day, 0L);
            series.getData().add(new XYChart.Data<>(day.substring(0, 3), count));
        }

        activityChart.getData().clear();
        activityChart.getData().add(series);
    }

    private void updateRecentActivity(List<Solution> solutions) {
        recentActivityContainer.getChildren().clear();

        List<Solution> recent = solutions.stream()
            .limit(5)
            .collect(Collectors.toList());

        if (recent.isEmpty()) {
            Label noActivity = new Label("No activity yet. Start coding!");
            noActivity.setStyle("-fx-text-fill: #c8d6e5;");
            recentActivityContainer.getChildren().add(noActivity);
        } else {
            for (Solution solution : recent) {
                recentActivityContainer.getChildren().add(createActivityItem(solution));
            }
        }
    }

    private VBox createActivityItem(Solution solution) {
        VBox item = new VBox(5);
        item.setStyle("-fx-background-color: #16213e; -fx-padding: 10; -fx-background-radius: 5;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label challengeLabel = new Label("📝 Challenge: " + solution.getChallengeId());
        challengeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4ecdc4;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label(solution.getSubmittedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")));
        dateLabel.setStyle("-fx-text-fill: #c8d6e5; -fx-font-size: 11px;");

        header.getChildren().addAll(challengeLabel, spacer, dateLabel);

        Label statusLabel = new Label(solution.isPassed() ? "✓ Completed successfully!" : "✗ Attempted");
        statusLabel.setStyle("-fx-text-fill: " + (solution.isPassed() ? "#2ecc71" : "#f39c12") + "; -fx-font-size: 12px;");

        Label hintsLabel = new Label("💡 Hints used: " + solution.getHintsUsedForThisSolution());
        hintsLabel.setStyle("-fx-text-fill: #f9ca24; -fx-font-size: 11px;");

        item.getChildren().addAll(header, statusLabel, hintsLabel);
        return item;
    }

    private void setupQuickActions() {
        Button continueButton = new Button("▶ Continue Coding");
        continueButton.setStyle("-fx-background-color: #4ecdc4; -fx-background-radius: 8; -fx-padding: 10; -fx-font-weight: bold;");
        continueButton.setMaxWidth(Double.MAX_VALUE);
        continueButton.setOnAction(e -> navigateTo(e, "/com/example/syntaxio/challenge-browser.fxml"));

        Button browseButton = new Button("📚 Browse All Challenges");
        browseButton.setStyle("-fx-background-color: #16213e; -fx-background-radius: 8; -fx-padding: 10; -fx-text-fill: #4ecdc4;");
        browseButton.setMaxWidth(Double.MAX_VALUE);
        browseButton.setOnAction(e -> navigateTo(e, "/com/example/syntaxio/challenge-browser.fxml"));

        Button statsButton = new Button("📊 View Detailed Stats");
        statsButton.setStyle("-fx-background-color: #16213e; -fx-background-radius: 8; -fx-padding: 10; -fx-text-fill: #4ecdc4;");
        statsButton.setMaxWidth(Double.MAX_VALUE);
        statsButton.setOnAction(e -> showDetailedStats());

        quickActionsContainer.getChildren().addAll(continueButton, browseButton, statsButton);
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            switchScreen(event, fxmlPath, 1200, 800);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    private void showDetailedStats() {
        User user = sessionManager.getCurrentUser();
        int total = challengeDAO.getAllChallenges().size();
        double rate = total > 0 ? (double) user.getTotalChallengesCompleted() / total * 100 : 0;

        String stats = String.format("""
            ┌─────────────────────────────────┐
            │     📊 CODEBUDDY STATISTICS     │
            ├─────────────────────────────────┤
            │                                 │
            │  👤 Username: %s                │
            │  📅 Joined: %s                  │
            │                                 │
            │  📚 Challenges Completed: %d    │
            │  💡 Total Hints Used: %d        │
            │  ⭐ Points Earned: %d           │
            │  🎯 Completion Rate: %.1f%%     │
            │                                 │
            └─────────────────────────────────┘
            """,
            user.getUsername(),
            user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
            user.getTotalChallengesCompleted(),
            user.getTotalHintsUsed(),
            user.getTotalChallengesCompleted() * 10,
            rate
        );

        TextArea statsArea = new TextArea(stats);
        statsArea.setEditable(false);
        statsArea.setStyle("-fx-font-family: monospace; -fx-font-size: 13px; -fx-background-color: #16213e; -fx-text-fill: #4ecdc4;");

        Alert statsDialog = new Alert(Alert.AlertType.INFORMATION);
        statsDialog.setTitle("Detailed Statistics");
        statsDialog.setHeaderText("Your Coding Journey Stats");
        statsDialog.getDialogPane().setContent(statsArea);
        statsDialog.getDialogPane().setPrefWidth(500);
        statsDialog.showAndWait();
    }

    @FXML
    private void onLogout(ActionEvent event) throws IOException {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Logout");
        confirmDialog.setHeaderText("Are you sure you want to logout?");
        confirmDialog.setContentText("Your progress will be saved.");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            sessionManager.logout();
            switchScreen(event, "/com/example/syntaxio/login-screen.fxml", 350, 650);
        }
    }
}
