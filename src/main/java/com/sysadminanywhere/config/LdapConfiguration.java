package com.sysadminanywhere.config;

import com.sysadminanywhere.domain.DirectorySetting;
import lombok.SneakyThrows;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.ldap.client.api.NoVerificationTrustManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class LdapConfiguration {

    @Value("${ldap.host.server:localhost}")
    private String server;

    @Value("${ldap.host.port:389}")
    private int port;

    @Value("${ldap.host.groups.allowed:}")
    private String groupsAllowed;

    @SneakyThrows
    @Bean
    public LdapConnection createConnection(LdapConnectionConfig sslConfig) {
        LdapConnection connection = new LdapNetworkConnection(sslConfig);
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
    public DirectorySetting directorySetting() {
        DirectorySetting directorySetting = new DirectorySetting();

        if(!groupsAllowed.isEmpty())
            directorySetting.setGroupsAllowed(Arrays.asList(groupsAllowed.split(";")));

        return directorySetting;
    }

}