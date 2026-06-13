package com.example.syntaxio.ui.controller;

import com.example.syntaxio.database.SessionManager;
import com.example.syntaxio.database.SqliteChallengeDAO;
import com.example.syntaxio.database.SqliteSolutionDAO;
import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.Solution;
import com.example.syntaxio.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.syntaxio.ui.util.ScreenManager.switchScreen;

public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label usernameLabel;
    @FXML private Label joinDateLabel;
    @FXML private Label memberSinceLabel;
    @FXML private Label statsLabel;
    @FXML private Label completedChallengesLabel;
    @FXML private Label totalChallengesLabel;
    @FXML private Label completionRateLabel;
    @FXML private Label progressPercentageLabel;
    @FXML private Label hintsUsedLabel;
    @FXML private Label submissionsLabel;
    @FXML private Label lastSubmissionLabel;
    @FXML private Label easyProgressLabel;
    @FXML private Label mediumProgressLabel;
    @FXML private Label hardProgressLabel;
    @FXML private Label dashboardMessageLabel;
    @FXML private Label recentActivity1Label;
    @FXML private Label recentActivity2Label;
    @FXML private Label recentActivity3Label;
    @FXML private Label recentActivity4Label;
    @FXML private Label recentActivity5Label;
    @FXML private Label achievement1Label;
    @FXML private Label achievement2Label;
    @FXML private Label achievement3Label;
    @FXML private Label achievement4Label;

    @FXML private ProgressBar overallProgressBar;
    @FXML private PieChart progressChart;
    @FXML private BarChart<String, Number> activityChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private SessionManager sessionManager;
    private SqliteChallengeDAO challengeDAO;
    private SqliteSolutionDAO solutionDAO;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        challengeDAO = new SqliteChallengeDAO();
        solutionDAO = new SqliteSolutionDAO();

        configureCharts();

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            renderSignedOutState();
            return;
        }

        renderUserHeader(currentUser);
        loadDashboardData(currentUser);
    }

    @FXML
    private void onBackToHome(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/syntaxio/main-menu.fxml", 1200, 1150);
    }

    @FXML
    private void onBrowseChallenges(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/syntaxio/coding-challenge-browser.fxml", 1200, 1000);
    }

    @FXML
    private void onLogout(ActionEvent event) throws IOException {
        sessionManager.logout();
        switchScreen(event, "/com/example/syntaxio/login-screen.fxml", 350, 650);
    }

    @FXML
    private void onRefresh() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            renderSignedOutState();
            return;
        }

        sessionManager.refreshCurrentUser();
        renderUserHeader(sessionManager.getCurrentUser());
        loadDashboardData(sessionManager.getCurrentUser());
    }

    @FXML
    private void onShowDetailedStats() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            showInfo("Dashboard", "Sign in to view dashboard statistics.");
            return;
        }

        List<Challenge> challenges = challengeDAO.getAllChallenges();
        List<Solution> solutions = solutionDAO.getSolutionsByUserId(currentUser.getId());
        DashboardStats stats = buildStats(currentUser, challenges, solutions);

        showInfo("Detailed Statistics", String.format(Locale.ENGLISH,
                """
                User: %s
                Member since: %s
                Challenges completed: %d / %d
                Completion rate: %.1f%%
                Submissions: %d
                Hints used: %d
                """,
                currentUser.getUsername(),
                formatDate(currentUser.getCreatedAt().toLocalDate()),
                stats.completedChallenges(),
                stats.totalChallenges(),
                stats.completionRate(),
                stats.totalSubmissions(),
                stats.hintsUsed()
        ));
    }

    private void configureCharts() {
        if (progressChart != null) {
            progressChart.setLegendVisible(true);
            progressChart.setLabelsVisible(true);
        }

        if (activityChart != null) {
            activityChart.setLegendVisible(false);
            activityChart.setAnimated(false);
        }

        if (xAxis != null) {
            xAxis.setLabel("Day");
        }

        if (yAxis != null) {
            yAxis.setLabel("Submissions");
            yAxis.setMinorTickVisible(false);
            yAxis.setTickUnit(1);
        }
    }

    private void renderUserHeader(User user) {
        setText(welcomeLabel, "Welcome back, " + user.getUsername() + "!");
        setText(usernameLabel, user.getUsername());
        setText(joinDateLabel, "Member since " + formatDate(user.getCreatedAt().toLocalDate()));
        setText(memberSinceLabel, formatDate(user.getCreatedAt().toLocalDate()));
        setText(dashboardMessageLabel, "");
    }

    private void loadDashboardData(User user) {
        Thread loadThread = new Thread(() -> {
            List<Challenge> challenges = challengeDAO.getAllChallenges();
            List<Solution> solutions = solutionDAO.getSolutionsByUserId(user.getId());
            DashboardStats stats = buildStats(user, challenges, solutions);

            Platform.runLater(() -> renderDashboard(stats));
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    private DashboardStats buildStats(User user, List<Challenge> challenges, List<Solution> solutions) {
        Set<String> completedChallengeIds = solutions.stream()
                .filter(Solution::isPassed)
                .map(Solution::getChallengeId)
                .collect(Collectors.toSet());

        int totalChallenges = challenges.size();
        int completedChallenges = completedChallengeIds.size();
        int totalSubmissions = solutions.size();
        int hintsUsed = Math.max(
                user.getTotalHintsUsed(),
                solutions.stream().mapToInt(Solution::getHintsUsedForThisSolution).sum()
        );
        double completionRate = totalChallenges == 0
                ? 0
                : completedChallenges * 100.0 / totalChallenges;

        return new DashboardStats(
                challenges,
                solutions,
                completedChallengeIds,
                totalChallenges,
                completedChallenges,
                totalSubmissions,
                hintsUsed,
                completionRate
        );
    }

    private void renderDashboard(DashboardStats stats) {
        setText(statsLabel, stats.completedChallenges() + " / " + stats.totalChallenges() + " Challenges Completed");
        setText(completedChallengesLabel, String.valueOf(stats.completedChallenges()));
        setText(totalChallengesLabel, String.valueOf(stats.totalChallenges()));
        setText(completionRateLabel, String.format(Locale.ENGLISH, "%.1f%% completion rate", stats.completionRate()));
        setText(progressPercentageLabel, String.format(Locale.ENGLISH, "%.0f%%", stats.completionRate()));
        setText(hintsUsedLabel, stats.hintsUsed() + " hints used");
        setText(submissionsLabel, stats.totalSubmissions() + " submissions");
        setText(lastSubmissionLabel, formatLastSubmission(stats.solutions()));
        setProgress(overallProgressBar, stats.completionRate() / 100.0);

        renderProgressChart(stats);
        renderActivityChart(stats.solutions());
        renderSkillProgress(stats);
        renderRecentActivity(stats);
        renderAchievements(stats);
    }

    private void renderProgressChart(DashboardStats stats) {
        if (progressChart == null) return;

        int remaining = Math.max(0, stats.totalChallenges() - stats.completedChallenges());
        progressChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Completed", stats.completedChallenges()),
                new PieChart.Data("Remaining", remaining)
        ));
    }

    private void renderActivityChart(List<Solution> solutions) {
        if (activityChart == null) return;

        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> submissionsByDate = solutions.stream()
                .collect(Collectors.groupingBy(
                        solution -> solution.getSubmittedAt().toLocalDate(),
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            LocalDate date = today.minusDays(daysAgo);
            String label = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            series.getData().add(new XYChart.Data<>(label, submissionsByDate.getOrDefault(date, 0L)));
        }

        activityChart.getData().clear();
        activityChart.getData().add(series);
    }

    private void renderSkillProgress(DashboardStats stats) {
        setText(easyProgressLabel, formatDifficultyProgress(stats, "EASY"));
        setText(mediumProgressLabel, formatDifficultyProgress(stats, "MEDIUM"));
        setText(hardProgressLabel, formatDifficultyProgress(stats, "HARD"));
    }

    private String formatDifficultyProgress(DashboardStats stats, String difficulty) {
        long total = stats.challenges().stream()
                .filter(challenge -> difficulty.equals(challenge.getDifficulty()))
                .count();
        long completed = stats.challenges().stream()
                .filter(challenge -> difficulty.equals(challenge.getDifficulty()))
                .filter(challenge -> stats.completedChallengeIds().contains(challenge.getId()))
                .count();

        return toTitleCase(difficulty) + ": " + completed + " / " + total;
    }

    private void renderRecentActivity(DashboardStats stats) {
        List<Solution> recentSolutions = stats.solutions().stream()
                .sorted(Comparator.comparing(Solution::getSubmittedAt).reversed())
                .limit(5)
                .toList();

        if (recentSolutions.isEmpty()) {
            setLabelList(List.of("No submissions yet."),
                    recentActivity1Label,
                    recentActivity2Label,
                    recentActivity3Label,
                    recentActivity4Label,
                    recentActivity5Label
            );
            return;
        }

        Map<String, String> challengeTitles = stats.challenges().stream()
                .collect(Collectors.toMap(Challenge::getId, Challenge::getTitle, (first, second) -> first));

        List<String> activityLines = recentSolutions.stream()
                .map(solution -> formatActivityLine(solution, challengeTitles))
                .toList();

        setLabelList(activityLines,
                recentActivity1Label,
                recentActivity2Label,
                recentActivity3Label,
                recentActivity4Label,
                recentActivity5Label
        );
    }

    private String formatActivityLine(Solution solution, Map<String, String> challengeTitles) {
        String challengeTitle = challengeTitles.getOrDefault(solution.getChallengeId(), solution.getChallengeId());
        String status = solution.isPassed() ? "Passed" : "Attempted";
        String submittedAt = solution.getSubmittedAt().format(DateTimeFormatter.ofPattern("MMM d, h:mm a"));
        return status + " " + challengeTitle + " - " + submittedAt;
    }

    private void renderAchievements(DashboardStats stats) {
        List<String> achievements = buildAchievementLines(stats);
        String fallback = "Complete a challenge to unlock achievements.";

        if (achievements.isEmpty()) {
            setLabelList(List.of(fallback),
                    achievement1Label,
                    achievement2Label,
                    achievement3Label,
                    achievement4Label
            );
            return;
        }

        setLabelList(achievements,
                achievement1Label,
                achievement2Label,
                achievement3Label,
                achievement4Label
        );
    }

    private List<String> buildAchievementLines(DashboardStats stats) {
        return List.of(
                        stats.completedChallenges() > 0 ? "First completion unlocked" : "",
                        stats.totalChallenges() > 0 && stats.completedChallenges() == stats.totalChallenges()
                                ? "All current challenges completed"
                                : "",
                        stats.totalSubmissions() >= 5 ? "Five submissions made" : "",
                        stats.hintsUsed() == 0 && stats.completedChallenges() > 0 ? "Completed without hints" : ""
                )
                .stream()
                .filter(line -> !line.isBlank())
                .toList();
    }

    private String formatLastSubmission(List<Solution> solutions) {
        return solutions.stream()
                .max(Comparator.comparing(Solution::getSubmittedAt))
                .map(solution -> solution.getSubmittedAt().format(DateTimeFormatter.ofPattern("MMM d, h:mm a")))
                .orElse("No submissions yet");
    }

    private void renderSignedOutState() {
        setText(dashboardMessageLabel, "Sign in to view your dashboard.");
        setText(welcomeLabel, "Dashboard");
        setText(usernameLabel, "Guest");
        setText(joinDateLabel, "Track your progress and achievements");
        setText(memberSinceLabel, "Not signed in");
        setText(statsLabel, "Sign in required");
        setText(completedChallengesLabel, "0");
        setText(totalChallengesLabel, "0");
        setText(completionRateLabel, "0.0% completion rate");
        setText(progressPercentageLabel, "0%");
        setText(hintsUsedLabel, "0 hints used");
        setText(submissionsLabel, "0 submissions");
        setText(lastSubmissionLabel, "No submissions yet");
        setText(easyProgressLabel, "Easy: 0 / 0");
        setText(mediumProgressLabel, "Medium: 0 / 0");
        setText(hardProgressLabel, "Hard: 0 / 0");
        setLabelList(List.of("Sign in to view activity."),
                recentActivity1Label,
                recentActivity2Label,
                recentActivity3Label,
                recentActivity4Label,
                recentActivity5Label
        );
        setLabelList(List.of("Sign in to view achievements."),
                achievement1Label,
                achievement2Label,
                achievement3Label,
                achievement4Label
        );
        setProgress(overallProgressBar, 0);

        if (progressChart != null) {
            progressChart.getData().clear();
        }

        if (activityChart != null) {
            activityChart.getData().clear();
        }
    }

    private void setLabelList(List<String> lines, Label... labels) {
        for (int index = 0; index < labels.length; index++) {
            String text = index < lines.size() ? lines.get(index) : "";
            setText(labels[index], text);
        }
    }

    private void setText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }

    private void setProgress(ProgressBar progressBar, double value) {
        if (progressBar != null) {
            progressBar.setProgress(Math.max(0, Math.min(1, value)));
        }
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    private String toTitleCase(String value) {
        String lower = value.toLowerCase(Locale.ENGLISH);
        return lower.substring(0, 1).toUpperCase(Locale.ENGLISH) + lower.substring(1);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record DashboardStats(
            List<Challenge> challenges,
            List<Solution> solutions,
            Set<String> completedChallengeIds,
            int totalChallenges,
            int completedChallenges,
            int totalSubmissions,
            int hintsUsed,
            double completionRate
    ) {
    }
}
