package com.sysadminanywhere.directory.service;

import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ConnectionService {

    private final LdapConnectionPool pool;
    private final VaultService vaultService;

    public ConnectionService(LdapConnectionPool ldapConnectionPool, VaultService vaultService) {
        this.pool = ldapConnectionPool;
        this.vaultService = vaultService;
    }

    private LdapConnection getConnection() throws LdapException {
        LdapConnection connection = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String password = vaultService.getPassword(username);
            connection = pool.getConnection();
            connection.bind(createBindRequest(username, password));
        }
        return connection;
    }

    @SneakyThrows
    public Entry getRootDse() {
        LdapConnection connection = null;
        try {
            connection = pool.getConnection();
            connection.bind();
            return connection.getRootDse();
        } catch (LdapException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.unBind();
                pool.releaseConnection(connection);
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
                pool.releaseConnection(connection);
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
                pool.releaseConnection(connection);
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
                pool.releaseConnection(connection);
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
                pool.releaseConnection(connection);
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
                pool.releaseConnection(connection);
            }
        }
    }

    @SneakyThrows
    public boolean authenticate(String username, String password) {
        LdapConnection connection = null;
        try {
            connection = pool.getConnection();
            connection.bind(createBindRequest(username, password));
            vaultService.savePassword(username, password);
            return true;
        } catch (LdapException e) {
            return false;
        } finally {
            if (connection != null) {
                connection.unBind();
                pool.releaseConnection(connection);
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

}