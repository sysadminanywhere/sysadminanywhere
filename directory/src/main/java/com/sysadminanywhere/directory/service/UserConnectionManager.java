package com.sysadminanywhere.directory.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.constants.SupportedSaslMechanisms;
import org.apache.directory.ldap.client.api.*;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserConnectionManager {
    // Храним пулы: UserDN -> LdapConnectionPool
    private final Map<String, LdapConnectionPool> userPools = new ConcurrentHashMap<>();

    // Базовые настройки (хост, порт, ssl) из вашего основного конфига
    private final LdapConnectionConfig baseConfig;

    public UserConnectionManager(LdapConnectionConfig baseConfig) {
        this.baseConfig = baseConfig;
    }

    public LdapConnection getConnection(String username, String password) throws Exception {
        LdapConnectionPool pool = userPools.computeIfAbsent(username, dn -> {
            try {
                // 1. Создаем персональный конфиг для пользователя
                LdapConnectionConfig userConfig = createSpecificConfig(username, password);

                // 2. Создаем фабрику на базе этого конфига
                DefaultLdapConnectionFactory factory = new DefaultLdapConnectionFactory(userConfig);

                // 3. Создаем пул с валидацией
                ValidatingPoolableLdapConnectionFactory poolFactory =
                        new ValidatingPoolableLdapConnectionFactory(factory);

                LdapConnectionPool newPool = new LdapConnectionPool(poolFactory);
                newPool.setMaxTotal(5);
                newPool.setTestOnBorrow(true);
                return newPool;
            } catch (Exception e) {
                throw new RuntimeException("Could not create pool for user: " + dn, e);
            }
        });

        return pool.getConnection();
    }

    private LdapConnectionConfig createSpecificConfig(String username, String password) {
        LdapConnectionConfig config = new LdapConnectionConfig();
        config.setLdapHost(baseConfig.getLdapHost());
        config.setLdapPort(baseConfig.getLdapPort());
        config.setUseSsl(baseConfig.isUseSsl());
        config.setTrustManagers(baseConfig.getTrustManagers());

        // ВОТ ЗДЕСЬ УСТАНАВЛИВАЕМ КРЕДЕНШЛЫ ДЛЯ ПУЛА
        config.setCredentials(password);
        config.setName(username);

        // Важно для SSL: даем время на закрытие, чтобы не было WARN
        config.setCloseTimeout(500L);

        return config;
    }

}