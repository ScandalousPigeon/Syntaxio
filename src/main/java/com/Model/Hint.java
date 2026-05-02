package com.Model;

import java.time.LocalDateTime;

public class Hint {
    private String id;
    private String challengeId;
    private String hintText;
    private String hintType;  // "GENERAL", "PSEUDOCODE", "DOCUMENTATION"
    private int confidence;   // 0-100
    private boolean wasHelpful;
    private LocalDateTime requestedAt;
    
    public Hint(String challengeId, String hintText, String hintType, int confidence) {
        this.id = java.util.UUID.randomUUID().toString();
        this.challengeId = challengeId;
        this.hintText = hintText;
        this.hintType = hintType;
        this.confidence = confidence;
        this.wasHelpful = false;
        this.requestedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getChallengeId() { return challengeId; }
    public void setChallengeId(String challengeId) { this.challengeId = challengeId; }
    
    public String getHintText() { return hintText; }
    public void setHintText(String hintText) { this.hintText = hintText; }
    
    public String getHintType() { return hintType; }
    public void setHintType(String hintType) { this.hintType = hintType; }
    
    public int getConfidence() { return confidence; }
    public void setConfidence(int confidence) { this.confidence = confidence; }
    
    public boolean isWasHelpful() { return wasHelpful; }
    public void setWasHelpful(boolean wasHelpful) { this.wasHelpful = wasHelpful; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    // Helper methods
    public String getConfidenceColor() {
        if (confidence >= 70) return "#2ecc71";
        if (confidence >= 40) return "#f39c12";
        return "#e74c3c";
    }
    
    public String getConfidenceText() {
        if (confidence >= 70) return "High confidence";
        if (confidence >= 40) return "Medium confidence";
        return "Low confidence - verify carefully";
    }    
}
