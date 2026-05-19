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

        return new GeneratedPuzzle(response);
    }
}