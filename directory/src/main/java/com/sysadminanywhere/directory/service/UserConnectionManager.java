package com.sysadminanywhere.directory.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.ldap.client.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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

    @Value("${ldap.host.verify-certificate:false}")
    private boolean verifyCertificate;

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

    public LdapConnection getConnection(String username, String password) {
        return getConnection("legacy", username, password);
    }

    /**
     * Получить LDAP соединение для пользователя в контексте сервиса
     * @param service имя сервиса-источника токена
     * @param username имя пользователя (DN для bind)
     * @param password пароль пользователя
     * @return LDAP соединение
     */
    public LdapConnection getConnection(String service, String username, String password) {
        String safeService = normalizeService(service);
        String connectionKey = safeService + "|" + username;

        UserConnectionHolder holder = connections.computeIfAbsent(connectionKey, key -> {
            try {
                LdapConnection ldapConnection = new LdapNetworkConnection(createSpecificConfig());
                ldapConnection.connect();
                ldapConnection.bind(createBindRequest(username, password));
                log.info("Created new LDAP connection for service: {}, user: {}", safeService, username);

                return new UserConnectionHolder(ldapConnection);
            } catch (Exception e) {
                log.error("Failed to create LDAP connection for service {}, user {}: {}", safeService, username, e.getMessage());
                throw new RuntimeException("Could not create connection for service/user: " + connectionKey, e);
            }
        });

        holder.touch();
        log.debug("Using LDAP connection for service: {}, user: {}", safeService, username);

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
        if (useSsl && !verifyCertificate) {
            // Preserve the existing application behavior: AD certificates are trusted
            // without requiring them to be installed in the JVM trust store.
            config.setTrustManagers(new NoVerificationTrustManager());
        } else if (useSsl) {
            // Leave the default JVM trust managers in place when certificate
            // verification is explicitly enabled.
            log.info("LDAP certificate verification is enabled");
        }

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
            String connectionKey = entry.getKey();
            UserConnectionHolder holder = entry.getValue();
            long idleTime = now - holder.lastUsed;

            if (idleTime > poolTtlMs) {
                if (connections.remove(connectionKey, holder)) {
                    try {
                        log.info("Auto-closing LDAP pool for {} (idle {} ms)", connectionKey, idleTime);
                        closeConnection(holder.connection);
                        closedConnections++;

                    } catch (Exception e) {
                        log.warn("Error auto-closing pool for {}: {}", connectionKey, e.getMessage());
                    }
                }
            }
        }

        if (closedConnections > 0) {
            log.debug("Cleanup completed. Closed {} idle connections", closedConnections);
        }
    }

    private String normalizeService(String service) {
        if (service == null || service.isBlank()) {
            return "legacy";
        }
        return service.trim().toLowerCase();
    }

    /** Close idle LDAP sessions with an LDAP unbind before closing the MINA socket. */
    private void closeConnection(LdapConnection connection) throws Exception {
        if (connection == null) return;
        if (connection.isConnected()) {
            try {
                connection.unBind();
            } catch (Exception exception) {
                log.debug("LDAP unbind failed while closing idle session: {}", exception.getMessage());
            }
        }
        connection.close();
    }

    /** Trust manager used for the legacy LDAPS mode. */
    private static final class NoVerificationTrustManager extends X509ExtendedTrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}
