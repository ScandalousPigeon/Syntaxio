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
    void generatePuzzlePromptContainsRequiredOutputContract() {
        FakeLLMClient fakeLLM = new FakeLLMClient(validLLMResponse());
        PuzzleGenerationService service = new PuzzleGenerationService(fakeLLM);

        service.generatePuzzle("strings", "MEDIUM");

        String prompt = fakeLLM.getLastPrompt();

        assertAll(
                () -> assertTrue(prompt.contains("Return exactly the following format")),
                () -> assertTrue(prompt.contains("TITLE:")),
                () -> assertTrue(prompt.contains("DESCRIPTION:")),
                () -> assertTrue(prompt.contains("DIFFICULTY:")),
                () -> assertTrue(prompt.contains("STARTER_CODE:")),
                () -> assertTrue(prompt.contains("TEST_1_DESCRIPTION:")),
                () -> assertTrue(prompt.contains("TEST_1_INPUT:")),
                () -> assertTrue(prompt.contains("TEST_1_EXPECTED:")),
                () -> assertTrue(prompt.contains("TEST_2_DESCRIPTION:")),
                () -> assertTrue(prompt.contains("TEST_2_INPUT:")),
                () -> assertTrue(prompt.contains("TEST_2_EXPECTED:")),
                () -> assertTrue(prompt.contains("TEST_3_DESCRIPTION:")),
                () -> assertTrue(prompt.contains("TEST_3_INPUT:")),
                () -> assertTrue(prompt.contains("TEST_3_EXPECTED:")),
                () -> assertTrue(prompt.contains("MODEL_SOLUTION:")),
                () -> assertTrue(prompt.contains("<EASY, MEDIUM, or HARD>")),
                () -> assertTrue(prompt.contains("<Java method only, not a full class>")),
                () -> assertTrue(prompt.contains("The puzzle should be suitable for beginner programmers")),
                () -> assertTrue(prompt.contains("Make sure the model solution is correct"))
        );
    }

    @Test
    void generatePuzzleParsesStructuredLLMResponseIntoGeneratedPuzzle() {
        FakeLLMClient fakeLLM = new FakeLLMClient(validLLMResponse());
        PuzzleGenerationService service = new PuzzleGenerationService(fakeLLM);

        GeneratedPuzzle puzzle = service.generatePuzzle("arrays", "EASY");

        assertEquals("Count Positive Numbers", puzzle.getTitle());
        assertEquals(
                "Write a method that counts how many positive numbers are in an integer array.",
                puzzle.getDescription()
        );
        assertEquals("EASY", puzzle.getDifficulty());
        assertTrue(puzzle.getStarterCode().contains("countPositive"));
        assertTrue(puzzle.getStarterCode().contains("return 0;"));
        assertTrue(puzzle.getModelSolution().contains("for"));
        assertTrue(puzzle.getModelSolution().contains("return count;"));

        assertEquals(3, puzzle.getTestCases().size());

        assertTestCase(
                puzzle.getTestCases().get(0),
                "Mixed positive and negative numbers",
                "new int[]{1, -2, 3}",
                "2"
        );
        assertTestCase(
                puzzle.getTestCases().get(1),
                "No positive numbers",
                "new int[]{-1, -2, 0}",
                "0"
        );
        assertTestCase(
                puzzle.getTestCases().get(2),
                "All positive numbers",
                "new int[]{4, 5, 6}",
                "3"
        );
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

    @Test
    void generatePuzzleThrowsExceptionWhenRequiredSectionIsMissing() {
        String response = replaceRequired(
                validLLMResponse(),
                "TEST_2_EXPECTED:",
                "TEST_2_EXPECTED_MISSING:"
        );
        FakeLLMClient fakeLLM = new FakeLLMClient(response);
        PuzzleGenerationService service = new PuzzleGenerationService(fakeLLM);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.generatePuzzle("arrays", "EASY")
        );

        assertTrue(exception.getMessage().contains("Missing required section: TEST_2_EXPECTED:"));
    }

    @Test
    void generatePuzzleThrowsExceptionWhenRequiredSectionIsEmpty() {
        String response = replaceRequired(
                validLLMResponse(),
                "TITLE:\nCount Positive Numbers",
                "TITLE:\n"
        );
        FakeLLMClient fakeLLM = new FakeLLMClient(response);
        PuzzleGenerationService service = new PuzzleGenerationService(fakeLLM);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.generatePuzzle("arrays", "EASY")
        );

        assertTrue(exception.getMessage().contains("Section cannot be empty: TITLE:"));
    }

    @Test
    void generatePuzzleTrimsWhitespaceAroundSectionContent() {
        String response = replaceRequired(
                validLLMResponse(),
                "TITLE:\nCount Positive Numbers",
                "TITLE:\n\n   Count Positive Numbers   \n"
        );
        FakeLLMClient fakeLLM = new FakeLLMClient(response);
        PuzzleGenerationService service = new PuzzleGenerationService(fakeLLM);

        GeneratedPuzzle puzzle = service.generatePuzzle("arrays", "EASY");

        assertEquals("Count Positive Numbers", puzzle.getTitle());
    }

    @Test
    void generatePuzzleThrowsExceptionWhenLLMResponseIsNull() {
        FakeLLMClient fakeLLM = new FakeLLMClient(null);
        PuzzleGenerationService service = new PuzzleGenerationService(fakeLLM);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.generatePuzzle("arrays", "EASY")
        );

        assertEquals("Could not parse generated puzzle response.", exception.getMessage());
    }

    @Test
    void generatePuzzleDoesNotSwallowLLMClientFailures() {
        RuntimeException failure = new RuntimeException("LLM unavailable");
        PuzzleGenerationService service = new PuzzleGenerationService(prompt -> {
            throw failure;
        });

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.generatePuzzle("arrays", "EASY")
        );

        assertSame(failure, exception);
    }

    private static void assertTestCase(
            TestCase testCase,
            String expectedDescription,
            String expectedInput,
            String expectedOutput
    ) {
        assertAll(
                () -> assertEquals(expectedDescription, testCase.getDescription()),
                () -> assertEquals(expectedInput, testCase.getInput()),
                () -> assertEquals(expectedOutput, testCase.getExpectedOutput())
        );
    }

    private static String replaceRequired(String source, String target, String replacement) {
        String updated = source.replace(target, replacement);
        assertNotEquals(source, updated, "Test fixture replacement did not match source text.");
        return updated;
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
