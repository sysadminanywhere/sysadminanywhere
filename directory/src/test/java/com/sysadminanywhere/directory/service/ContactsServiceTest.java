package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.ContactEntry;
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
class ContactsServiceTest {

    @Mock
    private LdapService ldapService;

    private ContactsService contactsService;

    @BeforeEach
    void setUp() {
        contactsService = new ContactsService(ldapService);
    }

    @Test
    void getAll_withPageable_returnsPageOfContacts() throws Exception {
        Entry entry = new DefaultEntry("cn=John Doe,ou=Contacts,dc=example,dc=com");
        entry.add("cn", "John Doe");
        entry.add("objectClass", "contact", "person");

        Page<Entry> entryPage = new PageImpl<>(List.of(entry));
        when(ldapService.searchPage(anyString(), any(Sort.class), any(Pageable.class), any(String[].class)))
                .thenReturn(entryPage);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ContactEntry> result = contactsService.getAll(pageable, "", "*");

        assertThat(result).isNotNull();
        verify(ldapService).searchPage(contains("objectClass=contact"), any(Sort.class), eq(pageable), any(String[].class));
    }

    @Test
    void getAll_withFilters_returnsListOfContacts() throws Exception {
        Entry entry = new DefaultEntry("cn=John Doe,ou=Contacts,dc=example,dc=com");
        entry.add("cn", "John Doe");
        entry.add("objectClass", "contact", "person");

        when(ldapService.searchWithAttributes(anyString(), any(String[].class)))
                .thenReturn(List.of(entry));

        List<ContactEntry> result = contactsService.getAll("", "*");

        assertThat(result).isNotNull();
        verify(ldapService).searchWithAttributes(contains("objectClass=contact"), any(String[].class));
    }

    @Test
    void getByCN_returnsContactWhenFound() throws Exception {
        Entry entry = new DefaultEntry("cn=John Doe,ou=Contacts,dc=example,dc=com");
        entry.add("cn", "John Doe");
        entry.add("objectClass", "contact", "person");
        entry.add("distinguishedName", "cn=John Doe,ou=Contacts,dc=example,dc=com");

        when(ldapService.search(anyString())).thenReturn(List.of(entry));

        ContactEntry result = contactsService.getByCN("John Doe");

        assertThat(result).isNotNull();
        assertThat(result.getCn()).isEqualTo("John Doe");
        verify(ldapService).search(contains("cn=John Doe"));
    }

    @Test
    void getByCN_returnsNullWhenNotFound() {
        when(ldapService.search(anyString())).thenReturn(Collections.emptyList());

        ContactEntry result = contactsService.getByCN("NonExistent");

        assertThat(result).isNull();
    }

    @Test
    void add_createsContactWithDefaultContainer() throws Exception {
        when(ldapService.getUsersContainer()).thenReturn("CN=Users,DC=example,DC=com");

        Entry createdEntry = new DefaultEntry("cn=John Doe,CN=Users,DC=example,DC=com");
        createdEntry.add("cn", "John Doe");
        createdEntry.add("objectClass", "contact", "person");
        createdEntry.add("distinguishedName", "cn=John Doe,CN=Users,DC=example,DC=com");
        createdEntry.add("displayName", "John Doe");
        createdEntry.add("givenName", "John");
        createdEntry.add("sn", "Doe");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        ContactEntry result = contactsService.add(null, "John Doe", "John Doe", "John", "Doe", null);

        assertThat(result).isNotNull();
        verify(ldapService).add(any(Entry.class));
        verify(ldapService).getUsersContainer();
    }

    @Test
    void add_createsContactWithSpecifiedContainer() throws Exception {
        Entry createdEntry = new DefaultEntry("cn=John Doe,OU=Contacts,DC=example,DC=com");
        createdEntry.add("cn", "John Doe");
        createdEntry.add("objectClass", "contact", "person");
        createdEntry.add("distinguishedName", "cn=John Doe,OU=Contacts,DC=example,DC=com");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        ContactEntry result = contactsService.add("OU=Contacts,DC=example,DC=com", "John Doe", "John Doe", "John", "Doe", null);

        assertThat(result).isNotNull();
        verify(ldapService).add(any(Entry.class));
        verify(ldapService, never()).getUsersContainer();
    }

    @Test
    void add_setsInitialsWhenProvided() throws Exception {
        when(ldapService.getUsersContainer()).thenReturn("CN=Users,DC=example,DC=com");

        Entry createdEntry = new DefaultEntry("cn=John Doe,CN=Users,DC=example,DC=com");
        createdEntry.add("cn", "John Doe");
        createdEntry.add("objectClass", "contact", "person");
        createdEntry.add("distinguishedName", "cn=John Doe,CN=Users,DC=example,DC=com");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        contactsService.add(null, "John Doe", "John Doe", "John", "Doe", "JD");

        verify(ldapService).updateProperty(anyString(), eq("initials"), eq("JD"));
    }

    @Test
    void update_modifiesContactAndReturnsUpdated() throws Exception {
        Entry existingEntry = new DefaultEntry("cn=John Doe,CN=Users,DC=example,DC=com");
        existingEntry.add("cn", "John Doe");
        existingEntry.add("objectClass", "contact", "person");
        existingEntry.add("distinguishedName", "cn=John Doe,CN=Users,DC=example,DC=com");

        when(ldapService.search(anyString())).thenReturn(List.of(existingEntry));

        ContactEntry contact = new ContactEntry();
        contact.setCn("John Doe");
        contact.setDistinguishedName("cn=John Doe,CN=Users,DC=example,DC=com");

        ContactEntry result = contactsService.update(contact);

        assertThat(result).isNotNull();
        verify(ldapService).update(any(ModifyRequest.class));
    }

    @Test
    void delete_removesContact() throws Exception {
        contactsService.delete("cn=John Doe,CN=Users,DC=example,DC=com");

        verify(ldapService).delete(any(Entry.class));
    }

    @Test
    void getDefaultContainer_returnsUsersContainer() {
        when(ldapService.getUsersContainer()).thenReturn("CN=Users,DC=example,DC=com");

        String result = contactsService.getDefaultContainer();

        assertThat(result).isEqualTo("CN=Users,DC=example,DC=com");
    }

    @Test
    void getLdapService_returnsLdapService() {
        LdapService result = contactsService.getLdapService();

        assertThat(result).isEqualTo(ldapService);
    }
}
