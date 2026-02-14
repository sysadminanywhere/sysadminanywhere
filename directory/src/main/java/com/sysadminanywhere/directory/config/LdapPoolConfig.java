package com.sysadminanywhere.directory.config;

import org.apache.directory.ldap.client.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LdapPoolConfig {

    @Bean
    public LdapConnectionConfig ldapConnectionConfig(@Value("${ldap.host.server}") String host,
                                                     @Value("${ldap.host.port}") int port,
                                                     @Value("${ldap.host.use.ssl}") boolean useSsl) {

        LdapConnectionConfig config = new LdapConnectionConfig();
        config.setLdapHost(host);
        config.setLdapPort(port);
        config.setUseSsl(useSsl);
        config.setTrustManagers(new NoVerificationTrustManager());
        config.setCloseTimeout(500L); // Даем 500мс на корректное закрытие сессии
        return config;
    }

    @Bean
    public LdapConnectionFactory ldapConnectionFactory(LdapConnectionConfig config) {
        // Это и есть фабрика, которая умеет создавать соединения
        return new DefaultLdapConnectionFactory(config);
    }

    @Bean(destroyMethod = "close")
    public LdapConnectionPool ldapConnectionPool(LdapConnectionFactory factory) {
        // Используем Validating... если хотим, чтобы пул проверял соединения перед выдачей
        ValidatingPoolableLdapConnectionFactory poolFactory =
                new ValidatingPoolableLdapConnectionFactory(factory);

        LdapConnectionPool pool = new LdapConnectionPool(poolFactory);

        // Настройки жизненного цикла соединений
        pool.setTestOnBorrow(true); // Проверка при взятии
        pool.setMaxTotal(100);       // Лимит соединений
        pool.setBlockWhenExhausted(true);

        return pool;
    }

}