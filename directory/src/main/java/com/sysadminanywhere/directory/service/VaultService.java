package com.sysadminanywhere.directory.service;

import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Base64;
import java.util.Map;

@Service
public class VaultService {

    private static final String DEFAULT_SERVICE = "legacy";

    private final VaultTemplate vaultTemplate;

    public VaultService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public void savePassword(String username, String password) {
        savePassword(DEFAULT_SERVICE, username, password);
    }

    public void savePassword(String service, String username, String password) {
        String safeService = normalizeService(service);
        String path = "secret/data/sysadminanywhere/users/" + safeService + "/" + username;
        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
        Map<String, Object> data = Map.of("data", Map.of("password", encodedPassword));
        vaultTemplate.write(path, data);
    }

    public String getPassword(String username) {
        return getPassword(DEFAULT_SERVICE, username);
    }

    public String getPassword(String service, String username) {
        String safeService = normalizeService(service);
        String path = "secret/data/sysadminanywhere/users/" + safeService + "/" + username;
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

        byte[] decodedBytes = Base64.getDecoder().decode(encodedPassword);
        return new String(decodedBytes);
    }

    private String normalizeService(String service) {
        if (service == null || service.isBlank()) {
            return DEFAULT_SERVICE;
        }
        return service.trim().toLowerCase();
    }

}
