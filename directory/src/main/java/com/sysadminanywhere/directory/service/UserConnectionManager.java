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

    @Value("${ldap.host.server:localhost}")
    private String server;

    @Value("${ldap.host.port:389}")
    private int port;

    @Value("${ldap.host.use.ssl:false}")
    private boolean useSsl;

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

    /**
     * Получить LDAP соединение для пользователя
     * @param username имя пользователя (DN)
     * @param password пароль пользователя
     * @return LDAP соединение
     */
    public LdapConnection getConnection(String username, String password) {
        UserConnectionHolder holder = connections.computeIfAbsent(username, dn -> {
            try {
                LdapConnection ldapConnection = new LdapNetworkConnection(createSpecificConfig());
                ldapConnection.connect();
                ldapConnection.bind(createBindRequest(dn, password));
                log.info("Created new LDAP connection for user: {}", dn);

                return new UserConnectionHolder(ldapConnection);
            } catch (Exception e) {
                log.error("Failed to create LDAP connection for user {}: {}", dn, e.getMessage());
                throw new RuntimeException("Could not create connection for user: " + dn, e);
            }
        });

        holder.touch();
        log.debug("Using existing LDAP connection for user: {}", username);

        return holder.connection;
    }

    /**
     * Создать конфигурацию LDAP соединения
     * @return конфигурация LDAP соединения
     */
    public LdapConnectionConfig createSpecificConfig() {
        LdapConnectionConfig config = new LdapConnectionConfig();
        config.setLdapHost(server);
        config.setLdapPort(port);
        config.setUseSsl(useSsl);
        config.setTrustManagers(new NoVerificationTrustManager());

        config.setCloseTimeout(500L);
        config.setTimeout(30000L);

        log.debug("Created LDAP connection config for host: {}:{} (SSL: {})", server, port, useSsl);
        return config;
    }

    /**
     * Создать BIND запрос для аутентификации
     * @param username имя пользователя (DN)
     * @param password пароль пользователя
     * @return BIND запрос
     */
    public BindRequest createBindRequest(String username, String password) {
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setName(username);
        bindRequest.setCredentials(password);
        bindRequest.setSimple(true);
        log.debug("Created BIND request for user: {}", username);
        return bindRequest;
    }

    /**
     * Периодическая очистка неиспользуемых LDAP соединений (запускается каждые 60 сек)
     */
    @Scheduled(fixedDelay = 60_000)
    public void cleanupIdlePools() {
        long now = System.currentTimeMillis();
        int closedConnections = 0;

        for (Map.Entry<String, UserConnectionHolder> entry : connections.entrySet()) {
            String username = entry.getKey();
            UserConnectionHolder holder = entry.getValue();
            long idleTime = now - holder.lastUsed;

            if (idleTime > poolTtlMs) {
                if (connections.remove(username, holder)) {
                    try {
                        log.info("Auto-closing LDAP pool for user {} (idle {} ms)",
                                username, idleTime);
                        holder.connection.close();
                        closedConnections++;

                    } catch (Exception e) {
                        log.warn("Error auto-closing pool for {}: {}",
                                username, e.getMessage());
                    }
                }
            }
        }

        if (closedConnections > 0) {
            log.debug("Cleanup completed. Closed {} idle connections", closedConnections);
        }
    }

}