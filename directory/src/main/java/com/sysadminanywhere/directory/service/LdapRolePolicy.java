package com.sysadminanywhere.directory.service;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LdapRolePolicy {
    private final List<String> configuredAdminGroupDns;
    private final Set<String> adminUsers;
    private final Set<String> wmiReadUsers;

    public LdapRolePolicy(
            @Value("${directory.authorization.admin-group-dns:}") String adminGroupDns,
            @Value("${directory.authorization.admin-users:}") String adminUsers,
            @Value("${directory.authorization.wmi-read-users:}") String wmiReadUsers) {
        this.configuredAdminGroupDns = Arrays.stream(adminGroupDns.split(";"))
                .map(String::trim).filter(value -> !value.isEmpty()).toList();
        this.adminUsers = Arrays.stream(adminUsers.split(","))
                .map(String::trim).filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
        this.wmiReadUsers = Arrays.stream(wmiReadUsers.split(","))
                .map(String::trim).filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }

    public List<String> roles(String username, Collection<String> memberOf, String baseDn) {
        if (isAdmin(username, memberOf, baseDn)) return List.of("ROLE_READER", "ROLE_ADMIN");
        if (username != null && wmiReadUsers.contains(username.toLowerCase(Locale.ROOT)))
            return List.of("ROLE_READER", "ROLE_WMI_READER");
        return List.of("ROLE_READER");
    }

    public boolean isAdmin(String username, Collection<String> memberOf, String baseDn) {
        if (username != null && adminUsers.contains(username.toLowerCase(Locale.ROOT))) return true;
        if (memberOf == null || memberOf.isEmpty()) return false;
        List<String> adminGroups = configuredAdminGroupDns.isEmpty()
                ? List.of("CN=Domain Admins,CN=Users," + baseDn) : configuredAdminGroupDns;
        for (String group : memberOf) {
            for (String adminGroup : adminGroups) {
                if (sameDn(group, adminGroup)) return true;
            }
        }
        return false;
    }

    private boolean sameDn(String left, String right) {
        try {
            return new Dn(left).getNormName().equalsIgnoreCase(new Dn(right).getNormName());
        } catch (LdapException exception) {
            throw new IllegalArgumentException("Invalid LDAP administrator group DN", exception);
        }
    }
}
