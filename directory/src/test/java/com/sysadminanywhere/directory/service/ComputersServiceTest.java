package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.directory.model.UserAccountControls;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.AddRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.name.Dn;
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
class ComputersServiceTest {

    @Mock
    private LdapService ldapService;

    private ComputersService computersService;

    @BeforeEach
    void setUp() throws Exception {
        computersService = new ComputersService(ldapService);
    }

    @Test
    void getAll_withPageable_returnsPageOfComputers() throws Exception {
        Entry entry = new DefaultEntry("cn=PC01,ou=Computers,dc=example,dc=com");
        entry.add("cn", "PC01");
        entry.add("objectClass", "computer");

        Page<Entry> entryPage = new PageImpl<>(List.of(entry));
        when(ldapService.searchPage(anyString(), any(Sort.class), any(Pageable.class), any(String[].class)))
                .thenReturn(entryPage);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ComputerEntry> result = computersService.getAll(pageable, "", "*");

        assertThat(result).isNotNull();
        verify(ldapService).searchPage(contains("objectClass=computer"), any(Sort.class), eq(pageable), any(String[].class));
    }

    @Test
    void getAll_withFilters_returnsListOfComputers() throws Exception {
        Entry entry = new DefaultEntry("cn=PC01,ou=Computers,dc=example,dc=com");
        entry.add("cn", "PC01");
        entry.add("objectClass", "computer");

        when(ldapService.searchWithAttributes(anyString(), any(String[].class)))
                .thenReturn(List.of(entry));

        List<ComputerEntry> result = computersService.getAll("", "*");

        assertThat(result).isNotNull();
        verify(ldapService).searchWithAttributes(contains("objectClass=computer"), any(String[].class));
    }

    @Test
    void getByCN_returnsComputerWhenFound() throws Exception {
        Entry entry = new DefaultEntry("cn=PC01,ou=Computers,dc=example,dc=com");
        entry.add("cn", "PC01");
        entry.add("objectClass", "computer");
        entry.add("distinguishedName", "cn=PC01,ou=Computers,dc=example,dc=com");

        when(ldapService.search(anyString())).thenReturn(List.of(entry));

        ComputerEntry result = computersService.getByCN("PC01");

        assertThat(result).isNotNull();
        assertThat(result.getCn()).isEqualTo("PC01");
        verify(ldapService).search(contains("cn=PC01"));
    }

    @Test
    void getByCN_returnsNullWhenNotFound() throws Exception {
        when(ldapService.search(anyString())).thenReturn(Collections.emptyList());

        ComputerEntry result = computersService.getByCN("NonExistent");

        assertThat(result).isNull();
    }

    @Test
    void add_createsComputerWithDefaultContainer() throws Exception {
        when(ldapService.getComputersContainer()).thenReturn("CN=Computers,DC=example,DC=com");

        Entry createdEntry = new DefaultEntry("cn=PC01,CN=Computers,DC=example,DC=com");
        createdEntry.add("cn", "PC01");
        createdEntry.add("objectClass", "computer");
        createdEntry.add("distinguishedName", "cn=PC01,CN=Computers,DC=example,DC=com");
        createdEntry.add("userAccountControl", "4096");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        ComputerEntry result = computersService.add(null, "PC01", "Test computer", "HQ", true);

        assertThat(result).isNotNull();
        verify(ldapService).add(any(Entry.class));
        verify(ldapService).getComputersContainer();
    }

    @Test
    void add_createsComputerWithSpecifiedContainer() throws Exception {
        Entry createdEntry = new DefaultEntry("cn=PC01,OU=Workstations,DC=example,DC=com");
        createdEntry.add("cn", "PC01");
        createdEntry.add("objectClass", "computer");
        createdEntry.add("distinguishedName", "cn=PC01,OU=Workstations,DC=example,DC=com");
        createdEntry.add("userAccountControl", "4096");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        ComputerEntry result = computersService.add("OU=Workstations,DC=example,DC=com", "PC01", null, null, true);

        assertThat(result).isNotNull();
        verify(ldapService).add(any(Entry.class));
        verify(ldapService, never()).getComputersContainer();
    }

    @Test
    void add_setsDescriptionWhenProvided() throws Exception {
        when(ldapService.getComputersContainer()).thenReturn("CN=Computers,DC=example,DC=com");

        Entry createdEntry = new DefaultEntry("cn=PC01,CN=Computers,DC=example,DC=com");
        createdEntry.add("cn", "PC01");
        createdEntry.add("objectClass", "computer");
        createdEntry.add("distinguishedName", "cn=PC01,CN=Computers,DC=example,DC=com");
        createdEntry.add("userAccountControl", "4096");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        computersService.add(null, "PC01", "Test description", null, true);

        verify(ldapService).updateProperty(anyString(), eq("description"), eq("Test description"));
    }

    @Test
    void add_setsLocationWhenProvided() throws Exception {
        when(ldapService.getComputersContainer()).thenReturn("CN=Computers,DC=example,DC=com");

        Entry createdEntry = new DefaultEntry("cn=PC01,CN=Computers,DC=example,DC=com");
        createdEntry.add("cn", "PC01");
        createdEntry.add("objectClass", "computer");
        createdEntry.add("distinguishedName", "cn=PC01,CN=Computers,DC=example,DC=com");
        createdEntry.add("userAccountControl", "4096");

        when(ldapService.search(anyString())).thenReturn(List.of(createdEntry));

        computersService.add(null, "PC01", null, "Building A", true);

        verify(ldapService).updateProperty(anyString(), eq("location"), eq("Building A"));
    }

    @Test
    void update_modifiesComputerAndReturnsUpdated() throws Exception {
        Entry existingEntry = new DefaultEntry("cn=PC01,CN=Computers,DC=example,DC=com");
        existingEntry.add("cn", "PC01");
        existingEntry.add("objectClass", "computer");
        existingEntry.add("distinguishedName", "cn=PC01,CN=Computers,DC=example,DC=com");

        when(ldapService.search(anyString())).thenReturn(List.of(existingEntry));

        ComputerEntry computer = new ComputerEntry();
        computer.setCn("PC01");
        computer.setDistinguishedName("cn=PC01,CN=Computers,DC=example,DC=com");

        ComputerEntry result = computersService.update(computer);

        assertThat(result).isNotNull();
        verify(ldapService).update(any(ModifyRequest.class));
    }

    @Test
    void delete_removesComputer() throws Exception {
        computersService.delete("cn=PC01,CN=Computers,DC=example,DC=com");

        verify(ldapService).delete(any(Entry.class));
    }

    @Test
    void getUserControl_returnsUserAccountControl() {
        UserAccountControls result = computersService.getUserControl(512);

        assertThat(result).isNotNull();
    }

    @Test
    void getDefaultContainer_returnsComputersContainer() {
        when(ldapService.getComputersContainer()).thenReturn("CN=Computers,DC=example,DC=com");

        String result = computersService.getDefaultContainer();

        assertThat(result).isEqualTo("CN=Computers,DC=example,DC=com");
    }
}
