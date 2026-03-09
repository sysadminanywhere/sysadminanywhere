package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.common.directory.dto.LoginRequest;
import com.sysadminanywhere.inventory.client.AuthServiceClient;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthServiceClient client;

    public AuthService(AuthServiceClient client) {
        this.client = client;
    }

    public JwtResponse authenticate(String username, String password) {
        JwtResponse response = client.authenticate(new LoginRequest(username, password));
        if (response == null || response.token() == null) {
            throw new RuntimeException("Authentication failed");
        }
        return response;
    }

}