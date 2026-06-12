package com.example.syntaxio.model;

import java.time.LocalDateTime;

public class InProgressChallenge {
    private final int userId;
    private final String challengeId;
    private final String draftCode;
    private final LocalDateTime startedAt;
    private final LocalDateTime updatedAt;

    public InProgressChallenge(int userId, String challengeId, String draftCode,
                               LocalDateTime startedAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.challengeId = challengeId;
        this.draftCode = draftCode;
        this.startedAt = startedAt;
        this.updatedAt = updatedAt;
    }

    public int getUserId() {
        return userId;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public String getDraftCode() {
        return draftCode;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
