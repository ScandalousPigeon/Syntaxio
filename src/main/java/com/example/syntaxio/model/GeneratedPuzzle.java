package com.example.syntaxio.model;

import java.util.List;

public class GeneratedPuzzle {

    private final String title;
    private final String description;
    private final String difficulty;
    private final String starterCode;
    private final List<TestCase> testCases;
    private final String modelSolution;

    public GeneratedPuzzle(
            String title,
            String description,
            String difficulty,
            String starterCode,
            List<TestCase> testCases,
            String modelSolution
    ) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.starterCode = starterCode;
        this.testCases = testCases;
        this.modelSolution = modelSolution;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public String getModelSolution() {
        return modelSolution;
    }
}