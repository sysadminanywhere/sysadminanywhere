package com.sysadminanywhere.directory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    @Test
    void constructor_shouldCreateVaultService() {
        VaultService vaultService = new VaultService(vaultTemplate);
        assertNotNull(vaultService);
    }

    @Test
    void normalizeService_shouldReturnLowercase() throws Exception {
        VaultService vaultService = new VaultService(vaultTemplate);
        
        var method = VaultService.class.getDeclaredMethod("normalizeService", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(vaultService, "WEB");
        
        assertEquals("web", result);
    }

    @Test
    void normalizeService_shouldHandleEmpty() throws Exception {
        VaultService vaultService = new VaultService(vaultTemplate);
        
        var method = VaultService.class.getDeclaredMethod("normalizeService", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(vaultService, "");
        
        assertEquals("legacy", result);
    }
}
