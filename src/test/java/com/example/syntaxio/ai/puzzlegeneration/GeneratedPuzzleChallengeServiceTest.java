package com.example.syntaxio.ai.puzzlegeneration;

import com.example.syntaxio.ai.client.LLMClient;
import com.example.syntaxio.model.Challenge;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneratedPuzzleChallengeServiceTest {

    @Test
    void generateAndSaveChallengePersistsGeneratedPuzzleAsChallenge() {
        CapturingLLMClient llmClient = new CapturingLLMClient(validLLMResponse());
        List<Challenge> savedChallenges = new ArrayList<>();
        GeneratedPuzzleChallengeService service = new GeneratedPuzzleChallengeService(
                new PuzzleGenerationService(llmClient),
                challenge -> {
                    savedChallenges.add(challenge);
                    return true;
                },
                () -> "gen-test"
        );

        Challenge challenge = service.generateAndSaveChallenge(" arrays ", "easy");

        assertSame(challenge, savedChallenges.get(0));
        assertEquals("gen-test", challenge.getId());
        assertEquals("Count Positive Numbers", challenge.getTitle());
        assertEquals("EASY", challenge.getDifficulty());
        assertEquals(3, challenge.getTestCases().size());
        assertTrue(challenge.getStarterCode().contains("countPositive"));
        assertTrue(challenge.getModelSolution().contains("return count;"));
        assertTrue(llmClient.getLastPrompt().contains("Topic: arrays"));
        assertTrue(llmClient.getLastPrompt().contains("Difficulty: EASY"));
    }

    @Test
    void generateAndSaveChallengeThrowsWhenSaveFails() {
        GeneratedPuzzleChallengeService service = new GeneratedPuzzleChallengeService(
                new PuzzleGenerationService(prompt -> validLLMResponse()),
                challenge -> false,
                () -> "gen-test"
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.generateAndSaveChallenge("arrays", "EASY")
        );

        assertEquals("Generated puzzle could not be saved.", exception.getMessage());
    }

    @Test
    void generateAndSaveChallengeRejectsBlankTopic() {
        GeneratedPuzzleChallengeService service = new GeneratedPuzzleChallengeService(
                new PuzzleGenerationService(prompt -> validLLMResponse()),
                challenge -> true,
                () -> "gen-test"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.generateAndSaveChallenge(" ", "EASY")
        );

        assertEquals("Topic is required.", exception.getMessage());
    }

    private static String validLLMResponse() {
        return """
                TITLE:
                Count Positive Numbers

                DESCRIPTION:
                Write a method that counts how many positive numbers are in an integer array.

                DIFFICULTY:
                EASY

                STARTER_CODE:
                public int countPositive(int[] numbers) {
                    return 0;
                }

                TEST_1_DESCRIPTION:
                Mixed positive and negative numbers

                TEST_1_INPUT:
                new int[]{1, -2, 3}

                TEST_1_EXPECTED:
                2

                TEST_2_DESCRIPTION:
                No positive numbers

                TEST_2_INPUT:
                new int[]{-1, -2, 0}

                TEST_2_EXPECTED:
                0

                TEST_3_DESCRIPTION:
                All positive numbers

                TEST_3_INPUT:
                new int[]{4, 5, 6}

                TEST_3_EXPECTED:
                3

                MODEL_SOLUTION:
                public int countPositive(int[] numbers) {
                    int count = 0;
                    for (int number : numbers) {
                        if (number > 0) {
                            count++;
                        }
                    }
                    return count;
                }
                """;
    }

    private static class CapturingLLMClient implements LLMClient {
        private final String response;
        private String lastPrompt;

        private CapturingLLMClient(String response) {
            this.response = response;
        }

        @Override
        public String generate(String prompt) {
            this.lastPrompt = prompt;
            return response;
        }

        private String getLastPrompt() {
            return lastPrompt;
        }
    }
}
