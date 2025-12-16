package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.GroupScope;
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
class GroupsServiceTest {

    @Mock
    private LdapService ldapService;

    private GroupsService groupsService;

    @BeforeEach
    void setUp() {
        groupsService = new GroupsService(ldapService);
    }

    @Test
    void getAll_withPageable_returnsPageOfGroups() throws Exception {
        Entry entry = new DefaultEntry("cn=Admins,ou=Groups,dc=example,dc=com");
        entry.add("cn", "Admins");
        entry.add("objectClass", "group");

        Page<Entry> entryPage = new PageImpl<>(List.of(entry));
        when(ldapService.searchPage(anyString(), any(Sort.class), any(Pageable.class), any(String[].class)))
                .thenReturn(entryPage);

        Pageable pageable = PageRequest.of(0, 10);
        Page<GroupEntry> result = groupsService.getAll(pageable, "", "*");

        assertThat(result).isNotNull();
        verify(ldapService).searchPage(contains("objectClass=group"), any(Sort.class), eq(pageable), any(String[].class));
    }

    @Test
    void getAll_withFilters_returnsListOfGroups() throws Exception {
        Entry entry = new DefaultEntry("cn=Admins,ou=Groups,dc=example,dc=com");
        entry.add("cn", "Admins");
        entry.add("objectClass", "group");

        when(ldapService.searchWithAttributes(anyString(), any(String[].class)))
                .thenReturn(List.of(entry));

        List<GroupEntry> result = groupsService.getAll("", "*");

        assertThat(result).isNotNull();
        verify(ldapService).searchWithAttributes(contains("objectClass=group"), any(String[].class));
    }

    @Test
    void getByCN_returnsGroupWhenFound() throws Exception {
        Entry entry = new DefaultEntry("cn=Admins,ou=Groups,dc=example,dc=com");
        entry.add("cn", "Admins");
        entry.add("objectClass", "group");
        entry.add("distinguishedName", "cn=Admins,ou=Groups,dc=example,dc=com");

        when(ldapService.search(anyString())).thenReturn(List.of(entry));

        GroupEntry result = groupsService.getByCN("Admins");

        assertThat(result).isNotNull();
        assertThat(result.getCn()).isEqualTo("Admins");
        verify(ldapService).search(contains("cn=Admins"));
    }

    @Test
    void getByCN_returnsNullWhenNotFound() {
        when(ldapService.search(anyString())).thenReturn(Collections.emptyList());

        GroupEntry result = groupsService.getByCN("NonExistent");

        assertThat(result).isNull();
    }

    @Test
    void add_createsGroupWithDefaultContainer() throws Exception {
        when(ldapService.getUsersContainer()).thenReturn("CN=Users,DC=example,DC=com");

        Entry createdEntry = new DefaultEntry("cn=TestGroup,CN=Users,DC=example,DC=com");
        createdEntry.add("cn", "TestGroup");
        createdEntry.add("objectClass", "group");
        createdEntry.add("distinguishedName", "cn=TestGroup,CN=Users,DC=example,DC=com");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        GroupEntry result = groupsService.add(null, "TestGroup", null, GroupScope.Global, true);

        assertThat(result).isNotNull();
        verify(ldapService).add(any(Entry.class));
        verify(ldapService).getUsersContainer();
    }

    @Test
    void add_createsGroupWithSpecifiedContainer() throws Exception {
        Entry createdEntry = new DefaultEntry("cn=TestGroup,OU=Groups,DC=example,DC=com");
        createdEntry.add("cn", "TestGroup");
        createdEntry.add("objectClass", "group");
        createdEntry.add("distinguishedName", "cn=TestGroup,OU=Groups,DC=example,DC=com");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        GroupEntry result = groupsService.add("OU=Groups,DC=example,DC=com", "TestGroup", null, GroupScope.Global, true);

        assertThat(result).isNotNull();
        verify(ldapService).add(any(Entry.class));
        verify(ldapService, never()).getUsersContainer();
    }

    @Test
    void add_setsDescriptionWhenProvided() throws Exception {
        when(ldapService.getUsersContainer()).thenReturn("CN=Users,DC=example,DC=com");

        Entry createdEntry = new DefaultEntry("cn=TestGroup,CN=Users,DC=example,DC=com");
        createdEntry.add("cn", "TestGroup");
        createdEntry.add("objectClass", "group");
        createdEntry.add("distinguishedName", "cn=TestGroup,CN=Users,DC=example,DC=com");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        groupsService.add(null, "TestGroup", "Test description", GroupScope.Global, true);

        verify(ldapService).updateProperty(anyString(), eq("description"), eq("Test description"));
    }

