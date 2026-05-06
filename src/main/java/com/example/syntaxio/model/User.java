package com.example.syntaxio.model;

import java.time.LocalDateTime;

public class User {
    private int id;                   
    private String username;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private int totalHintsUsed;
    private int totalChallengesCompleted;  
    
    // Constructor for NEW user (without ID - database generates it)
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        this.totalHintsUsed = 0;
        this.totalChallengesCompleted = 0;
    }
    
    // Constructor for LOADING from database (with ID)
    public User(int id, String username, String passwordHash, 
                LocalDateTime createdAt, LocalDateTime lastLoginAt,
                int totalHintsUsed, int totalChallengesCompleted) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.totalHintsUsed = totalHintsUsed;
        this.totalChallengesCompleted = totalChallengesCompleted;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    public int getTotalHintsUsed() { return totalHintsUsed; }
    public void setTotalHintsUsed(int totalHintsUsed) { this.totalHintsUsed = totalHintsUsed; }
    
    public int getTotalChallengesCompleted() { return totalChallengesCompleted; }
    public void setTotalChallengesCompleted(int totalChallengesCompleted) { 
        this.totalChallengesCompleted = totalChallengesCompleted; 
    }
    
    // Helper methods
    public void incrementHintsUsed() {
        this.totalHintsUsed++;
    }
    
    public void incrementChallengesCompleted() {
        this.totalChallengesCompleted++;
    }  
}
