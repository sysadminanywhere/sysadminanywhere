package com.sysadminanywhere.directory.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.ldap.client.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserConnectionManager {

    @Value("${ldap.pool.ttl-ms:600000}")
    private long poolTtlMs;

    private final Map<String, UserConnectionHolder> connections = new ConcurrentHashMap<>();

    private static class UserConnectionHolder {
        final LdapConnection connection;
        volatile long lastUsed;

        UserConnectionHolder(LdapConnection connection) {
            this.connection = connection;
            this.lastUsed = System.currentTimeMillis();
        }

        void touch() {
            this.lastUsed = System.currentTimeMillis();
        }
    }

    private final LdapConnectionConfig baseConfig;

    public UserConnectionManager(LdapConnectionConfig baseConfig) {
        this.baseConfig = baseConfig;
    }

    public LdapConnection getConnection(String username, String password) throws Exception {
        UserConnectionHolder holder = connections.computeIfAbsent(username, dn -> {
            try {
                LdapConnection ldapConnection = new LdapNetworkConnection(createSpecificConfig());
                ldapConnection.connect();
                ldapConnection.bind(createBindRequest(dn, password));
                log.info("Created new connection for user: {}", dn);

                return new UserConnectionHolder(ldapConnection);
            } catch (Exception e) {
                throw new RuntimeException("Could not create connection for user: " + dn, e);
            }
        });

        holder.touch();

        return holder.connection;
    }

    public LdapConnectionConfig createSpecificConfig() {
        LdapConnectionConfig config = new LdapConnectionConfig();
        config.setLdapHost(baseConfig.getLdapHost());
        config.setLdapPort(baseConfig.getLdapPort());
        config.setUseSsl(baseConfig.isUseSsl());
        config.setTrustManagers(baseConfig.getTrustManagers());

        config.setCloseTimeout(5000L);
        config.setTimeout(30000L);

        return config;
    }

    public BindRequest createBindRequest(String username, String password) {
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setName(username);
        bindRequest.setCredentials(password);
        bindRequest.setSimple(true);
        return bindRequest;
    }

    @Scheduled(fixedDelay = 60_000)
    public void cleanupIdlePools() {

        long now = System.currentTimeMillis();

        connections.forEach((username, holder) -> {

            long idleTime = now - holder.lastUsed;

            if (idleTime > poolTtlMs) {

                if (connections.remove(username, holder)) {
                    try {
                        log.info("Авто-закрытие LDAP-пула пользователя {} (idle {} ms)",
                                username, idleTime);

                        holder.connection.close();

                    } catch (Exception e) {
                        log.warn("Ошибка при авто-закрытии пула {}: {}",
                                username, e.getMessage());
                    }
                }
            }
        });
    }

}