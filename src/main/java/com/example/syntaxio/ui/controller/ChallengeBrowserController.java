package com.example.syntaxio.ui.controller;

import com.example.syntaxio.database.SessionManager;
import com.example.syntaxio.database.SqliteChallengeDAO;
import com.example.syntaxio.database.SqliteSolutionDAO;
import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.Solution;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.syntaxio.ui.util.ScreenManager.switchScreen;

public class ChallengeBrowserController {

    private static final int CARD_COLUMNS = 2;
    private static final double CARD_HEIGHT = 100.0;
    private static final double CATEGORY_PANEL_HEIGHT = 586.0;
    private static final double DEFAULT_LAYOUT_HEIGHT = 874.0;
    private static final int DESCRIPTION_PREVIEW_LENGTH = 110;
    private static final String ALL_DIFFICULTIES = "All";

    @FXML private TextField searchField;
    @FXML private ToggleButton allDifficultyButton;
    @FXML private ToggleButton easyDifficultyButton;
    @FXML private ToggleButton mediumDifficultyButton;
    @FXML private ToggleButton hardDifficultyButton;
    @FXML private GridPane challengesGrid;

    private SqliteChallengeDAO challengeDAO;
    private SessionManager sessionManager;
    private List<Challenge> allChallenges = List.of();
    private Set<String> completedChallengeIds = Set.of();

    @FXML
    public void initialize() {
        challengeDAO = new SqliteChallengeDAO();
        sessionManager = SessionManager.getInstance();
        completedChallengeIds = loadCompletedChallengeIds();

        configureChallengeGrid();
        configureDifficultyButtons();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        }

