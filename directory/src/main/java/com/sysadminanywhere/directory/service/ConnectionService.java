package com.sysadminanywhere.directory.service;

import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ConnectionService {

    @Value("${ldap.host.server:localhost}")
    private String server;

    @Value("${ldap.host.port:389}")
    private int port;

    @Value("${ldap.host.use.ssl:false}")
    private boolean useSsl;

    private final VaultService vaultService;

    public ConnectionService(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    private LdapConnection getConnection() throws LdapException {
        LdapConnection connection = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String password = vaultService.getPassword(username);
            connection = new LdapNetworkConnection(ldapConfig());
            connection.bind(createBindRequest(username, password));
        }
        return connection;
    }

    @SneakyThrows
    public Entry getRootDse() {
        LdapConnection connection = null;
        try {
            connection = new LdapNetworkConnection(ldapConfig());
            connection.bind();
            return connection.getRootDse();
        } catch (LdapException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.unBind();
                connection.close();
            }
        }
    }

    @SneakyThrows
    public SearchCursor search(SearchRequest countRequest) {
        LdapConnection connection = null;
        try {
            connection = getConnection();
            return connection.search(countRequest);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.unBind();
                connection.close();
            }
        }
    }

    @SneakyThrows
    public void add(AddRequest addRequest) {
        LdapConnection connection = null;
        try {
            connection = getConnection();
            connection.add(addRequest);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.unBind();
                connection.close();
            }
        }
    }

    @SneakyThrows
    public ModifyResponse modify(ModifyRequest modifyRequest) {
        LdapConnection connection = null;
        try {
            connection = getConnection();
            return connection.modify(modifyRequest);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.unBind();
                connection.close();
            }
        }
    }

    @SneakyThrows
    public void modify(String dn, Modification modification) {
        LdapConnection connection = null;
        try {
            connection = getConnection();
            connection.modify(dn, modification);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.unBind();
                connection.close();
            }
        }
    }

    @SneakyThrows
    public void delete(Dn dn) {
        LdapConnection connection = null;
        try {
            connection = getConnection();
            connection.delete(dn);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.unBind();
                connection.close();
            }
        }
    }

    @SneakyThrows
    public boolean authenticate(String username, String password) {
        LdapConnection connection = null;
        try {
            connection = new LdapNetworkConnection(ldapConfig());
            connection.bind(createBindRequest(username, password));
            vaultService.savePassword(username, password);
            return true;
        } catch (LdapException e) {
            return false;
        } finally {
            if (connection != null) {
                connection.unBind();
                connection.close();
            }
        }
    }

    private BindRequest createBindRequest(String username, String password) {
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setName(username);
        bindRequest.setCredentials(password);
        bindRequest.setSimple(true);
        return bindRequest;
    }

    private LdapConnectionConfig ldapConfig() {
        LdapConnectionConfig config = new LdapConnectionConfig();
        config.setLdapHost(server);
        config.setUseSsl(useSsl);
        config.setLdapPort(port);
        config.setTrustManagers(new NoVerificationTrustManager());
        return config;
    }

}