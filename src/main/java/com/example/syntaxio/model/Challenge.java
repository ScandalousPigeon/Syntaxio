package com.example.syntaxio.model;

import java.util.List;

public class Challenge {
    private String id;
    private String title;
    private String description;
    private String starterCode;
    private String difficulty;  // "EASY", "MEDIUM", "HARD"
    private List<TestCase> testCases;
    private String modelSolution;  // For comparison after completion
    
    public Challenge(String id, String title, String description, 
                     String starterCode, String difficulty, 
                     List<TestCase> testCases, String modelSolution) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.starterCode = starterCode;
        this.difficulty = difficulty;
        this.testCases = testCases;
        this.modelSolution = modelSolution;
    }
    
    // No-args constructor for JSON
    public Challenge() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public List<TestCase> getTestCases() { return testCases; }
    public void setTestCases(List<TestCase> testCases) { this.testCases = testCases; }
    
    public String getModelSolution() { return modelSolution; }
    public void setModelSolution(String modelSolution) { this.modelSolution = modelSolution; }
    
    // Helper method to get difficulty color for UI
    public String getDifficultyColor() {
        switch (difficulty) {
            case "EASY": return "#4ecdc4";  // Teal
            case "MEDIUM": return "#f9ca24"; // Yellow
            case "HARD": return "#ff6b6b";   // Red
            default: return "#cccccc";
        }
    }    
}
