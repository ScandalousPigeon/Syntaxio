package com.example.syntaxio.ai.client;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import io.github.ollama4j.models.response.OllamaResult;

import java.time.Duration;

public class OllamaClient implements LLMClient {

    private static final String DEFAULT_HOST = "http://localhost:11434";
    private static final String DEFAULT_MODEL = "qwen2.5-coder:3b";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);

    private final OllamaGenerator ollama;
    private final String host;
    private final String model;

    public OllamaClient() {
        this(configuredHost(), configuredModel(), DEFAULT_TIMEOUT);
    }

    private OllamaClient(String host, String model, Duration timeout) {
        Ollama ollama = new Ollama(host);
        ollama.setRequestTimeoutSeconds(timeout.toSeconds());

        this.ollama = request -> ollama.generate(request, null);
        this.host = host;
        this.model = model;
    }

    OllamaClient(OllamaGenerator ollama, String host, String model) {
        this.ollama = ollama;
        this.host = host;
        this.model = model;
    }

    @Override
    public String generate(String prompt) {
        OllamaGenerateRequest request = OllamaGenerateRequest.builder()
                .withModel(model)
                .withPrompt(prompt)
                .withStreaming(false)
                .build();

        try {
            return ollama.generate(request).getResponse();
        } catch (OllamaException e) {
            throw new IllegalStateException("Ollama request failed at " + host + ".", e);
        }
    }

    private static String configuredHost() {
        String configured = System.getProperty("syntaxio.ollama.host");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("OLLAMA_HOST");
        }
        if (configured == null || configured.isBlank()) {
            configured = System.getProperty("syntaxio.ollama.endpoint");
        }
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("OLLAMA_ENDPOINT");
        }

        return configured == null || configured.isBlank() ? DEFAULT_HOST : normalizeHost(configured);
    }

    private static String configuredModel() {
        String configured = System.getProperty("syntaxio.ollama.model");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("OLLAMA_MODEL");
        }

        return configured == null || configured.isBlank() ? DEFAULT_MODEL : configured;
    }

    private static String normalizeHost(String configured) {
        String host = configured.trim();
        String generateEndpoint = "/api/generate";

        if (host.endsWith(generateEndpoint)) {
            host = host.substring(0, host.length() - generateEndpoint.length());
        }

        while (host.endsWith("/") && host.length() > 1) {
            host = host.substring(0, host.length() - 1);
        }

        return host;
    }

    interface OllamaGenerator {
        OllamaResult generate(OllamaGenerateRequest request) throws OllamaException;
    }
}