        loadChallenges();
    }

    private void configureChallengeGrid() {
        if (challengesGrid == null) {
            return;
        }

        challengesGrid.setMinHeight(Region.USE_PREF_SIZE);
        GridPane.setValignment(challengesGrid, VPos.TOP);
    }

    private Set<String> loadCompletedChallengeIds() {
        if (sessionManager.getCurrentUser() == null) {
            return Set.of();
        }

        SqliteSolutionDAO solutionDAO = new SqliteSolutionDAO();
        return solutionDAO
                .getSolutionsByUserId(sessionManager.getCurrentUser().getId())
                .stream()
                .filter(Solution::isPassed)
                .map(Solution::getChallengeId)
                .collect(Collectors.toSet());
    }

    private void configureDifficultyButtons() {
        if (allDifficultyButton == null
                || easyDifficultyButton == null
                || mediumDifficultyButton == null
                || hardDifficultyButton == null) {
            return;
        }

        allDifficultyButton.setUserData(ALL_DIFFICULTIES);
        easyDifficultyButton.setUserData("EASY");
        mediumDifficultyButton.setUserData("MEDIUM");
        hardDifficultyButton.setUserData("HARD");

        ToggleGroup group = allDifficultyButton.getToggleGroup();
        if (group == null) {
            group = new ToggleGroup();
            allDifficultyButton.setToggleGroup(group);
            easyDifficultyButton.setToggleGroup(group);
            mediumDifficultyButton.setToggleGroup(group);
            hardDifficultyButton.setToggleGroup(group);
        }

        ToggleGroup difficultyGroup = group;
        difficultyGroup.selectToggle(allDifficultyButton);
        difficultyGroup.selectedToggleProperty().addListener((obs, oldToggle, selectedToggle) -> {
            if (selectedToggle == null) {
                difficultyGroup.selectToggle(allDifficultyButton);
                return;
            }

            applyFilters();
        });
    }

    private void loadChallenges() {
        allChallenges = challengeDAO.getAllChallenges();
        applyFilters();
    }

    private void applyFilters() {
        if (challengesGrid == null) {
            return;
        }

        String searchText = normalizeSearch(searchField == null ? "" : searchField.getText());
        String selectedDifficulty = getSelectedDifficulty();

        List<Challenge> filteredChallenges = allChallenges.stream()
                .filter(challenge -> matchesSearch(challenge, searchText))
                .filter(challenge -> matchesDifficulty(challenge, selectedDifficulty))
                .toList();

        displayChallenges(filteredChallenges);
    }

    private boolean matchesSearch(Challenge challenge, String searchText) {
        if (searchText.isEmpty()) {
            return true;
        }

        return containsIgnoreCase(challenge.getTitle(), searchText)
                || containsIgnoreCase(challenge.getDescription(), searchText)
                || containsIgnoreCase(challenge.getDifficulty(), searchText);
    }

    private boolean matchesDifficulty(Challenge challenge, String selectedDifficulty) {
        return selectedDifficulty.equals(ALL_DIFFICULTIES)
                || selectedDifficulty.equals(challenge.getDifficulty());
    }

    private void displayChallenges(List<Challenge> challenges) {
        challengesGrid.getChildren().clear();
        challengesGrid.getRowConstraints().clear();

        if (challenges.isEmpty()) {
            resizeChallengeLayout(1);
            HBox emptyCard = createEmptyCard();
            GridPane.setColumnSpan(emptyCard, CARD_COLUMNS);
            challengesGrid.getChildren().add(emptyCard);
            return;
        }

        int rowCount = (int) Math.ceil(challenges.size() / (double) CARD_COLUMNS);
        resizeChallengeLayout(rowCount);

        for (int i = 0; i < challenges.size(); i++) {
            HBox card = createChallengeCard(challenges.get(i));
            GridPane.setColumnIndex(card, i % CARD_COLUMNS);
            GridPane.setRowIndex(card, i / CARD_COLUMNS);
            challengesGrid.getChildren().add(card);
        }
    }

    private void resizeChallengeLayout(int rowCount) {
        double challengeGridHeight = calculateChallengeGridHeight(rowCount);

        challengesGrid.setPrefHeight(challengeGridHeight);
        challengesGrid.setMinHeight(challengeGridHeight);
        addChallengeGridRows(rowCount);

        if (challengesGrid.getParent() instanceof GridPane layoutGrid) {
            double layoutHeight = Math.max(
                    DEFAULT_LAYOUT_HEIGHT,
                    Math.max(CATEGORY_PANEL_HEIGHT, challengeGridHeight)
            );

            layoutGrid.getRowConstraints().clear();
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(layoutHeight);
            rowConstraints.setPrefHeight(layoutHeight);
            layoutGrid.getRowConstraints().add(rowConstraints);

            layoutGrid.setMinHeight(layoutHeight);
            layoutGrid.setPrefHeight(layoutHeight);

            if (layoutGrid.getParent() instanceof VBox contentRoot) {
                contentRoot.setMinHeight(Region.USE_PREF_SIZE);
                contentRoot.setPrefHeight(Region.USE_COMPUTED_SIZE);
            }
        }
    }

    private double calculateChallengeGridHeight(int rowCount) {
        return rowCount * CARD_HEIGHT + Math.max(0, rowCount - 1) * challengesGrid.getVgap();
    }

    private void addChallengeGridRows(int rowCount) {
        for (int i = 0; i < rowCount; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(CARD_HEIGHT);
            rowConstraints.setPrefHeight(CARD_HEIGHT);
            challengesGrid.getRowConstraints().add(rowConstraints);
        }
    }

    private HBox createChallengeCard(Challenge challenge) {
        HBox card = new HBox();
        card.getStyleClass().add("content-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinHeight(CARD_HEIGHT);
        card.setPrefHeight(CARD_HEIGHT);
        card.setPrefWidth(200);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-cursor: hand;");
        card.setOnMouseClicked(event -> startChallenge(event, challenge.getId()));

        VBox body = new VBox(6);
        body.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(body, Priority.ALWAYS);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(challenge.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label difficultyLabel = new Label(challenge.getDifficulty());
        difficultyLabel.setStyle("-fx-text-fill: " + challenge.getDifficultyColor()
                + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        header.getChildren().addAll(titleLabel, difficultyLabel);

        Label descriptionLabel = new Label(createDescriptionPreview(challenge.getDescription()));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #c8d6e5; -fx-font-size: 12px;");

        String status = completedChallengeIds.contains(challenge.getId()) ? "Completed" : "Not Started";
        Label metadataLabel = new Label(challenge.getTestCases().size()
                + " tests - " + getDifficultyPoints(challenge.getDifficulty())
                + " - " + status);
        metadataLabel.setStyle("-fx-text-fill: #8b91a8; -fx-font-size: 12px;");

        body.getChildren().addAll(header, descriptionLabel, metadataLabel);
        card.getChildren().add(body);

        return card;
    }

    private HBox createEmptyCard() {
        HBox card = new HBox();
        card.getStyleClass().add("content-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinHeight(CARD_HEIGHT);
        card.setPrefHeight(CARD_HEIGHT);
        card.setMaxWidth(Double.MAX_VALUE);

        Label message = new Label("No challenges found. Try a different search or difficulty.");
        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #c8d6e5; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(message, spacer);

        return card;
    }

    private String getSelectedDifficulty() {
        if (allDifficultyButton == null || allDifficultyButton.getToggleGroup() == null) {
            return ALL_DIFFICULTIES;
        }

        Toggle selectedToggle = allDifficultyButton.getToggleGroup().getSelectedToggle();
        if (selectedToggle == null || selectedToggle.getUserData() == null) {
            return ALL_DIFFICULTIES;
        }

        return selectedToggle.getUserData().toString();
    }

    private String getDifficultyPoints(String difficulty) {
        return switch (difficulty) {
            case "EASY" -> "10 points";
            case "MEDIUM" -> "25 points";
            case "HARD" -> "50 points";
            default -> "0 points";
        };
    }

    private String createDescriptionPreview(String description) {
        String preview = description == null ? "" : description.replaceAll("\\s+", " ").trim();
        if (preview.length() <= DESCRIPTION_PREVIEW_LENGTH) {
            return preview;
        }

        return preview.substring(0, DESCRIPTION_PREVIEW_LENGTH - 3) + "...";
    }

    private String normalizeSearch(String searchText) {
        return searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);
    }

    private boolean containsIgnoreCase(String value, String searchText) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(searchText);
    }

    private void startChallenge(MouseEvent event, String challengeId) {
        try {
            CodingChallengeController.setCurrentChallengeId(challengeId);
            switchScreen(event, "/com/example/syntaxio/coding-challenge.fxml", 1200, 800);
        } catch (IOException e) {
            showError("Could not open challenge: " + e.getMessage());
        }
    }

    @FXML
    private void onBackToHome(ActionEvent event) throws IOException {
        switchScreen(event, "/com/example/syntaxio/main-menu.fxml", 1200, 1150);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Challenge Browser");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
