package com.sysadminanywhere.directory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

@Service
public class VaultService {

    private final VaultTemplate vaultTemplate;

    public VaultService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public void savePassword(String username, String password) {
        String path = "secret/data/users/" + username;
        Map<String, Object> data = Map.of("data", Map.of("password", password));
        vaultTemplate.write(path, data);
    }

    public String getPassword(String username) {
        String path = "secret/data/users/" + username;
        VaultResponse response = vaultTemplate.read(path);
        if (response == null || response.getData() == null) {
            return null;
        }
        Map<String, Object> data = (Map<String, Object>) response.getData().get("data");
        return data != null ? (String) data.get("password") : null;
    }

}