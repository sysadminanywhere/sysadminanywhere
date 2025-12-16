package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.UserAccountControls;
import com.sysadminanywhere.common.directory.model.UserEntry;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private LdapService ldapService;

    private UsersService usersService;

    @BeforeEach
    void setUp() {
        usersService = new UsersService(ldapService);
    }

    @Test
    void getAll_withPageable_returnsPageOfUsers() throws Exception {
        Entry entry = new DefaultEntry("cn=jdoe,ou=Users,dc=example,dc=com");
        entry.add("cn", "jdoe");
        entry.add("objectClass", "user", "person");

        Page<Entry> entryPage = new PageImpl<>(List.of(entry));
        when(ldapService.searchPage(anyString(), any(Sort.class), any(Pageable.class), any(String[].class)))
                .thenReturn(entryPage);

        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntry> result = usersService.getAll(pageable, "", "*");

        assertThat(result).isNotNull();
        verify(ldapService).searchPage(contains("objectClass=user"), any(Sort.class), eq(pageable), any(String[].class));
    }

    @Test
    void getAll_withFilters_returnsListOfUsers() throws Exception {
        Entry entry = new DefaultEntry("cn=jdoe,ou=Users,dc=example,dc=com");
        entry.add("cn", "jdoe");
        entry.add("objectClass", "user", "person");

        when(ldapService.searchWithAttributes(anyString(), any(String[].class)))
                .thenReturn(List.of(entry));

        List<UserEntry> result = usersService.getAll("", "*");

        assertThat(result).isNotNull();
        verify(ldapService).searchWithAttributes(contains("objectClass=user"), any(String[].class));
    }

    @Test
    void getByCN_returnsUserWhenFound() throws Exception {
        Entry entry = new DefaultEntry("cn=jdoe,ou=Users,dc=example,dc=com");
        entry.add("cn", "jdoe");
        entry.add("objectClass", "user", "person");
        entry.add("distinguishedName", "cn=jdoe,ou=Users,dc=example,dc=com");

        when(ldapService.search(anyString())).thenReturn(List.of(entry));

        UserEntry result = usersService.getByCN("jdoe");

        assertThat(result).isNotNull();
        assertThat(result.getCn()).isEqualTo("jdoe");
        verify(ldapService).search(contains("cn=jdoe"));
    }

    @Test
    void getByCN_returnsNullWhenNotFound() {
        when(ldapService.search(anyString())).thenReturn(Collections.emptyList());

        UserEntry result = usersService.getByCN("NonExistent");

        assertThat(result).isNull();
    }

    @Test
    void add_createsUserWithDefaultContainer() throws Exception {
        when(ldapService.getUsersContainer()).thenReturn("CN=Users,DC=example,DC=com");
        when(ldapService.getDomainName()).thenReturn("example.com");

        Entry createdEntry = new DefaultEntry("cn=jdoe,CN=Users,DC=example,DC=com");
        createdEntry.add("cn", "jdoe");
        createdEntry.add("objectClass", "user", "person");
        createdEntry.add("distinguishedName", "cn=jdoe,CN=Users,DC=example,DC=com");
        createdEntry.add("userAccountControl", "512");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        UserEntry result = usersService.add(null, "jdoe", "John Doe", "John", "Doe", null, "password123", false, false, false, false);

        assertThat(result).isNotNull();
        verify(ldapService).add(any(Entry.class));
        verify(ldapService).getUsersContainer();
    }

    @Test
    void add_createsUserWithSpecifiedContainer() throws Exception {
        when(ldapService.getDomainName()).thenReturn("example.com");

        Entry createdEntry = new DefaultEntry("cn=jdoe,OU=Staff,DC=example,DC=com");
        createdEntry.add("cn", "jdoe");
        createdEntry.add("objectClass", "user", "person");
        createdEntry.add("distinguishedName", "cn=jdoe,OU=Staff,DC=example,DC=com");
        createdEntry.add("userAccountControl", "512");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        UserEntry result = usersService.add("OU=Staff,DC=example,DC=com", "jdoe", "John Doe", "John", "Doe", null, "password123", false, false, false, false);

        assertThat(result).isNotNull();
        verify(ldapService).add(any(Entry.class));
        verify(ldapService, never()).getUsersContainer();
    }

    @Test
    void add_setsInitialsWhenProvided() throws Exception {
        when(ldapService.getUsersContainer()).thenReturn("CN=Users,DC=example,DC=com");
        when(ldapService.getDomainName()).thenReturn("example.com");

        Entry createdEntry = new DefaultEntry("cn=jdoe,CN=Users,DC=example,DC=com");
        createdEntry.add("cn", "jdoe");
        createdEntry.add("objectClass", "user", "person");
        createdEntry.add("distinguishedName", "cn=jdoe,CN=Users,DC=example,DC=com");
        createdEntry.add("userAccountControl", "512");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        usersService.add(null, "jdoe", "John Doe", "John", "Doe", "JD", "password123", false, false, false, false);

        verify(ldapService).updateProperty(anyString(), eq("initials"), eq("JD"));
    }

    @Test
    void changeUserAccountControl_setsCannotChangePassword() throws Exception {
        UserEntry user = new UserEntry();
        user.setDistinguishedName("cn=jdoe,CN=Users,DC=example,DC=com");
        user.setUserAccountControl(512);

        usersService.changeUserAccountControl(user, true, false, false, false);

        verify(ldapService).updateProperty(eq("cn=jdoe,CN=Users,DC=example,DC=com"), eq("userAccountControl"), anyString());
    }

    @Test
    void changeUserAccountControl_setsPasswordNeverExpires() throws Exception {
        UserEntry user = new UserEntry();
        user.setDistinguishedName("cn=jdoe,CN=Users,DC=example,DC=com");
        user.setUserAccountControl(512);

        usersService.changeUserAccountControl(user, false, true, false, false);

        verify(ldapService).updateProperty(eq("cn=jdoe,CN=Users,DC=example,DC=com"), eq("userAccountControl"), anyString());
    }

    @Test
    void changeUserAccountControl_setsAccountDisabled() throws Exception {
        UserEntry user = new UserEntry();
        user.setDistinguishedName("cn=jdoe,CN=Users,DC=example,DC=com");
        user.setUserAccountControl(512);

        usersService.changeUserAccountControl(user, false, false, true, false);

        verify(ldapService).updateProperty(eq("cn=jdoe,CN=Users,DC=example,DC=com"), eq("userAccountControl"), anyString());
    }

    @Test
    void changeUserAccountControl_setsMustChangePassword() throws Exception {
        UserEntry user = new UserEntry();
        user.setDistinguishedName("cn=jdoe,CN=Users,DC=example,DC=com");
        user.setUserAccountControl(512);

        usersService.changeUserAccountControl(user, false, false, false, true);

        verify(ldapService).updateProperty(eq("cn=jdoe,CN=Users,DC=example,DC=com"), eq("userAccountControl"), anyString());
        verify(ldapService).updateProperty(eq("cn=jdoe,CN=Users,DC=example,DC=com"), eq("pwdlastset"), eq("0"));
    }

    @Test
    void update_modifiesUserAndReturnsUpdated() throws Exception {
        Entry existingEntry = new DefaultEntry("cn=jdoe,CN=Users,DC=example,DC=com");
        existingEntry.add("cn", "jdoe");
        existingEntry.add("objectClass", "user", "person");
        existingEntry.add("distinguishedName", "cn=jdoe,CN=Users,DC=example,DC=com");

        when(ldapService.search(anyString())).thenReturn(List.of(existingEntry));

        UserEntry user = new UserEntry();
        user.setCn("jdoe");
        user.setDistinguishedName("cn=jdoe,CN=Users,DC=example,DC=com");

        UserEntry result = usersService.update(user);

        assertThat(result).isNotNull();
        verify(ldapService).update(any(ModifyRequest.class));
    }

    @Test
    void delete_removesUser() throws Exception {
        usersService.delete("cn=jdoe,CN=Users,DC=example,DC=com");

        verify(ldapService).delete(any(Entry.class));
    }

    @Test
    void getUserControl_returnsUserAccountControl() {
        UserAccountControls result = usersService.getUserControl(512);

        assertThat(result).isNotNull();
    }

    @Test
    void getDefaultContainer_returnsUsersContainer() {
        when(ldapService.getUsersContainer()).thenReturn("CN=Users,DC=example,DC=com");

        String result = usersService.getDefaultContainer();

        assertThat(result).isEqualTo("CN=Users,DC=example,DC=com");
    }

    @Test
    void resetPassword_updatesPasswordAndPwdLastSet() {
        usersService.resetPassword("cn=jdoe,CN=Users,DC=example,DC=com", "newPassword123");

        verify(ldapService).updateProperty("cn=jdoe,CN=Users,DC=example,DC=com", "userPassword", "newPassword123");
        verify(ldapService).updateProperty("cn=jdoe,CN=Users,DC=example,DC=com", "pwdLastSet", "0");
    }

    @Test
    void getLdapService_returnsLdapService() {
        LdapService result = usersService.getLdapService();

        assertThat(result).isEqualTo(ldapService);
    }
}
