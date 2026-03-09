package com.sysadminanywhere.directory.service;

import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Base64;
import java.util.Map;

@Service
public class VaultService {

    private final VaultTemplate vaultTemplate;

    public VaultService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public void savePassword(String username, String password) {
        String path = "secret/data/users/" + username;
        // Кодируем пароль в base64
        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
        Map<String, Object> data = Map.of("data", Map.of("password", encodedPassword));
        vaultTemplate.write(path, data);
    }

    public String getPassword(String username) {
        String path = "secret/data/users/" + username;
        VaultResponse response = vaultTemplate.read(path);
        if (response == null || response.getData() == null) {
            return null;
        }
        Map<String, Object> data = (Map<String, Object>) response.getData().get("data");
        if (data == null) {
            return null;
        }

        String encodedPassword = (String) data.get("password");
        if (encodedPassword == null) {
            return null;
        }

        // Декодируем пароль из base64
        byte[] decodedBytes = Base64.getDecoder().decode(encodedPassword);
        return new String(decodedBytes);
    }

}