package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.model.Containers;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LdapServiceTest {

    @Mock
    private LdapConnection connection;

    @Mock
    private LdapConnectionConfig ldapConnectionConfig;

    @Mock
    private SearchCursor searchCursor;

    @Mock
    private SearchResultDone searchResultDone;

    @Mock
    private PagedResults pagedResultsControl;

    private LdapService ldapService;

    @BeforeEach
    void setUp() throws Exception {
        Entry rootDse = new DefaultEntry();
        rootDse.add("rootdomainnamingcontext", "DC=example,DC=com");

        when(connection.getRootDse()).thenReturn(rootDse);

        ldapService = new LdapService(connection, ldapConnectionConfig);
    }

    @Test
    void constructor_initializesFieldsCorrectly() throws Exception {
        assertThat(ldapService.getDefaultNamingContext()).isEqualTo("DC=example,DC=com");
        assertThat(ldapService.getDomainName()).isEqualTo("example.com");
        assertThat(ldapService.getBaseDn().getName()).isEqualTo("DC=example,DC=com");
    }

    @Test
    void getDomainEntry_returnsRootDse() throws Exception {
        Entry rootDse = new DefaultEntry();
        rootDse.add("rootdomainnamingcontext", "DC=example,DC=com");
        when(connection.getRootDse()).thenReturn(rootDse);

        Entry result = ldapService.getDomainEntry();

        assertThat(result).isNotNull();
        verify(connection, times(2)).getRootDse();
    }

    @Test
    void convertEntry_convertsEntryToDto() throws Exception {
        Entry entry = new DefaultEntry("cn=test,dc=example,dc=com");
        entry.add("cn", "test");
        entry.add("description", "Test description");
        entry.add("objectClass", "top", "person");

        EntryDto result = ldapService.convertEntry(entry);

        assertThat(result.getDn()).isEqualTo("cn=test,dc=example,dc=com");
        assertThat(result.getAttributes()).containsKey("cn");
        assertThat(result.getAttributes().get("cn")).isEqualTo("test");
        assertThat(result.getAttributes().get("description")).isEqualTo("Test description");
        assertThat(result.getAttributes().get("objectclass")).isInstanceOf(List.class);
    }

    @Test
    void convertEntry_handlesBinaryAttributes() throws Exception {
        Entry entry = new DefaultEntry("cn=test,dc=example,dc=com");
        entry.add("objectSID", new byte[]{1, 2, 3, 4});

        EntryDto result = ldapService.convertEntry(entry);

        assertThat(result.getAttributes().get("objectsid")).isInstanceOf(byte[].class);
    }

    @Test
    void convertEntryList_convertsMultipleEntries() throws Exception {
        Entry entry1 = new DefaultEntry("cn=test1,dc=example,dc=com");
        entry1.add("cn", "test1");

        Entry entry2 = new DefaultEntry("cn=test2,dc=example,dc=com");
        entry2.add("cn", "test2");

        List<EntryDto> result = ldapService.convertEntryList(List.of(entry1, entry2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDn()).isEqualTo("cn=test1,dc=example,dc=com");
        assertThat(result.get(1).getDn()).isEqualTo("cn=test2,dc=example,dc=com");
    }

    @Test
    void search_returnsEmptyListWhenNotConnected() throws Exception {
        when(connection.isConnected()).thenReturn(false);

        List<Entry> result = ldapService.search("(objectClass=*)");

        assertThat(result).isEmpty();
    }

    @Test
    void search_returnsEmptyListWhenNotAuthenticated() throws Exception {
        when(connection.isConnected()).thenReturn(true);
        when(connection.isAuthenticated()).thenReturn(false);

        List<Entry> result = ldapService.search("(objectClass=*)");

        assertThat(result).isEmpty();
    }

    @Test
    void search_returnsEntriesWhenConnectedAndAuthenticated() throws Exception {
        when(connection.isConnected()).thenReturn(true);
        when(connection.isAuthenticated()).thenReturn(true);

        Entry resultEntry = new DefaultEntry("cn=test,dc=example,dc=com");
        resultEntry.add("cn", "test");
        SearchResultEntry searchResultEntry = mock(SearchResultEntry.class);
        when(searchResultEntry.getEntry()).thenReturn(resultEntry);

        when(searchCursor.next()).thenReturn(true, false);
        when(searchCursor.get()).thenReturn(searchResultEntry);
        when(searchCursor.getSearchResultDone()).thenReturn(searchResultDone);
        when(searchResultDone.getControl(PagedResults.OID)).thenReturn(pagedResultsControl);
        when(pagedResultsControl.getCookie()).thenReturn(new byte[0]);

        when(connection.search(any(SearchRequest.class))).thenReturn(searchCursor);

        List<Entry> result = ldapService.search("(objectClass=*)");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDn().getName()).isEqualTo("cn=test,dc=example,dc=com");
    }

    @Test
    void count_returnsZeroWhenNotConnected() throws Exception {
        when(connection.isConnected()).thenReturn(false);

        Long result = ldapService.count("(objectClass=*)");

        assertThat(result).isZero();
    }

    @Test
    void count_returnsCountWhenConnected() throws Exception {
        when(connection.isConnected()).thenReturn(true);
        when(connection.isAuthenticated()).thenReturn(true);

        SearchResultEntry searchResultEntry = mock(SearchResultEntry.class);

        when(searchCursor.next()).thenReturn(true, true, false);
        when(searchCursor.get()).thenReturn(searchResultEntry);

        when(connection.search(any(SearchRequest.class))).thenReturn(searchCursor);

        Long result = ldapService.count("(objectClass=*)");

        assertThat(result).isEqualTo(2);
    }

    @Test
    void add_callsConnectionAdd() throws Exception {
        Entry entry = new DefaultEntry("cn=test,dc=example,dc=com");
        entry.add("cn", "test");

        ldapService.add(entry);

        verify(connection).add(any(AddRequest.class));
    }

    @Test
    void update_callsConnectionModify() throws Exception {
        ModifyRequest modifyRequest = new ModifyRequestImpl();
        modifyRequest.setName(new Dn("cn=test,dc=example,dc=com"));

        ldapService.update(modifyRequest);

        verify(connection).modify(modifyRequest);
    }

    @Test
    void delete_callsConnectionDelete() throws Exception {
        Entry entry = new DefaultEntry("cn=test,dc=example,dc=com");

        ldapService.delete(entry);

        verify(connection).delete(entry.getDn());
    }

    @Test
    void updateProperty_callsConnectionModify() throws Exception {
        ldapService.updateProperty("cn=test,dc=example,dc=com", "description", "New description");

        verify(connection).modify(eq("cn=test,dc=example,dc=com"), any(Modification.class));
    }

    @Test
    void searchPage_returnsEmptyPageWhenNotConnected() throws Exception {
        when(connection.isConnected()).thenReturn(false);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Entry> result = ldapService.searchPage("(objectClass=*)", Sort.unsorted(), pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void searchPage_returnsPagedResultsWhenConnected() throws Exception {
        when(connection.isConnected()).thenReturn(true);
        when(connection.isAuthenticated()).thenReturn(true);

        Entry resultEntry = new DefaultEntry("cn=test,dc=example,dc=com");
        resultEntry.add("cn", "test");
        SearchResultEntry searchResultEntry = mock(SearchResultEntry.class);
        when(searchResultEntry.getEntry()).thenReturn(resultEntry);

        when(searchCursor.next()).thenReturn(true, false);
        when(searchCursor.get()).thenReturn(searchResultEntry);
        when(searchCursor.getSearchResultDone()).thenReturn(searchResultDone);
        when(searchResultDone.getControl(PagedResults.OID)).thenReturn(pagedResultsControl);
        when(pagedResultsControl.getCookie()).thenReturn(new byte[0]);

        when(connection.search(any(SearchRequest.class))).thenReturn(searchCursor);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Entry> result = ldapService.searchPage("(objectClass=*)", Sort.unsorted(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getWellKnownObjects_returnsEmptyListWhenNoResults() throws Exception {
        when(connection.isConnected()).thenReturn(true);
        when(connection.isAuthenticated()).thenReturn(true);

        when(searchCursor.next()).thenReturn(false);
        when(searchCursor.getSearchResultDone()).thenReturn(searchResultDone);
        when(searchResultDone.getControl(PagedResults.OID)).thenReturn(pagedResultsControl);
        when(pagedResultsControl.getCookie()).thenReturn(new byte[0]);

        when(connection.search(any(SearchRequest.class))).thenReturn(searchCursor);

        List<String> result = ldapService.getWellKnownObjects();

        assertThat(result).isEmpty();
    }

    @Test
    void deleteMember_returnsTrueOnSuccess() throws Exception {
        ModifyResponse modifyResponse = mock(ModifyResponse.class);
        when(connection.modify(any(ModifyRequest.class))).thenReturn(modifyResponse);

        boolean result = ldapService.deleteMember("cn=user,dc=example,dc=com", "cn=group,dc=example,dc=com");

        assertThat(result).isTrue();
        verify(connection).modify(any(ModifyRequest.class));
    }

    @Test
    void deleteMember_returnsFalseOnException() throws Exception {
        when(connection.modify(any(ModifyRequest.class))).thenThrow(new RuntimeException("Error"));

        boolean result = ldapService.deleteMember("cn=user,dc=example,dc=com", "cn=group,dc=example,dc=com");

        assertThat(result).isFalse();
    }

    @Test
    void addMember_returnsTrueOnSuccess() throws Exception {
        ModifyResponse modifyResponse = mock(ModifyResponse.class);
        when(connection.modify(any(ModifyRequest.class))).thenReturn(modifyResponse);

        boolean result = ldapService.addMember("cn=user,dc=example,dc=com", "cn=group,dc=example,dc=com");

        assertThat(result).isTrue();
        verify(connection).modify(any(ModifyRequest.class));
    }

    @Test
    void addMember_returnsFalseOnException() throws Exception {
        when(connection.modify(any(ModifyRequest.class))).thenThrow(new RuntimeException("Error"));

        boolean result = ldapService.addMember("cn=user,dc=example,dc=com", "cn=group,dc=example,dc=com");

        assertThat(result).isFalse();
    }

    @Test
    void getContainers_returnsContainersWhenConnected() throws Exception {
        when(connection.isConnected()).thenReturn(true);
        when(connection.isAuthenticated()).thenReturn(true);

        when(searchCursor.next()).thenReturn(false);
        when(searchCursor.getSearchResultDone()).thenReturn(searchResultDone);
        when(searchResultDone.getControl(PagedResults.OID)).thenReturn(pagedResultsControl);
        when(pagedResultsControl.getCookie()).thenReturn(new byte[0]);

        when(connection.search(any(SearchRequest.class))).thenReturn(searchCursor);

        Containers result = ldapService.getContainers();

        assertThat(result).isNotNull();
        assertThat(result.getContainers()).isEmpty();
    }

    @Test
    void getAudit_returnsEmptyPageWhenNoResults() throws Exception {
        when(connection.isConnected()).thenReturn(true);
        when(connection.isAuthenticated()).thenReturn(true);

        when(searchCursor.next()).thenReturn(false);
        when(searchCursor.getSearchResultDone()).thenReturn(searchResultDone);
        when(searchResultDone.getControl(PagedResults.OID)).thenReturn(pagedResultsControl);
        when(pagedResultsControl.getCookie()).thenReturn(new byte[0]);

        when(connection.search(any(SearchRequest.class))).thenReturn(searchCursor);

        Pageable pageable = PageRequest.of(0, 10);
        Map<String, String> filters = new HashMap<>();

        Page<AuditDto> result = ldapService.getAudit(pageable, filters);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void getAudit_returnsPagedAuditResults() throws Exception {
        when(connection.isConnected()).thenReturn(true);
        when(connection.isAuthenticated()).thenReturn(true);

        Entry auditEntry = new DefaultEntry("cn=test,dc=example,dc=com");
        auditEntry.add("name", "test");
        auditEntry.add("whencreated", "20240101010101.0Z");
        auditEntry.add("whenchanged", "20240102020202.0Z");

        SearchResultEntry searchResultEntry = mock(SearchResultEntry.class);
        when(searchResultEntry.getEntry()).thenReturn(auditEntry);

        when(searchCursor.next()).thenReturn(true, false);
        when(searchCursor.get()).thenReturn(searchResultEntry);
        when(searchCursor.getSearchResultDone()).thenReturn(searchResultDone);
        when(searchResultDone.getControl(PagedResults.OID)).thenReturn(pagedResultsControl);
        when(pagedResultsControl.getCookie()).thenReturn(new byte[0]);

        when(connection.search(any(SearchRequest.class))).thenReturn(searchCursor);

        Pageable pageable = PageRequest.of(0, 10);
        Map<String, String> filters = new HashMap<>();
        filters.put("startDate", "2024-01-01");
        filters.put("endDate", "2024-01-31");

        Page<AuditDto> result = ldapService.getAudit(pageable, filters);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("test");
        assertThat(result.getContent().get(0).getAction()).isEqualTo("Changed");
    }

    @Test
    void search_withSortParameter_appliesSorting() throws Exception {
        when(connection.isConnected()).thenReturn(true);
        when(connection.isAuthenticated()).thenReturn(true);

        when(searchCursor.next()).thenReturn(false);
        when(searchCursor.getSearchResultDone()).thenReturn(searchResultDone);
        when(searchResultDone.getControl(PagedResults.OID)).thenReturn(pagedResultsControl);
        when(pagedResultsControl.getCookie()).thenReturn(new byte[0]);

        when(connection.search(any(SearchRequest.class))).thenReturn(searchCursor);

        Sort sort = Sort.by(Sort.Direction.ASC, "sn");
        List<Entry> result = ldapService.search(ldapService.getBaseDn(), "(objectClass=*)", SearchScope.SUBTREE, sort);

        assertThat(result).isEmpty();
        verify(connection).search(any(SearchRequest.class));
    }

    @Test
    void searchWithAttributes_callsSearchWithSpecifiedAttributes() throws Exception {
        when(connection.isConnected()).thenReturn(true);
        when(connection.isAuthenticated()).thenReturn(true);

        when(searchCursor.next()).thenReturn(false);
        when(searchCursor.getSearchResultDone()).thenReturn(searchResultDone);
        when(searchResultDone.getControl(PagedResults.OID)).thenReturn(pagedResultsControl);
        when(pagedResultsControl.getCookie()).thenReturn(new byte[0]);

        when(connection.search(any(SearchRequest.class))).thenReturn(searchCursor);

        List<Entry> result = ldapService.searchWithAttributes("(objectClass=*)", "cn", "sn", "mail");

        assertThat(result).isEmpty();
        verify(connection).search(any(SearchRequest.class));
    }

    @Test
    void getAudit_handlesOffsetBeyondListSize() throws Exception {
        when(connection.isConnected()).thenReturn(true);
        when(connection.isAuthenticated()).thenReturn(true);

        Entry auditEntry = new DefaultEntry("cn=test,dc=example,dc=com");
        auditEntry.add("name", "test");
        auditEntry.add("whencreated", "20240101010101.0Z");
        auditEntry.add("whenchanged", "20240102020202.0Z");

        SearchResultEntry searchResultEntry = mock(SearchResultEntry.class);
        when(searchResultEntry.getEntry()).thenReturn(auditEntry);

        when(searchCursor.next()).thenReturn(true, false);
        when(searchCursor.get()).thenReturn(searchResultEntry);
        when(searchCursor.getSearchResultDone()).thenReturn(searchResultDone);
        when(searchResultDone.getControl(PagedResults.OID)).thenReturn(pagedResultsControl);
        when(pagedResultsControl.getCookie()).thenReturn(new byte[0]);

        when(connection.search(any(SearchRequest.class))).thenReturn(searchCursor);

        Pageable pageable = PageRequest.of(10, 10);
        Map<String, String> filters = new HashMap<>();
        filters.put("startDate", "2024-01-01");
        filters.put("endDate", "2024-01-31");

        Page<AuditDto> result = ldapService.getAudit(pageable, filters);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
