package com.example.syntaxio.ai.puzzlegeneration;

import com.example.syntaxio.ai.client.LLMClient;
import com.example.syntaxio.model.GeneratedPuzzle;
import com.example.syntaxio.model.TestCase;

import java.util.List;

public class PuzzleGenerationService {

    private final LLMClient llmClient;

    public PuzzleGenerationService(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    public GeneratedPuzzle generatePuzzle(String topic, String difficulty) {
        String prompt = buildPrompt(topic, difficulty);
        String response = llmClient.generate(prompt);

        return parseGeneratedPuzzle(response);
    }

    private String buildPrompt(String topic, String difficulty) {
        return """
                You are generating a beginner Java coding challenge for Syntaxio.

                Return exactly the following format:

                TITLE:
                <short puzzle title>

                DESCRIPTION:
                <clear beginner-friendly puzzle description>

                DIFFICULTY:
                <EASY, MEDIUM, or HARD>

                STARTER_CODE:
                <Java method only, not a full class>

                TEST_1_DESCRIPTION:
                <description of test case 1>

                TEST_1_INPUT:
                <Java expression for the input>

                TEST_1_EXPECTED:
                <expected output>

                TEST_2_DESCRIPTION:
                <description of test case 2>

                TEST_2_INPUT:
                <Java expression for the input>

                TEST_2_EXPECTED:
                <expected output>

                TEST_3_DESCRIPTION:
                <description of test case 3>

                TEST_3_INPUT:
                <Java expression for the input>

                TEST_3_EXPECTED:
                <expected output>

                MODEL_SOLUTION:
                <Java method only, not a full class>

                Requirements:
                - Topic: %s
                - Difficulty: %s
                - The puzzle should be suitable for beginner programmers.
                - Make sure the model solution is correct.
                """.formatted(topic, difficulty);
    }

    private GeneratedPuzzle parseGeneratedPuzzle(String response) {
        try {
            String title = getSection(response, "TITLE:", "DESCRIPTION:");
            String description = getSection(response, "DESCRIPTION:", "DIFFICULTY:");
            String difficulty = getSection(response, "DIFFICULTY:", "STARTER_CODE:");
            String starterCode = getSection(response, "STARTER_CODE:", "TEST_1_DESCRIPTION:");

            String test1Description = getSection(response, "TEST_1_DESCRIPTION:", "TEST_1_INPUT:");
            String test1Input = getSection(response, "TEST_1_INPUT:", "TEST_1_EXPECTED:");
            String test1Expected = getSection(response, "TEST_1_EXPECTED:", "TEST_2_DESCRIPTION:");

            String test2Description = getSection(response, "TEST_2_DESCRIPTION:", "TEST_2_INPUT:");
            String test2Input = getSection(response, "TEST_2_INPUT:", "TEST_2_EXPECTED:");
            String test2Expected = getSection(response, "TEST_2_EXPECTED:", "TEST_3_DESCRIPTION:");

            String test3Description = getSection(response, "TEST_3_DESCRIPTION:", "TEST_3_INPUT:");
            String test3Input = getSection(response, "TEST_3_INPUT:", "TEST_3_EXPECTED:");
            String test3Expected = getSection(response, "TEST_3_EXPECTED:", "MODEL_SOLUTION:");

            String modelSolution = getSection(response, "MODEL_SOLUTION:", null);

            List<TestCase> testCases = List.of(
                    new TestCase(test1Description, test1Input, test1Expected),
                    new TestCase(test2Description, test2Input, test2Expected),
                    new TestCase(test3Description, test3Input, test3Expected)
            );

            return new GeneratedPuzzle(
                    title,
                    description,
                    difficulty,
                    starterCode,
                    testCases,
                    modelSolution
            );

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse generated puzzle response.", e);
        }
    }

    private String getSection(String text, String startLabel, String endLabel) {
        int startIndex = text.indexOf(startLabel);

        if (startIndex == -1) {
            throw new IllegalArgumentException("Missing required section: " + startLabel);
        }

        startIndex += startLabel.length();

        int endIndex;

        if (endLabel == null) {
            endIndex = text.length();
        } else {
            endIndex = text.indexOf(endLabel, startIndex);

            if (endIndex == -1) {
                throw new IllegalArgumentException("Missing required section: " + endLabel);
            }
        }

        String section = text.substring(startIndex, endIndex).trim();

        if (section.isEmpty()) {
            throw new IllegalArgumentException("Section cannot be empty: " + startLabel);
        }

        return section;
    }
}