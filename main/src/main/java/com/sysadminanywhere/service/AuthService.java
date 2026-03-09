package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.AuthServiceClient;
import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.common.directory.dto.LoginRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String CLIENT_SERVICE = "main";

    private final AuthServiceClient client;

    public AuthService(AuthServiceClient client) {
        this.client = client;
    }

    public JwtResponse authenticate(String username, String password) {
        JwtResponse response = client.authenticate(new LoginRequest(username, password, CLIENT_SERVICE));
        if (response == null || response.token() == null) {
            throw new RuntimeException("Authentication failed");
        }
        return response;
    }

}
