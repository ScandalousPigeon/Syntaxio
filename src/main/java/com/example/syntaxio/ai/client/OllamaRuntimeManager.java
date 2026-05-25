package com.example.syntaxio.ai.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class OllamaRuntimeManager {

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(250);

    private final String host;
    private final String model;
    private final OllamaStatusClient statusClient;
    private final ProcessLauncher processLauncher;
    private final Duration startupTimeout;
    private final Duration pollInterval;

    private volatile boolean startupRequested;
    private volatile Process serverProcess;
    private volatile Process modelPullProcess;

    public OllamaRuntimeManager() {
        this(
                OllamaClient.configuredHost(),
                OllamaClient.configuredModel(),
                new HttpOllamaStatusClient(),
                command -> new ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .start(),
                STARTUP_TIMEOUT,
                POLL_INTERVAL
        );
    }

    OllamaRuntimeManager(
            String host,
            String model,
            OllamaStatusClient statusClient,
            ProcessLauncher processLauncher,
            Duration startupTimeout,
            Duration pollInterval
    ) {
        this.host = host;
        this.model = model;
        this.statusClient = statusClient;
        this.processLauncher = processLauncher;
        this.startupTimeout = startupTimeout;
        this.pollInterval = pollInterval;
    }

    public void startInBackground() {
        synchronized (this) {
            if (startupRequested) {
                return;
            }
            startupRequested = true;
        }

        Thread startupThread = new Thread(this::startServerAndPrepareModel, "ollama-runtime-startup");
        startupThread.setDaemon(true);
        startupThread.start();
    }

    public void stopManagedProcesses() {
        destroyIfAlive(modelPullProcess);
        destroyIfAlive(serverProcess);
    }

    void startServerAndPrepareModel() {
        try {
            if (startServerIfNeeded()) {
                ensureModelAvailable();
            }
        } catch (InterruptedException e) {
            System.err.println("Ollama startup was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("Unable to start Ollama automatically: " + e.getMessage());
        }
    }

    boolean startServerIfNeeded() throws IOException, InterruptedException {
        if (statusClient.isRunning(host)) {
            return true;
        }
        if (!isLocalHost(host)) {
            return false;
        }

        serverProcess = processLauncher.start(List.of("ollama", "serve"));
        return waitUntilRunning();
    }

    boolean ensureModelAvailable() throws IOException, InterruptedException {
        if (!statusClient.isRunning(host)) {
            return false;
        }
        if (statusClient.hasModel(host, model)) {
            return true;
        }
        if (!isLocalHost(host)) {
            return false;
        }

        modelPullProcess = processLauncher.start(List.of("ollama", "pull", model));
        int exitCode = modelPullProcess.waitFor();
        return exitCode == 0 && statusClient.hasModel(host, model);
    }

    private boolean waitUntilRunning() throws InterruptedException {
        long deadline = System.nanoTime() + startupTimeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (statusClient.isRunning(host)) {
                return true;
            }
            Thread.sleep(pollInterval.toMillis());
        }
        return statusClient.isRunning(host);
    }

    private boolean isLocalHost(String configuredHost) {
        String uriHost = URI.create(configuredHost).getHost();
        return uriHost != null
                && (uriHost.equalsIgnoreCase("localhost")
                || uriHost.equals("127.0.0.1")
                || uriHost.equals("0.0.0.0")
                || uriHost.equals("::1")
                || uriHost.equals("[::1]"));
    }

    private void destroyIfAlive(Process process) {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    interface ProcessLauncher {
        Process start(List<String> command) throws IOException;
    }

    interface OllamaStatusClient {
        boolean isRunning(String host);

        boolean hasModel(String host, String model);
    }

    private static class HttpOllamaStatusClient implements OllamaStatusClient {
        private final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(HTTP_TIMEOUT)
                .build();

        @Override
        public boolean isRunning(String host) {
            HttpResponse<String> response = getTags(host);
            return response != null && response.statusCode() >= 200 && response.statusCode() < 300;
        }

        @Override
        public boolean hasModel(String host, String model) {
            HttpResponse<String> response = getTags(host);
            return response != null
                    && response.statusCode() >= 200
                    && response.statusCode() < 300
                    && response.body().contains("\"" + model + "\"");
        }

        private HttpResponse<String> getTags(String host) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(host + "/api/tags"))
                    .timeout(HTTP_TIMEOUT)
                    .GET()
                    .build();

            try {
                return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }
        }
    }
}
