package com.Model;

public class TestCase {
    private String description;
    private String input;
    private String expectedOutput;
    private boolean passed;
    private String actualOutput;

    public TestCase(String description, String input, String expectedOutput) {
        this.description = description;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.passed = false;
        this.actualOutput = "";
    }
    
    // No-args constructor for JSON
    public TestCase() {}
    
    // Getters and Setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    
    public String getActualOutput() { return actualOutput; }
    public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }    
}
