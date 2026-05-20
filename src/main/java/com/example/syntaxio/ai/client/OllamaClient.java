package com.example.syntaxio.ai.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OllamaClient implements LLMClient {

    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public String generate(String prompt) {
        String jsonBody = """
                {
                  "model": "qwen2.5-coder:3b",
                  "prompt": "%s",
                  "stream": false
                }
                """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return extractResponse(response.body());
        } catch (IOException | InterruptedException e) {
            return "Error contacting Ollama: " + e.getMessage();
        }
    }

    private String extractResponse(String json) {
        String key = "\"response\":\"";
        int start = json.indexOf(key);

        if (start == -1) {
            return json;
        }

        start += key.length();
        int end = json.indexOf("\",\"done\"", start);

        if (end == -1) {
            return json.substring(start);
        }

        return json.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
    }
}