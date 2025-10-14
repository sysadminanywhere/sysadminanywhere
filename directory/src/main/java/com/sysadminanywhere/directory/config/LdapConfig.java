package com.sysadminanywhere.directory.config;

import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.ldap.client.api.NoVerificationTrustManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

@Configuration
public class LdapConfig {

    @Autowired
    private VaultTemplate vaultTemplate;

    @Value("${ldap.host.server:localhost}")
    private String server;

    @Value("${ldap.host.port:389}")
    private int port;

    @SneakyThrows
    @Bean
    public LdapConnection createConnection(LdapConnectionConfig sslConfig) {
        LdapConnection connection = new LdapNetworkConnection(sslConfig);

        VaultResponse response = vaultTemplate
                .opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get("SysadminAnywhere");

        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setCredentials(response.getData().get("Password").toString());
        bindRequest.setSimple(true);
        bindRequest.setName(response.getData().get("UserName").toString());

        connection.bind(bindRequest);

        return connection;
    }

    @Bean
    public LdapConnectionConfig sslConfig() {
        LdapConnectionConfig sslConfig = new LdapConnectionConfig();
        sslConfig.setLdapHost(server);
        sslConfig.setUseSsl(true);
        sslConfig.setLdapPort(port);
        sslConfig.setTrustManagers(new NoVerificationTrustManager());
        return sslConfig;
    }

    @Bean
    public DirectoryConfig directoryConfig() {
        DirectoryConfig directoryConfig = new DirectoryConfig();

//        if(!groupsAllowed.isEmpty())
//            directoryConfig.setGroupsAllowed(Arrays.asList(groupsAllowed.split(";")));

        return directoryConfig;
    }

}