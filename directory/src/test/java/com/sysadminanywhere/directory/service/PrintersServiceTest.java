package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.PrinterEntry;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
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
class PrintersServiceTest {

    @Mock
    private LdapService ldapService;

    private PrintersService printersService;

    @BeforeEach
    void setUp() {
        printersService = new PrintersService(ldapService);
    }

    @Test
    void getAll_withPageable_returnsPageOfPrinters() throws Exception {
        Entry entry = new DefaultEntry("cn=Printer01,ou=Printers,dc=example,dc=com");
        entry.add("cn", "Printer01");
        entry.add("objectClass", "printQueue");

        Page<Entry> entryPage = new PageImpl<>(List.of(entry));
        when(ldapService.searchPage(anyString(), any(Sort.class), any(Pageable.class), any(String[].class)))
                .thenReturn(entryPage);

        Pageable pageable = PageRequest.of(0, 10);
        Page<PrinterEntry> result = printersService.getAll(pageable, "", "*");

        assertThat(result).isNotNull();
        verify(ldapService).searchPage(contains("objectClass=printQueue"), any(Sort.class), eq(pageable), any(String[].class));
    }

    @Test
    void getAll_withFilters_returnsListOfPrinters() throws Exception {
        Entry entry = new DefaultEntry("cn=Printer01,ou=Printers,dc=example,dc=com");
        entry.add("cn", "Printer01");
        entry.add("objectClass", "printQueue");

        when(ldapService.searchWithAttributes(anyString(), any(String[].class)))
                .thenReturn(List.of(entry));

        List<PrinterEntry> result = printersService.getAll("", "*");

        assertThat(result).isNotNull();
        verify(ldapService).searchWithAttributes(contains("objectClass=printQueue"), any(String[].class));
    }

    @Test
    void getByCN_returnsPrinterWhenFound() throws Exception {
        Entry entry = new DefaultEntry("cn=Printer01,ou=Printers,dc=example,dc=com");
        entry.add("cn", "Printer01");
        entry.add("objectClass", "printQueue");
        entry.add("distinguishedName", "cn=Printer01,ou=Printers,dc=example,dc=com");

        when(ldapService.search(anyString())).thenReturn(List.of(entry));

        PrinterEntry result = printersService.getByCN("Printer01");

        assertThat(result).isNotNull();
        assertThat(result.getCn()).isEqualTo("Printer01");
        verify(ldapService).search(contains("cn=Printer01"));
    }

    @Test
    void getByCN_returnsNullWhenNotFound() {
        when(ldapService.search(anyString())).thenReturn(Collections.emptyList());

        PrinterEntry result = printersService.getByCN("NonExistent");

        assertThat(result).isNull();
    }

    @Test
    void delete_removesPrinter() throws Exception {
        printersService.delete("cn=Printer01,ou=Printers,dc=example,dc=com");

        verify(ldapService).delete(any(Entry.class));
    }

    @Test
    void getDefaultContainer_returnsUsersContainer() {
        when(ldapService.getUsersContainer()).thenReturn("CN=Users,DC=example,DC=com");

        String result = printersService.getDefaultContainer();

        assertThat(result).isEqualTo("CN=Users,DC=example,DC=com");
    }

    @Test
    void getLdapService_returnsLdapService() {
        LdapService result = printersService.getLdapService();

        assertThat(result).isEqualTo(ldapService);
    }
}
