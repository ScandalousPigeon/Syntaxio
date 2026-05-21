package com.example.syntaxio.ai.chat;

import com.example.syntaxio.ai.client.LLMClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainMenuAssistantTest {

    @Test
    void replySendsUserMessageToLLM() {
        FakeLLMClient fakeLLM = new FakeLLMClient("Hello! How can I help?");
        MainMenuAssistant assistant = new MainMenuAssistant(fakeLLM);

        assistant.reply("What is a loop?");

        assertEquals(1, fakeLLM.getCallCount());
        assertTrue(fakeLLM.getLastPrompt().contains("What is a loop?"));
    }

    @Test
    void replyReturnsLLMResponse() {
        FakeLLMClient fakeLLM = new FakeLLMClient("A loop repeats code.");
        MainMenuAssistant assistant = new MainMenuAssistant(fakeLLM);

        String response = assistant.reply("Explain loops");

        assertEquals("A loop repeats code.", response);
    }

    @Test
    void replyUsesBeginnerFriendlySystemPrompt() {
        FakeLLMClient fakeLLM = new FakeLLMClient("Hello!");
        MainMenuAssistant assistant = new MainMenuAssistant(fakeLLM);

        assistant.reply("What is an array?");

        assertTrue(fakeLLM.getLastPrompt().contains("beginner"));
        assertTrue(fakeLLM.getLastPrompt().contains("Syntaxio"));
        assertTrue(fakeLLM.getLastPrompt().contains("Do not generate full assignment solutions"));
    }

    private static class FakeLLMClient implements LLMClient {
        private final String response;
        private String lastPrompt;
        private int callCount;

        FakeLLMClient(String response) {
            this.response = response;
        }

        @Override
        public String generate(String prompt) {
            this.lastPrompt = prompt;
            this.callCount++;
            return response;
        }

        String getLastPrompt() {
            return lastPrompt;
        }

        int getCallCount() {
            return callCount;
        }
    }
}