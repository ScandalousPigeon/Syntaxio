package com.example.syntaxio.runner;

import com.example.syntaxio.model.TestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeExecutorTest {

    @Test
    void executeTestsCallsUserMethodWithEachStoredInput() {
        String userCode = """
                public int sumArray(int[] numbers) {
                    int sum = 0;
                    for (int number : numbers) {
                        sum += number;
                    }
                    return sum;
                }
                """;
        List<TestCase> testCases = List.of(
                new TestCase("positive numbers", "new int[]{1, 2, 3}", "6"),
                new TestCase("negative numbers", "new int[]{-1, -2, -3}", "-6"),
                new TestCase("empty array", "new int[]{}", "0")
        );

        List<TestCase> results = CodeExecutor.executeTests(userCode, testCases);

        assertAll(
                () -> assertTrue(results.get(0).isPassed()),
                () -> assertEquals("6", results.get(0).getActualOutput()),
                () -> assertTrue(results.get(1).isPassed()),
                () -> assertEquals("-6", results.get(1).getActualOutput()),
                () -> assertTrue(results.get(2).isPassed()),
                () -> assertEquals("0", results.get(2).getActualOutput())
        );
    }

    @Test
    void executeTestsSupportsStringResults() {
        String userCode = """
                public String reverseString(String input) {
                    return new StringBuilder(input).reverse().toString();
                }
                """;
        List<TestCase> testCases = List.of(
                new TestCase("normal string", "\"hello\"", "olleh"),
                new TestCase("empty string", "\"\"", "")
        );

        List<TestCase> results = CodeExecutor.executeTests(userCode, testCases);

        assertAll(
                () -> assertTrue(results.get(0).isPassed()),
                () -> assertEquals("olleh", results.get(0).getActualOutput()),
                () -> assertTrue(results.get(1).isPassed()),
                () -> assertEquals("", results.get(1).getActualOutput())
        );
    }

    @Test
    void executeTestsFormatsArrayResults() {
        String userCode = """
                public int[] doubleNumbers(int[] numbers) {
                    int[] doubled = new int[numbers.length];
                    for (int i = 0; i < numbers.length; i++) {
                        doubled[i] = numbers[i] * 2;
                    }
                    return doubled;
                }
                """;
        List<TestCase> testCases = List.of(
                new TestCase("doubles numbers", "new int[]{1, 2, 3}", "[2, 4, 6]")
        );

        List<TestCase> results = CodeExecutor.executeTests(userCode, testCases);

        assertTrue(results.get(0).isPassed());
        assertEquals("[2, 4, 6]", results.get(0).getActualOutput());
    }
}
