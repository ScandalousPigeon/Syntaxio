package com.example.syntaxio.ai.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OllamaClientTest {

    @Test
    void extractJsonStringFieldReadsOllamaResponseText() {
        String json = """
                {"model":"llama3.2","response":"TITLE:\\nGenerated Puzzle\\n\\nDESCRIPTION:\\nDo work.","done":true}
                """;

        String response = OllamaClient.extractJsonStringField(json, "response");

        assertEquals("TITLE:\nGenerated Puzzle\n\nDESCRIPTION:\nDo work.", response);
    }
}
