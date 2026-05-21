package com.example.syntaxio.ai.puzzlegeneration;

import com.example.syntaxio.ai.client.LLMClient;
import com.example.syntaxio.model.GeneratedPuzzle;

public class PuzzleGenerationService {

    private final LLMClient llmClient;

    public PuzzleGenerationService(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    public GeneratedPuzzle generatePuzzle(String topic, String difficulty) {
        String prompt = """
                Generate a coding puzzle for beginners.

                Return the result in this format:
                TITLE:
                DESCRIPTION:
                STARTER_CODE:
                TEST_CASES:
                EXPECTED_SOLUTION_EXPLANATION:

                Topic: %s
                Difficulty: %s

                The puzzle should be original, clear, and suitable for beginners.
                """.formatted(topic, difficulty);

        String response = llmClient.generate(prompt);

        return parseGeneratedPuzzle(response);
    }

    private GeneratedPuzzle parseGeneratedPuzzle(String response) {
        GeneratedPuzzle puzzle = new GeneratedPuzzle();

        puzzle.setTitle(extractSection(response, "TITLE:", "DESCRIPTION:"));
        puzzle.setDescription(extractSection(response, "DESCRIPTION:", "STARTER_CODE:"));
        puzzle.setStarterCode(extractSection(response, "STARTER_CODE:", "TEST_CASES:"));
        puzzle.setTestCases(extractSection(response, "TEST_CASES:", "EXPECTED_SOLUTION_EXPLANATION:"));
        puzzle.setExpectedSolutionExplanation(
                extractSection(response, "EXPECTED_SOLUTION_EXPLANATION:", null)
        );

        return puzzle;
    }

    private String extractSection(String response, String startMarker, String endMarker) {
        int startIndex = response.indexOf(startMarker);

        if (startIndex == -1) {
            return "";
        }

        startIndex += startMarker.length();

        int endIndex;

        if (endMarker == null) {
            endIndex = response.length();
        } else {
            endIndex = response.indexOf(endMarker);

            if (endIndex == -1) {
                endIndex = response.length();
            }
        }

        return response.substring(startIndex, endIndex).trim();
    }
}