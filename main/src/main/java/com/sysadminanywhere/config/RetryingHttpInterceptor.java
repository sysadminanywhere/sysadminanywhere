package com.sysadminanywhere.config;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/** Retries only idempotent requests and transient server failures. */
public final class RetryingHttpInterceptor implements ClientHttpRequestInterceptor {
    private final int attempts;
    private final long delayMs;

    public RetryingHttpInterceptor(int attempts, long delayMs) {
        this.attempts = Math.max(1, attempts);
        this.delayMs = Math.max(0, delayMs);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (request.getMethod() != HttpMethod.GET && request.getMethod() != HttpMethod.HEAD) {
            return execution.execute(request, body);
        }
        IOException last = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                ClientHttpResponse response = execution.execute(request, body);
                if (response.getStatusCode().is5xxServerError() && attempt < attempts) {
                    response.close();
                    pause();
                    continue;
                }
                return response;
            } catch (IOException exception) {
                last = exception;
                if (attempt == attempts) throw exception;
                pause();
            }
        }
        throw last == null ? new IOException("HTTP request failed") : last;
    }

    private void pause() throws IOException {
        try { Thread.sleep(delayMs); }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("HTTP retry interrupted", exception);
        }
    }
}
