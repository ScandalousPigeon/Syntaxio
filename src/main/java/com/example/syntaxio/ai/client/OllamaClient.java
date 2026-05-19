package com.example.syntaxio.ai.client;

public class OllamaClient implements LLMClient {

    @Override
    public String generate(String prompt) {
        // send prompt to http://localhost:11434/api/generate (default location)
        return "real Ollama response";
    }
}