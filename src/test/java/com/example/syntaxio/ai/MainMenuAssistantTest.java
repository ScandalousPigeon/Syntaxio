package com.example.syntaxio.ai;

import com.example.syntaxio.ai.chat.MainMenuAssistant;
import com.example.syntaxio.ai.client.LLMClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainMenuAssistantTest {

    @Test
    void replyReturnsAIResponse() {
        FakeLLMClient fakeLLM = new FakeLLMClient("A stack is a last-in, first-out data structure.");
        MainMenuAssistant assistant = new MainMenuAssistant(fakeLLM);

        String response = assistant.reply("What is a stack?");

        assertEquals("A stack is a last-in, first-out data structure.", response);
    }

    @Test
    void replyCallsLLMClientOnce() {
        FakeLLMClient fakeLLM = new FakeLLMClient("AI response");
        MainMenuAssistant assistant = new MainMenuAssistant(fakeLLM);

        assistant.reply("Explain recursion");

        assertEquals(1, fakeLLM.getCallCount());
    }

    @Test
    void replyIncludesUserMessageInPrompt() {
        FakeLLMClient fakeLLM = new FakeLLMClient("AI response");
        MainMenuAssistant assistant = new MainMenuAssistant(fakeLLM);

        assistant.reply("Explain arrays");

        assertTrue(fakeLLM.getLastPrompt().contains("Explain arrays"));
    }

    @Test
    void replyPromptMentionsSyntaxio() {
        FakeLLMClient fakeLLM = new FakeLLMClient("AI response");
        MainMenuAssistant assistant = new MainMenuAssistant(fakeLLM);

        assistant.reply("Hello");

        assertTrue(fakeLLM.getLastPrompt().contains("Syntaxio"));
    }

    @Test
    void replyPromptIsBeginnerFriendly() {
        FakeLLMClient fakeLLM = new FakeLLMClient("AI response");
        MainMenuAssistant assistant = new MainMenuAssistant(fakeLLM);

        assistant.reply("What is a loop?");

        assertTrue(fakeLLM.getLastPrompt().contains("beginner-friendly"));
    }

    @Test
    void replyPromptDoesNotAllowFullAssignmentSolutions() {
        FakeLLMClient fakeLLM = new FakeLLMClient("AI response");
        MainMenuAssistant assistant = new MainMenuAssistant(fakeLLM);

        assistant.reply("Do my assignment");

        assertTrue(fakeLLM.getLastPrompt().contains("Do not generate full assignment solutions"));
    }

    private static class FakeLLMClient implements LLMClient {
        private final String response;
        private int callCount;
        private String lastPrompt;

        FakeLLMClient(String response) {
            this.response = response;
        }

        @Override
        public String generate(String prompt) {
            callCount++;
            lastPrompt = prompt;
            return response;
        }

        int getCallCount() {
            return callCount;
        }

        String getLastPrompt() {
            return lastPrompt;
        }
    }
}