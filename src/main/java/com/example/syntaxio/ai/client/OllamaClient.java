package com.example.syntaxio.ai.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OllamaClient implements LLMClient {

    private static final URI DEFAULT_ENDPOINT = URI.create("http://localhost:11434/api/generate");
    private static final String DEFAULT_MODEL = "llama3.2";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);

    private final HttpClient httpClient;
    private final URI endpoint;
    private final String model;
    private final Duration timeout;

    public OllamaClient() {
        this(
                HttpClient.newHttpClient(),
                configuredEndpoint(),
                configuredModel(),
                DEFAULT_TIMEOUT
        );
    }

    OllamaClient(HttpClient httpClient, URI endpoint, String model, Duration timeout) {
        this.httpClient = httpClient;
        this.endpoint = endpoint;
        this.model = model;
        this.timeout = timeout;
    }

    @Override
    public String generate(String prompt) {
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(prompt)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Ollama request failed with status " + response.statusCode() + ": " + response.body()
                );
            }

            return extractJsonStringField(response.body(), "response");
        } catch (IOException e) {
            throw new IllegalStateException("Could not connect to Ollama at " + endpoint + ".", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Ollama request was interrupted.", e);
        }
    }

    private String buildRequestBody(String prompt) {
        return """
                {"model":"%s","prompt":"%s","stream":false}
                """.formatted(escapeJson(model), escapeJson(prompt));
    }

    static String extractJsonStringField(String json, String fieldName) {
        String label = "\"" + fieldName + "\"";
        int labelIndex = json.indexOf(label);

        if (labelIndex == -1) {
            throw new IllegalStateException("Ollama response did not include field: " + fieldName);
        }

        int colonIndex = json.indexOf(':', labelIndex + label.length());
        if (colonIndex == -1) {
            throw new IllegalStateException("Malformed Ollama response.");
        }

        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length() || json.charAt(valueStart) != '"') {
            throw new IllegalStateException("Ollama response field was not a string: " + fieldName);
        }

        StringBuilder value = new StringBuilder();
        boolean escaping = false;

        for (int i = valueStart + 1; i < json.length(); i++) {
            char current = json.charAt(i);

            if (escaping) {
                if (current == 'u') {
                    if (i + 4 >= json.length()) {
                        throw new IllegalStateException("Malformed unicode escape in Ollama response.");
                    }
                    value.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                    i += 4;
                } else {
                    value.append(unescapeJsonCharacter(current));
                }
                escaping = false;
            } else if (current == '\\') {
                escaping = true;
            } else if (current == '"') {
                return value.toString();
            } else {
                value.append(current);
            }
        }

        throw new IllegalStateException("Malformed Ollama response.");
    }

    private static char unescapeJsonCharacter(char escape) {
        return switch (escape) {
            case '"' -> '"';
            case '\\' -> '\\';
            case '/' -> '/';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            default -> throw new IllegalStateException("Unsupported escape sequence in Ollama response: \\" + escape);
        };
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (current < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) current));
                    } else {
                        escaped.append(current);
                    }
                }
            }
        }

        return escaped.toString();
    }

    private static URI configuredEndpoint() {
        String configured = System.getProperty("syntaxio.ollama.endpoint");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("OLLAMA_ENDPOINT");
        }

        return configured == null || configured.isBlank() ? DEFAULT_ENDPOINT : URI.create(configured);
    }

    private static String configuredModel() {
        String configured = System.getProperty("syntaxio.ollama.model");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("OLLAMA_MODEL");
        }

        return configured == null || configured.isBlank() ? DEFAULT_MODEL : configured;
    }
}
