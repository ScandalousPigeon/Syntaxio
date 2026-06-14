package com.example.syntaxio.ai.chat;

import com.example.syntaxio.ai.client.LLMClient;
import com.example.syntaxio.model.Challenge;

public class PuzzlePageAssistant {
    private final LLMClient llmClient;

    public PuzzlePageAssistant(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    public String reply(String userMessage, Challenge challenge, String currentCode) {
        String challengeTitle = challenge == null ? "Unknown challenge" : challenge.getTitle();
        String challengeDescription = challenge == null ? "" : challenge.getDescription();
        String starterOrDraftCode = currentCode == null ? "" : currentCode;

        String prompt = """
                You are the AI assistant inside Syntaxio's coding challenge page.

                Help beginner programmers reason through the current puzzle.
                Give hints, explain concepts, point out likely mistakes, and suggest next steps.
                Do not provide a full final solution unless the user has already solved it and asks for an explanation.

                Current challenge:
                %s

                Challenge description:
                %s

                User's current code:
                %s

                User message:
                %s
                """.formatted(challengeTitle, challengeDescription, starterOrDraftCode, userMessage);

        return llmClient.generate(prompt);
    }
}
