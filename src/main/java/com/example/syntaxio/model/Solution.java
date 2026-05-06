package com.example.syntaxio.model;

import java.time.LocalDateTime;

public class Solution {
    private String challengeId;
    private String code;
    private boolean passed;
    private int hintsUsed;
    private LocalDateTime submittedAt;

    public Solution(String challengeId, String code, boolean passed, int hintsUsed) {
        this.challengeId = challengeId;
        this.code = code;
        this.passed = passed;
        this.hintsUsed = hintsUsed;
        this.submittedAt = LocalDateTime.now();
    }

    public String getChallengeId() { return challengeId; }
    public String getCode() { return code; }
    public boolean isPassed() { return passed; }
    public int getHintsUsedForThisSolution() { return hintsUsed; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
