package com.sysadminanywhere.directory.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LdapRolePolicyTest {
    private static final String BASE_DN = "DC=example,DC=com";

    @Test
    void ordinaryAccountHasReadOnlyRole() {
        LdapRolePolicy policy = new LdapRolePolicy("", "", "");
        assertEquals(List.of("ROLE_READER"), policy.roles("alice", List.of(), BASE_DN));
    }

    @Test
    void directDomainAdminMembershipGrantsAdmin() {
        LdapRolePolicy policy = new LdapRolePolicy("", "", "");
        assertEquals(List.of("ROLE_READER", "ROLE_ADMIN"), policy.roles("alice",
                List.of("cn=domain admins,cn=users,dc=EXAMPLE,dc=COM"), BASE_DN));
    }

    @Test
    void matchingNameInDifferentContainerDoesNotGrantAdmin() {
        LdapRolePolicy policy = new LdapRolePolicy("", "", "");
        assertEquals(List.of("ROLE_READER"), policy.roles("alice",
                List.of("CN=Domain Admins,OU=Other," + BASE_DN), BASE_DN));
    }

    @Test
    void configuredGroupAndUserNamesAreExact() {
        LdapRolePolicy policy = new LdapRolePolicy("CN=Operators,OU=Groups," + BASE_DN,
                "DOMAIN\\backup", "");
        assertEquals(List.of("ROLE_READER", "ROLE_ADMIN"), policy.roles("DOMAIN\\BACKUP", List.of(), BASE_DN));
        assertEquals(List.of("ROLE_READER", "ROLE_ADMIN"), policy.roles("alice",
                List.of("CN=Operators,OU=Groups," + BASE_DN), BASE_DN));
        assertEquals(List.of("ROLE_READER"), policy.roles("backup", List.of(), BASE_DN));
    }

    @Test
    void wmiReaderCannotBecomeAdmin() {
        LdapRolePolicy policy = new LdapRolePolicy("", "", "readonly");
        assertEquals(List.of("ROLE_READER", "ROLE_WMI_READER"), policy.roles("READONLY", List.of(), BASE_DN));
    }
}
