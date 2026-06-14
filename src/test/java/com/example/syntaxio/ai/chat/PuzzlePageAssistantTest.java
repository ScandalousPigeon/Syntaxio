package com.example.syntaxio.ai.chat;

import com.example.syntaxio.ai.client.LLMClient;
import com.example.syntaxio.model.Challenge;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PuzzlePageAssistantTest {

    @Test
    void replyReturnsLLMResponse() {
        FakeLLMClient fakeLLM = new FakeLLMClient("Try checking the loop condition.");
        PuzzlePageAssistant assistant = new PuzzlePageAssistant(fakeLLM);

        String response = assistant.reply("Why is my answer wrong?", challenge(), "return 0;");

        assertEquals("Try checking the loop condition.", response);
    }

    @Test
    void replyPromptIncludesChallengeContextAndCurrentCode() {
        FakeLLMClient fakeLLM = new FakeLLMClient("AI response");
        PuzzlePageAssistant assistant = new PuzzlePageAssistant(fakeLLM);

        assistant.reply("Give me a hint", challenge(), "return numbers.length;");

        assertTrue(fakeLLM.lastPrompt.contains("Sum of Array"));
        assertTrue(fakeLLM.lastPrompt.contains("Return the sum of all numbers."));
        assertTrue(fakeLLM.lastPrompt.contains("return numbers.length;"));
        assertTrue(fakeLLM.lastPrompt.contains("Give me a hint"));
    }

    @Test
    void replyPromptAvoidsFullFinalSolutionsByDefault() {
        FakeLLMClient fakeLLM = new FakeLLMClient("AI response");
        PuzzlePageAssistant assistant = new PuzzlePageAssistant(fakeLLM);

        assistant.reply("Solve this for me", challenge(), "return 0;");

        assertTrue(fakeLLM.lastPrompt.contains("Do not provide a full final solution"));
    }

    private Challenge challenge() {
        return new Challenge(
                "ch-001",
                "Sum of Array",
                "Return the sum of all numbers.",
                "return 0;",
                "EASY",
                List.of(),
                "return sum;"
        );
    }

    private static class FakeLLMClient implements LLMClient {
        private final String response;
        private String lastPrompt;

        FakeLLMClient(String response) {
            this.response = response;
        }

        @Override
        public String generate(String prompt) {
            lastPrompt = prompt;
            return response;
        }
    }
}
