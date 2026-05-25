package com.example.syntaxio.ai.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OllamaRuntimeManagerTest {

    @Test
    void defaultModelIsQwen25Coder3b() {
        assertEquals("qwen2.5-coder:3b", OllamaClient.DEFAULT_MODEL);
    }

    @Test
    void startsOllamaServeWhenLocalServerIsNotRunning() throws IOException, InterruptedException {
        FakeStatusClient statusClient = new FakeStatusClient(false, true);
        CapturingProcessLauncher processLauncher = new CapturingProcessLauncher();
        OllamaRuntimeManager manager = manager(statusClient, processLauncher);

        assertTrue(manager.startServerIfNeeded());

        assertEquals(List.of(List.of("ollama", "serve")), processLauncher.commands);
    }

    @Test
    void doesNotStartProcessForRemoteOllamaHost() throws IOException, InterruptedException {
        FakeStatusClient statusClient = new FakeStatusClient(false);
        CapturingProcessLauncher processLauncher = new CapturingProcessLauncher();
        OllamaRuntimeManager manager = new OllamaRuntimeManager(
                "http://example.com:11434",
                OllamaClient.DEFAULT_MODEL,
                statusClient,
                processLauncher,
                Duration.ZERO,
                Duration.ZERO
        );

        assertFalse(manager.startServerIfNeeded());

        assertTrue(processLauncher.commands.isEmpty());
    }

    @Test
    void pullsQwenModelWhenOllamaIsRunningAndModelIsMissing() throws IOException, InterruptedException {
        FakeStatusClient statusClient = new FakeStatusClient(true);
        statusClient.modelResponses.add(false);
        statusClient.modelResponses.add(true);
        CapturingProcessLauncher processLauncher = new CapturingProcessLauncher();
        OllamaRuntimeManager manager = manager(statusClient, processLauncher);

        assertTrue(manager.ensureModelAvailable());

        assertEquals(List.of(List.of("ollama", "pull", "qwen2.5-coder:3b")), processLauncher.commands);
    }

    @Test
    void doesNotPullModelWhenItAlreadyExists() throws IOException, InterruptedException {
        FakeStatusClient statusClient = new FakeStatusClient(true);
        statusClient.modelResponses.add(true);
        CapturingProcessLauncher processLauncher = new CapturingProcessLauncher();
        OllamaRuntimeManager manager = manager(statusClient, processLauncher);

        assertTrue(manager.ensureModelAvailable());

        assertTrue(processLauncher.commands.isEmpty());
    }

    private OllamaRuntimeManager manager(
            FakeStatusClient statusClient,
            CapturingProcessLauncher processLauncher
    ) {
        return new OllamaRuntimeManager(
                OllamaClient.DEFAULT_HOST,
                OllamaClient.DEFAULT_MODEL,
                statusClient,
                processLauncher,
                Duration.ofMillis(1),
                Duration.ZERO
        );
    }

    private static class FakeStatusClient implements OllamaRuntimeManager.OllamaStatusClient {
        private final Deque<Boolean> runningResponses = new ArrayDeque<>();
        private final Deque<Boolean> modelResponses = new ArrayDeque<>();

        private FakeStatusClient(boolean... runningResponses) {
            for (boolean response : runningResponses) {
                this.runningResponses.add(response);
            }
        }

        @Override
        public boolean isRunning(String host) {
            return runningResponses.isEmpty() || runningResponses.removeFirst();
        }

        @Override
        public boolean hasModel(String host, String model) {
            return modelResponses.isEmpty() || modelResponses.removeFirst();
        }
    }

    private static class CapturingProcessLauncher implements OllamaRuntimeManager.ProcessLauncher {
        private final List<List<String>> commands = new ArrayList<>();

        @Override
        public Process start(List<String> command) {
            commands.add(command);
            return new FinishedProcess(0);
        }
    }

    private static class FinishedProcess extends Process {
        private final int exitCode;

        private FinishedProcess(int exitCode) {
            this.exitCode = exitCode;
        }

        @Override
        public OutputStream getOutputStream() {
            return OutputStream.nullOutputStream();
        }

        @Override
        public java.io.InputStream getInputStream() {
            return java.io.InputStream.nullInputStream();
        }

        @Override
        public java.io.InputStream getErrorStream() {
            return java.io.InputStream.nullInputStream();
        }

        @Override
        public int waitFor() {
            return exitCode;
        }

        @Override
        public int exitValue() {
            return exitCode;
        }

        @Override
        public void destroy() {
        }

        @Override
        public boolean isAlive() {
            return false;
        }
    }
}
