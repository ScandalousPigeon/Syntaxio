package com.example.syntaxio.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainMenuAssistantTest {

    @Test
    void sendMessageAddsUserMessageAndAIResponse() {
        FakeAIService fakeAI = new FakeAIService("A stack is last-in, first-out data structure.");
        MainMenuAssistant chat = new MainMenuAssistant(fakeAI);

        chat.sendMessage("What is a stack?");

        assertEquals(2, chat.getMessages().size());

        assertEquals("USER", chat.getMessages().get(0).role());
        assertEquals("What is a stack?", chat.getMessages().get(0).content());

        assertEquals("AI", chat.getMessages().get(1).role());
        assertEquals("A stack is last-in, first-out data structure.", chat.getMessages().get(1).content());
    }

    @Test
    void blankMessageDoesNotCallAI() {
        FakeAIService fakeAI = new FakeAIService("This should not be used.");
        MainMenuAssistant chat = new MainMenuAssistant(fakeAI);

        chat.sendMessage("   ");

        assertEquals(0, chat.getMessages().size());
        assertEquals(0, fakeAI.getCallCount());
    }

    @Test
    void aiFailureResultsInFriendlyErrorMessage() {
        FakeAIService fakeAI = new FakeAIService("unused");
        fakeAI.setShouldFail(true);

        MainMenuAssistant chat = new MainMenuAssistant(fakeAI);

        chat.sendMessage("Explain recursion");

        assertEquals(2, chat.getMessages().size());
        assertEquals("USER", chat.getMessages().get(0).role());
        assertEquals("AI", chat.getMessages().get(1).role());
        assertTrue(chat.getMessages().get(1).content().contains("Could not contact AI"));
    }

    private static class FakeAIService implements AIService {
        private final String response;
        private boolean shouldFail;
        private int callCount;

        FakeAIService(String response) {
            this.response = response;
        }

        @Override
        public String ask(String message) {
            callCount++;

            if (shouldFail) {
                throw new RuntimeException("AI failed");
            }

            return response;
        }

        int getCallCount() {
            return callCount;
        }

        void setShouldFail(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }
    }
}