package com.sysadminanywhere.directory.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.standalone.StandaloneLdapApiService;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.ldap.client.api.*;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Arrays;

@Slf4j
@Configuration
public class LdapConfig {

    @Value("${ldap.host.server:localhost}")
    private String server;

    @Value("${ldap.host.port:389}")
    private int port;

    @Value("${ldap.host.use.ssl:false}")
    private boolean useSsl;

    @Bean
    public LdapConnectionConfig sslConfig() {
        LdapConnectionConfig sslConfig = new LdapConnectionConfig();
        sslConfig.setLdapHost(server);
        sslConfig.setUseSsl(useSsl);
        sslConfig.setLdapPort(port);
        sslConfig.setTrustManagers(new NoVerificationTrustManager());
        return sslConfig;
    }

}