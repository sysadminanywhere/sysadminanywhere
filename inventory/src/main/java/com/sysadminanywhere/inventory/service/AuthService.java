package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.common.directory.dto.LoginRequest;
import com.sysadminanywhere.inventory.client.AuthServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String CLIENT_SERVICE = "inventory";

    private final AuthServiceClient client;
    private final int maxAttempts;
    private final long retryDelayMs;

    public AuthService(AuthServiceClient client,
                       @Value("${directory.auth.max-attempts:3}") int maxAttempts,
                       @Value("${directory.auth.retry-delay-ms:2000}") long retryDelayMs) {
        this.client = client;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.retryDelayMs = Math.max(0, retryDelayMs);
    }

    public JwtResponse authenticate(String username, String password) {
        LoginRequest request = new LoginRequest(username, password, CLIENT_SERVICE);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                JwtResponse response = client.authenticate(request);
                if (response == null || response.token() == null) {
                    throw new IllegalStateException("Directory service returned an empty authentication response");
                }
                return response;
            } catch (RestClientResponseException exception) {
                // A 4xx response is a credential/configuration problem, not a transient outage.
                throw exception;
            } catch (ResourceAccessException exception) {
                if (attempt == maxAttempts) throw new IllegalStateException(
                        "Directory service is unavailable after " + maxAttempts + " attempts", exception);
                sleepBeforeRetry();
            }
        }
        throw new IllegalStateException("Directory authentication did not complete");
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(retryDelayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Directory authentication retry interrupted", exception);
        }
    }

}
