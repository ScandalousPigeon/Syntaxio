package com.example.syntaxio.ai.client;

import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import io.github.ollama4j.models.response.OllamaResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OllamaClientTest {

    @Test
    void generateReturnsOllama4jResponseText() {
        CapturingOllamaGenerator ollama = new CapturingOllamaGenerator(
                new OllamaResult("TITLE:\nGenerated Puzzle", null, 120, 200)
        );
        OllamaClient client = new OllamaClient(ollama, "http://localhost:11434", "llama3.2");

        String response = client.generate("Create a puzzle");

        assertEquals("TITLE:\nGenerated Puzzle", response);
        assertEquals("llama3.2", ollama.request.getModel());
        assertEquals("Create a puzzle", ollama.request.getPrompt());
        assertFalse(ollama.request.isStream());
    }

    @Test
    void generateWrapsOllama4jFailures() {
        OllamaException failure = new OllamaException("model not found");
        OllamaClient client = new OllamaClient(request -> {
            throw failure;
        }, "http://localhost:11434", "missing-model");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> client.generate("Hello")
        );

        assertEquals("Ollama request failed at http://localhost:11434.", exception.getMessage());
        assertSame(failure, exception.getCause());
    }

    private static class CapturingOllamaGenerator implements OllamaClient.OllamaGenerator {
        private final OllamaResult result;
        private OllamaGenerateRequest request;

        private CapturingOllamaGenerator(OllamaResult result) {
            this.result = result;
        }

        @Override
        public OllamaResult generate(OllamaGenerateRequest request) {
            this.request = request;
            return result;
        }
    }
}
