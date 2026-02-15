package com.sysadminanywhere.directory.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.ldap.client.api.*;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserConnectionManager {
    private final Map<String, LdapConnection> connections = new ConcurrentHashMap<>();

    // Базовые настройки (хост, порт, ssl) из вашего основного конфига
    private final LdapConnectionConfig baseConfig;

    public UserConnectionManager(LdapConnectionConfig baseConfig) {
        this.baseConfig = baseConfig;
    }

    public LdapConnection getConnection() throws Exception {
        LdapConnection connection = null;
            try {
                LdapConnection ldapConnection = new LdapNetworkConnection(createSpecificConfig());
                ldapConnection.connect();
                return ldapConnection;
            } catch (Exception e) {
                throw new RuntimeException("Could not create connection", e);
            }
    }

    public LdapConnection getConnection(String username, String password) throws Exception {
        LdapConnection connection = connections.computeIfAbsent(username, dn -> {
            try {
                LdapConnection ldapConnection = new LdapNetworkConnection(createSpecificConfig());
                ldapConnection.connect();
                ldapConnection.bind(createBindRequest(dn, password));
                log.info("Created new connection for user: {}", dn);

                return ldapConnection;
            } catch (Exception e) {
                throw new RuntimeException("Could not create connection for user: " + dn, e);
            }
        });

        return connection;
    }

    private LdapConnectionConfig createSpecificConfig() {
        LdapConnectionConfig config = new LdapConnectionConfig();
        config.setLdapHost(baseConfig.getLdapHost());
        config.setLdapPort(baseConfig.getLdapPort());
        config.setUseSsl(baseConfig.isUseSsl());
        config.setTrustManagers(baseConfig.getTrustManagers());

        config.setCloseTimeout(5000L);
        config.setTimeout(30000L);

        return config;
    }

    private BindRequest createBindRequest(String username, String password) {
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setName(username);
        bindRequest.setCredentials(password);
        bindRequest.setSimple(true);
        return bindRequest;
    }

}