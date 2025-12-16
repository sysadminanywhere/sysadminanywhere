package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.directory.service.LdapService;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LdapControllerTest {

    @Mock
    private LdapService ldapService;

    @InjectMocks
    private LdapController ldapController;

    @Test
    void getAudit_returnsPageOfAudit() {
        AuditDto auditDto = createAuditDto("ADD");
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, String> filters = Map.of("user", "admin");
        Page<AuditDto> page = new PageImpl<>(List.of(auditDto));
        when(ldapService.getAudit(pageable, filters)).thenReturn(page);

        var response = ldapController.getAudit(pageable, filters);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(page);
        verify(ldapService).getAudit(pageable, filters);
    }

    @Test
    void getAuditList_returnsListOfAudit() {
        AuditDto auditDto = createAuditDto("ADD");
        Map<String, String> filters = Map.of("action", "ADD");
        when(ldapService.getAuditList(filters)).thenReturn(List.of(auditDto));

        var response = ldapController.getAudit(filters);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsExactly(auditDto);
        verify(ldapService).getAuditList(filters);
    }

    @Test
    void search_returnsListOfEntries() throws Exception {
        SearchDto searchDto = new SearchDto();
        searchDto.setDistinguishedName("DC=example,DC=com");
        searchDto.setFilter("(objectClass=*)");
        searchDto.setSearchScope(SearchScope.SUBTREE.getScope());
        searchDto.setAttributes(new String[]{"cn"});

        Entry entry = new DefaultEntry("cn=test,DC=example,DC=com");
        entry.add("cn", "test");

        EntryDto entryDto = new EntryDto();
        entryDto.setDn("cn=test,DC=example,DC=com");
        entryDto.setAttributes(Map.of("cn", "test"));

        when(ldapService.searchWithAttributes(any(Dn.class), anyString(), any(SearchScope.class), any(String[].class)))
                .thenReturn(List.of(entry));
        when(ldapService.convertEntryList(anyList())).thenReturn(List.of(entryDto));

        var response = ldapController.search(searchDto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsExactly(entryDto);

        ArgumentCaptor<Dn> dnCaptor = ArgumentCaptor.forClass(Dn.class);
        ArgumentCaptor<String> filterCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SearchScope> scopeCaptor = ArgumentCaptor.forClass(SearchScope.class);
        ArgumentCaptor<String[]> attrsCaptor = ArgumentCaptor.forClass(String[].class);

        verify(ldapService).searchWithAttributes(dnCaptor.capture(), filterCaptor.capture(), scopeCaptor.capture(), attrsCaptor.capture());
        assertThat(dnCaptor.getValue()).isEqualTo(new Dn("DC=example,DC=com"));
        assertThat(filterCaptor.getValue()).isEqualTo("(objectClass=*)");
        assertThat(scopeCaptor.getValue()).isEqualTo(SearchScope.SUBTREE);
        assertThat(attrsCaptor.getValue()).containsExactly("cn");
        verify(ldapService).convertEntryList(anyList());
    }

    @Test
    void count_returnsCount() throws Exception {
        SearchDto searchDto = new SearchDto();
        searchDto.setDistinguishedName("DC=example,DC=com");
        searchDto.setFilter("(objectClass=user)");
        searchDto.setSearchScope(SearchScope.ONELEVEL.getScope());

        when(ldapService.count(any(Dn.class), anyString(), any(SearchScope.class))).thenReturn(42L);

        var response = ldapController.count(searchDto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(42L);
        verify(ldapService).count(any(Dn.class), eq("(objectClass=user)"), eq(SearchScope.ONELEVEL));
    }

    @Test
    void getRootDse_returnsDomainEntry() throws Exception {
        Entry domainEntry = new DefaultEntry("DC=example,DC=com");
        domainEntry.add("dc", "example");

        EntryDto entryDto = new EntryDto();
        entryDto.setDn("DC=example,DC=com");
        entryDto.setAttributes(Map.of("dc", "example"));

        when(ldapService.getDomainEntry()).thenReturn(domainEntry);
        when(ldapService.convertEntry(domainEntry)).thenReturn(entryDto);

        var response = ldapController.getRootDse();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(entryDto);
        verify(ldapService).getDomainEntry();
        verify(ldapService).convertEntry(domainEntry);
    }

    @Test
    void addMember_addsMemberToGroup() {
        when(ldapService.addMember(anyString(), anyString())).thenReturn(true);

        var response = ldapController.addMember("cn=jdoe,CN=Users,DC=example,DC=com", "cn=Admins,CN=Users,DC=example,DC=com");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isTrue();
        verify(ldapService).addMember("cn=jdoe,CN=Users,DC=example,DC=com", "cn=Admins,CN=Users,DC=example,DC=com");
    }

    @Test
    void deleteMember_removesMemberFromGroup() {
        when(ldapService.deleteMember(anyString(), anyString())).thenReturn(true);

        var response = ldapController.deleteMember("cn=jdoe,CN=Users,DC=example,DC=com", "cn=Admins,CN=Users,DC=example,DC=com");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isTrue();
        verify(ldapService).deleteMember("cn=jdoe,CN=Users,DC=example,DC=com", "cn=Admins,CN=Users,DC=example,DC=com");
    }

    private AuditDto createAuditDto(String action) {
        AuditDto auditDto = new AuditDto();
        auditDto.setAction(action);
        auditDto.setDistinguishedName("cn=test,DC=example,DC=com");
        return auditDto;
    }
}
