package com.example.syntaxio.ai.chat;

import com.example.syntaxio.ai.client.LLMClient;

public class MainMenuAssistant {

    private final LLMClient llmClient;

    public MainMenuAssistant(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    public String reply(String userMessage) {
        String prompt = """
                You are the friendly AI assistant for Syntaxio, a beginner coding practice app.

                Help users understand programming, algorithms, and data structures.
                Keep explanations beginner-friendly.
                Do not generate full assignment solutions.

                User message:
                %s
                """.formatted(userMessage);

        return llmClient.generate(prompt);
    }
}