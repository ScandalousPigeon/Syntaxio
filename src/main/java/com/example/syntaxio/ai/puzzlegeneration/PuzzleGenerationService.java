package com.example.syntaxio.ai.puzzlegeneration;

import com.example.syntaxio.ai.client.LLMClient;
import com.example.syntaxio.database.SqliteChallengeDAO;
import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.GeneratedPuzzle;
import com.example.syntaxio.model.TestCase;

import java.util.List;
import java.util.UUID;

public class PuzzleGenerationService {

    private final LLMClient llmClient;
    private final SqliteChallengeDAO challengeDAO;

    public PuzzleGenerationService(LLMClient llmClient, SqliteChallengeDAO challengeDAO) {
        this.llmClient = llmClient;
        this.challengeDAO = challengeDAO;
    }

    public Challenge generateAndSavePuzzle(String topic, String difficulty) {
        String response = llmClient.generate(buildPrompt(topic, difficulty));

        Challenge challenge = parseChallenge(response, difficulty);

        boolean saved = challengeDAO.addChallenge(challenge);

        if (!saved) {
            throw new RuntimeException("Generated puzzle could not be saved to the database.");
        }

        return challenge;
    }

    private String buildPrompt(String topic, String difficulty) {
        return """
                You are generating a beginner Java coding challenge for Syntaxio.

                Return EXACTLY this format.
                Do not use markdown code fences.
                Do not add extra commentary.

                TITLE:
                <short puzzle title>

                DESCRIPTION:
                <clear beginner-friendly description including method signature>

                STARTER_CODE:
                <Java method only, not a full class>

                TEST_1_DESCRIPTION:
                <description>

                TEST_1_INPUT:
                <Java expression for the input>

                TEST_1_EXPECTED:
                <expected output>

                TEST_2_DESCRIPTION:
                <description>

                TEST_2_INPUT:
                <Java expression for the input>

                TEST_2_EXPECTED:
                <expected output>

                TEST_3_DESCRIPTION:
                <description>

                TEST_3_INPUT:
                <Java expression for the input>

                TEST_3_EXPECTED:
                <expected output>

                MODEL_SOLUTION:
                <Java method only, not a full class>

                Requirements:
                - Topic: %s
                - Difficulty: %s
                - Suitable for beginner programmers
                - Use simple Java
                - The starter code should contain TODO-style placeholder logic
                - The model solution should be correct
                """.formatted(topic, difficulty);
    }

    private Challenge parseChallenge(String response, String difficulty) {
        String id = "ai-" + UUID.randomUUID();

        String title = section(response, "TITLE:", "DESCRIPTION:");
        String description = section(response, "DESCRIPTION:", "STARTER_CODE:");
        String starterCode = section(response, "STARTER_CODE:", "TEST_1_DESCRIPTION:");

        String test1Description = section(response, "TEST_1_DESCRIPTION:", "TEST_1_INPUT:");
        String test1Input = section(response, "TEST_1_INPUT:", "TEST_1_EXPECTED:");
        String test1Expected = section(response, "TEST_1_EXPECTED:", "TEST_2_DESCRIPTION:");

        String test2Description = section(response, "TEST_2_DESCRIPTION:", "TEST_2_INPUT:");
        String test2Input = section(response, "TEST_2_INPUT:", "TEST_2_EXPECTED:");
        String test2Expected = section(response, "TEST_2_EXPECTED:", "TEST_3_DESCRIPTION:");

        String test3Description = section(response, "TEST_3_DESCRIPTION:", "TEST_3_INPUT:");
        String test3Input = section(response, "TEST_3_INPUT:", "TEST_3_EXPECTED:");
        String test3Expected = section(response, "TEST_3_EXPECTED:", "MODEL_SOLUTION:");

        String modelSolution = section(response, "MODEL_SOLUTION:", null);

        List<TestCase> testCases = List.of(
                new TestCase(test1Description, test1Input, test1Expected),
                new TestCase(test2Description, test2Input, test2Expected),
                new TestCase(test3Description, test3Input, test3Expected)
        );

        return new Challenge(
                id,
                title,
                description,
                starterCode,
                difficulty,
                testCases,
                modelSolution
        );
    }

    private String section(String text, String startLabel, String endLabel) {
        int start = text.indexOf(startLabel);

        if (start == -1) {
            throw new RuntimeException("Missing section: " + startLabel + "\n\nResponse was:\n" + text);
        }

        start += startLabel.length();

        int end = endLabel == null ? text.length() : text.indexOf(endLabel, start);

        if (end == -1) {
            throw new RuntimeException("Missing section: " + endLabel + "\n\nResponse was:\n" + text);
        }

        return text.substring(start, end).trim();
    }

    public GeneratedPuzzle generatePuzzle(String arrays, String easy) {

    }
}
