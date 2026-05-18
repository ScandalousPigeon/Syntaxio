package com.example.syntaxio.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AIService {

    private final HttpClient client = HttpClient.newHttpClient();

    public String getHint(String userCode, String puzzleDescription) {
        String prompt = """
                You are helping a beginner programmer.
                Give a helpful hint, but do not give the full solution.

                Puzzle:
                %s

                User code:
                %s
                """.formatted(puzzleDescription, userCode);

        String jsonBody = """
                {
                  "model": "qwen2.5-coder:3b",
                  "prompt": %s,
                  "stream": false
                }
                """.formatted(toJsonString(prompt));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.body();

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error contacting local AI model: " + e.getMessage();
        }
    }

    private String toJsonString(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                + "\"";
    }
}