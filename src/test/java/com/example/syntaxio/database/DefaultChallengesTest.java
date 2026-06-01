package com.example.syntaxio.database;

import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.TestCase;
import com.example.syntaxio.runner.CodeExecutor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultChallengesTest {

    @Test
    void defaultCatalogContainsTwentyUniqueChallenges() {
        List<Challenge> challenges = DefaultChallenges.all();
        Set<String> ids = new HashSet<>();

        assertEquals(20, challenges.size());

        for (Challenge challenge : challenges) {
            assertTrue(ids.add(challenge.getId()), "Duplicate challenge ID: " + challenge.getId());
            assertFalse(challenge.getTitle().isBlank(), "Blank title for " + challenge.getId());
            assertFalse(challenge.getDescription().isBlank(), "Blank description for " + challenge.getId());
            assertFalse(challenge.getStarterCode().isBlank(), "Blank starter code for " + challenge.getId());
            assertFalse(challenge.getModelSolution().isBlank(), "Blank model solution for " + challenge.getId());
            assertTrue(isValidDifficulty(challenge.getDifficulty()), "Invalid difficulty for " + challenge.getId());
            assertTrue(challenge.getTestCases().size() >= 3, "Too few test cases for " + challenge.getId());
        }
    }

    @Test
    void defaultChallengeModelSolutionsPassTheirTestCases() {
        List<String> failures = new ArrayList<>();

        for (Challenge challenge : DefaultChallenges.all()) {
            List<TestCase> testCases = copyTestCases(challenge.getTestCases());
            List<TestCase> results = CodeExecutor.executeTests(challenge.getModelSolution(), testCases);

            for (TestCase result : results) {
                if (!result.isPassed()) {
                    failures.add(challenge.getId() + " " + challenge.getTitle()
                            + " failed '" + result.getDescription()
                            + "' expected " + result.getExpectedOutput()
                            + " but got " + result.getActualOutput());
                }
            }
        }

        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    private static boolean isValidDifficulty(String difficulty) {
        return difficulty.equals("EASY") || difficulty.equals("MEDIUM") || difficulty.equals("HARD");
    }

    private static List<TestCase> copyTestCases(List<TestCase> testCases) {
        List<TestCase> copies = new ArrayList<>();
        for (TestCase testCase : testCases) {
            copies.add(new TestCase(
                    testCase.getDescription(),
                    testCase.getInput(),
                    testCase.getExpectedOutput()
            ));
        }
        return copies;
    }
}
