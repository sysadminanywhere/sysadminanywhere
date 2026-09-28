package com.sysadminanywhere.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.model.WebhookSubscription;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class WebhookService {
    private final ObjectMapper objectMapper;
    private final Path configPath;
    private final HttpClient httpClient;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    public WebhookService(ObjectMapper objectMapper,
                          @Value("${webhooks.config-path:${user.dir}/data/webhooks.json}") String configPath) {
        this.objectMapper = objectMapper;
        this.configPath = Paths.get(configPath);
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public synchronized List<WebhookSubscription> list() {
        if (!Files.exists(configPath)) return new ArrayList<>();
        try {
            List<WebhookSubscription> result = objectMapper.readValue(configPath.toFile(), new TypeReference<List<WebhookSubscription>>() {});
            return result == null ? new ArrayList<>() : result;
        } catch (Exception exception) {
            log.warn("Unable to read webhook configuration: {}", exception.getMessage());
            return new ArrayList<>();
        }
    }

    public synchronized WebhookSubscription save(WebhookSubscription subscription) {
        validate(subscription);
        if (subscription.getId() == null || subscription.getId().isBlank()) subscription.setId(UUID.randomUUID().toString());
        List<WebhookSubscription> subscriptions = list();
        subscriptions.removeIf(item -> Objects.equals(item.getId(), subscription.getId()));
        subscriptions.add(subscription);
        write(subscriptions);
        return subscription;
    }

    public synchronized void delete(String id) {
        List<WebhookSubscription> subscriptions = list();
        subscriptions.removeIf(item -> Objects.equals(item.getId(), id));
        write(subscriptions);
    }

    public void test(String id) {
        WebhookSubscription subscription = list().stream().filter(item -> Objects.equals(item.getId(), id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown webhook"));
        executor.submit(() -> deliver(subscription, "webhook.test", Map.of("message", "Webhook delivery test")));
    }

    public void publish(String event, Object data) {
        Map<String, Object> payload = Map.of("event", event, "timestamp", Instant.now().toString(), "data", data);
        for (WebhookSubscription subscription : list()) {
            if (!subscription.isEnabled() || !accepts(subscription, event)) continue;
            executor.submit(() -> deliver(subscription, event, payload));
        }
    }

    private boolean accepts(WebhookSubscription subscription, String event) {
        return subscription.getEvents() == null || subscription.getEvents().isEmpty() ||
                subscription.getEvents().stream().anyMatch(value -> "*".equals(value) || event.equalsIgnoreCase(value));
    }

    private void deliver(WebhookSubscription subscription, String event, Object payload) {
        try {
            String body = objectMapper.writeValueAsString(payload);
            String signature = sign(subscription.getSecret(), body);
            Exception failure = null;
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    HttpRequest request = HttpRequest.newBuilder(URI.create(subscription.getUrl()))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .header("X-Sysadmin-Event", event)
                            .header("X-Sysadmin-Signature", "sha256=" + signature)
                            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() >= 200 && response.statusCode() < 300) return;
                    failure = new IllegalStateException("HTTP " + response.statusCode());
                } catch (Exception exception) {
                    failure = exception;
                }
                Thread.sleep(attempt * 500L);
            }
            log.warn("Webhook delivery failed after retries for {}: {}", subscription.getUrl(), failure == null ? "unknown" : failure.getMessage());
        } catch (Exception exception) {
            log.warn("Webhook payload failed: {}", exception.getMessage());
        }
    }

    private String sign(String secret, String body) throws Exception {
        if (secret == null || secret.isBlank()) return "";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        StringBuilder result = new StringBuilder();
        for (byte value : mac.doFinal(body.getBytes(StandardCharsets.UTF_8))) result.append(String.format("%02x", value));
        return result.toString();
    }

    private void validate(WebhookSubscription subscription) {
        if (subscription == null || subscription.getUrl() == null || subscription.getUrl().isBlank()) {
            throw new IllegalArgumentException("Webhook URL is required");
        }
        URI uri;
        try { uri = URI.create(subscription.getUrl()); }
        catch (Exception exception) { throw new IllegalArgumentException("Invalid webhook URL"); }
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Webhook URL must use HTTP or HTTPS");
        }
    }

    private void write(List<WebhookSubscription> subscriptions) {
        try {
            if (configPath.getParent() != null) Files.createDirectories(configPath.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configPath.toFile(), subscriptions);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to save webhook configuration", exception);
        }
    }
}