    @Test
    void update_modifiesGroupAndReturnsUpdated() throws Exception {
        Entry existingEntry = new DefaultEntry("cn=TestGroup,CN=Users,DC=example,DC=com");
        existingEntry.add("cn", "TestGroup");
        existingEntry.add("objectClass", "group");
        existingEntry.add("distinguishedName", "cn=TestGroup,CN=Users,DC=example,DC=com");

        when(ldapService.search(anyString())).thenReturn(List.of(existingEntry));

        GroupEntry group = new GroupEntry();
        group.setCn("TestGroup");
        group.setDistinguishedName("cn=TestGroup,CN=Users,DC=example,DC=com");

        GroupEntry result = groupsService.update(group);

        assertThat(result).isNotNull();
        verify(ldapService).update(any(ModifyRequest.class));
    }

    @Test
    void delete_removesGroup() throws Exception {
        groupsService.delete("cn=TestGroup,CN=Users,DC=example,DC=com");

        verify(ldapService).delete(any(Entry.class));
    }

    @Test
    void getGroupTypeName_returnsCorrectNameForGlobalDistribution() {
        String result = groupsService.getGroupTypeName(2L);
        assertThat(result).isEqualTo("Global distribution group");
    }

    @Test
    void getGroupTypeName_returnsCorrectNameForDomainLocalDistribution() {
        String result = groupsService.getGroupTypeName(4L);
        assertThat(result).isEqualTo("Domain local distribution group");
    }

    @Test
    void getGroupTypeName_returnsCorrectNameForUniversalDistribution() {
        String result = groupsService.getGroupTypeName(8L);
        assertThat(result).isEqualTo("Universal distribution group");
    }

    @Test
    void getGroupTypeName_returnsCorrectNameForGlobalSecurity() {
        String result = groupsService.getGroupTypeName(-2147483646L);
        assertThat(result).isEqualTo("Global security group");
    }

    @Test
    void getGroupTypeName_returnsCorrectNameForDomainLocalSecurity() {
        String result = groupsService.getGroupTypeName(-2147483644L);
        assertThat(result).isEqualTo("Domain local security group");
    }

    @Test
    void getGroupTypeName_returnsCorrectNameForUniversalSecurity() {
        String result = groupsService.getGroupTypeName(-2147483640L);
        assertThat(result).isEqualTo("Universal security group");
    }

    @Test
    void getGroupTypeName_returnsCorrectNameForBuiltIn() {
        String result = groupsService.getGroupTypeName(-2147483643L);
        assertThat(result).isEqualTo("BuiltIn Group");
    }

    @Test
    void getGroupTypeName_returnsEmptyForUnknown() {
        String result = groupsService.getGroupTypeName(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void getGroupType_returnsCorrectValueForGlobalSecurity() {
        long result = groupsService.getGroupType(GroupScope.Global, true);
        assertThat(result).isEqualTo(-2147483646L);
    }

    @Test
    void getGroupType_returnsCorrectValueForLocalSecurity() {
        long result = groupsService.getGroupType(GroupScope.Local, true);
        assertThat(result).isEqualTo(-2147483644L);
    }

    @Test
    void getGroupType_returnsCorrectValueForUniversalSecurity() {
        long result = groupsService.getGroupType(GroupScope.Universal, true);
        assertThat(result).isEqualTo(-2147483640L);
    }

    @Test
    void getGroupType_returnsCorrectValueForGlobalDistribution() {
        long result = groupsService.getGroupType(GroupScope.Global, false);
        assertThat(result).isEqualTo(2L);
    }

    @Test
    void getGroupType_returnsCorrectValueForLocalDistribution() {
        long result = groupsService.getGroupType(GroupScope.Local, false);
        assertThat(result).isEqualTo(4L);
    }

    @Test
    void getGroupType_returnsCorrectValueForUniversalDistribution() {
        long result = groupsService.getGroupType(GroupScope.Universal, false);
        assertThat(result).isEqualTo(8L);
    }

    @Test
    void getDefaultContainer_returnsUsersContainer() {
        when(ldapService.getUsersContainer()).thenReturn("CN=Users,DC=example,DC=com");

        String result = groupsService.getDefaultContainer();

        assertThat(result).isEqualTo("CN=Users,DC=example,DC=com");
    }

    @Test
    void getLdapService_returnsLdapService() {
        LdapService result = groupsService.getLdapService();

        assertThat(result).isEqualTo(ldapService);
    }
}
