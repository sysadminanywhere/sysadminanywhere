package com.sysadminanywhere.directory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

@Configuration
public class VaultConfig {

    @Value("${spring.cloud.vault.uri:http://localhost:8200}")
    private String vaultUri;

    @Value("${spring.cloud.vault.token:}")
    private String vaultToken;

    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint endpoint = VaultEndpoint.from(vaultUri);
        return new VaultTemplate(endpoint, new TokenAuthentication(vaultToken));
    }
}
