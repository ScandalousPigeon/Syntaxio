package com.example.syntaxio.ai.puzzlegeneration;

import com.example.syntaxio.database.SqliteChallengeDAO;
import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.GeneratedPuzzle;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class GeneratedPuzzleChallengeService {

    private static final String GENERATED_ID_PREFIX = "gen-";

    private final PuzzleGenerationService puzzleGenerationService;
    private final Function<Challenge, Boolean> challengeSaver;
    private final Supplier<String> idSupplier;

    public GeneratedPuzzleChallengeService(
            PuzzleGenerationService puzzleGenerationService,
            SqliteChallengeDAO challengeDAO
    ) {
        this(
                puzzleGenerationService,
                challengeDAO::addChallenge,
                () -> GENERATED_ID_PREFIX + UUID.randomUUID()
        );
    }

    GeneratedPuzzleChallengeService(
            PuzzleGenerationService puzzleGenerationService,
            Function<Challenge, Boolean> challengeSaver,
            Supplier<String> idSupplier
    ) {
        this.puzzleGenerationService = puzzleGenerationService;
        this.challengeSaver = challengeSaver;
        this.idSupplier = idSupplier;
    }

    public Challenge generateAndSaveChallenge(String topic, String difficulty) {
        String normalizedTopic = normalizeTopic(topic);
        String normalizedDifficulty = normalizeDifficulty(difficulty);

        GeneratedPuzzle generatedPuzzle = puzzleGenerationService.generatePuzzle(normalizedTopic, normalizedDifficulty);
        Challenge challenge = toChallenge(generatedPuzzle, idSupplier.get());

        if (!challengeSaver.apply(challenge)) {
            throw new IllegalStateException("Generated puzzle could not be saved.");
        }

        return challenge;
    }

    private static Challenge toChallenge(GeneratedPuzzle puzzle, String id) {
        return new Challenge(
                id,
                puzzle.getTitle(),
                puzzle.getDescription(),
                puzzle.getStarterCode(),
                puzzle.getDifficulty(),
                puzzle.getTestCases(),
                puzzle.getModelSolution()
        );
    }

    private static String normalizeTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Topic is required.");
        }

        return topic.trim();
    }

    private static String normalizeDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            throw new IllegalArgumentException("Difficulty is required.");
        }

        String normalizedDifficulty = difficulty.trim().toUpperCase(Locale.ROOT);
        if (!normalizedDifficulty.equals("EASY")
                && !normalizedDifficulty.equals("MEDIUM")
                && !normalizedDifficulty.equals("HARD")) {
            throw new IllegalArgumentException("Difficulty must be EASY, MEDIUM, or HARD.");
        }

        return normalizedDifficulty;
    }
}
