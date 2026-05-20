package com.example.syntaxio.ai.puzzlegeneration;

import com.example.syntaxio.ai.client.LLMClient;
import com.example.syntaxio.model.GeneratedPuzzle;
import com.example.syntaxio.model.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PuzzleGenerationServiceTest {

    @Test
    void generatePuzzleSendsTopicAndDifficultyToLLM() {
        FakeLLMClient fakeLLM = new FakeLLMClient(validLLMResponse());
        PuzzleGenerationService service = new PuzzleGenerationService(fakeLLM);

        service.generatePuzzle("arrays", "EASY");

        assertEquals(1, fakeLLM.getCallCount());
        assertTrue(fakeLLM.getLastPrompt().contains("arrays"));
        assertTrue(fakeLLM.getLastPrompt().contains("EASY"));
    }

    @Test
    void generatePuzzleParsesStructuredLLMResponseIntoGeneratedPuzzle() {
        FakeLLMClient fakeLLM = new FakeLLMClient(validLLMResponse());
        PuzzleGenerationService service = new PuzzleGenerationService(fakeLLM);

        GeneratedPuzzle puzzle = service.generatePuzzle("arrays", "EASY");

        assertEquals("Count Positive Numbers", puzzle.getTitle());
        assertEquals("EASY", puzzle.getDifficulty());
        assertTrue(puzzle.getDescription().contains("positive numbers"));
        assertTrue(puzzle.getStarterCode().contains("countPositive"));
        assertTrue(puzzle.getModelSolution().contains("for"));

        assertEquals(3, puzzle.getTestCases().size());

        TestCase firstTest = puzzle.getTestCases().get(0);
        assertEquals("Mixed positive and negative numbers", firstTest.getDescription());
        assertEquals("new int[]{1, -2, 3}", firstTest.getInput());
        assertEquals("2", firstTest.getExpectedOutput());
    }

    @Test
    void generatePuzzleThrowsExceptionWhenLLMResponseIsMalformed() {
        FakeLLMClient fakeLLM = new FakeLLMClient("This is not a valid puzzle response.");
        PuzzleGenerationService service = new PuzzleGenerationService(fakeLLM);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generatePuzzle("arrays", "EASY")
        );
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
                    // TODO: count positive numbers
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

    private static class FakeLLMClient implements LLMClient {
        // make a fake AI since we haven't implemented AI querying yet
        private final String response;
        private String lastPrompt;
        private int callCount;

        FakeLLMClient(String response) {
            this.response = response;
        }

        @Override
        public String generate(String prompt) {
            this.lastPrompt = prompt;
            this.callCount++;
            return response;
        }

        String getLastPrompt() {
            return lastPrompt;
        }

        int getCallCount() {
            return callCount;
        }
    }
}