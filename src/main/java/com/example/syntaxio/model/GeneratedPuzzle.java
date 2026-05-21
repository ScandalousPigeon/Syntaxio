package com.example.syntaxio.model;

public class GeneratedPuzzle {

    private String title;
    private String description;
    private String starterCode;
    private String testCases;
    private String expectedSolutionExplanation;

    public GeneratedPuzzle() {
    }

    public GeneratedPuzzle(String title,
                           String description,
                           String starterCode,
                           String testCases,
                           String expectedSolutionExplanation) {
        this.title = title;
        this.description = description;
        this.starterCode = starterCode;
        this.testCases = testCases;
        this.expectedSolutionExplanation = expectedSolutionExplanation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }

    public String getTestCases() {
        return testCases;
    }

    public void setTestCases(String testCases) {
        this.testCases = testCases;
    }

    public String getExpectedSolutionExplanation() {
        return expectedSolutionExplanation;
    }

    public void setExpectedSolutionExplanation(String expectedSolutionExplanation) {
        this.expectedSolutionExplanation = expectedSolutionExplanation;
    }
}