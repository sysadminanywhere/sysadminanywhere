package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.PrintersServiceClient;
import com.sysadminanywhere.common.directory.model.PrinterEntry;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrintersService {

    private final LdapService ldapService;
    private final PrintersServiceClient printersServiceClient;

    ResolveService<PrinterEntry> resolveService = new ResolveService<>(PrinterEntry.class);

    public PrintersService(LdapService ldapService, PrintersServiceClient printersServiceClient) {
        this.ldapService = ldapService;
        this.printersServiceClient = printersServiceClient;
    }

    @SneakyThrows
    public Page<PrinterEntry> getAll(Pageable pageable, String filters) {
        return printersServiceClient.getAll(pageable, filters);
    }

    @SneakyThrows
    public List<PrinterEntry> getAll(String filters) {
        return printersServiceClient.getList(filters);
    }

    public PrinterEntry getByCN(String cn) {
        return printersServiceClient.getList("(&(objectClass=printQueue)(cn=" + cn + "))").getFirst();
    }

    @SneakyThrows
    public void delete(String distinguishedName) {
        Entry entry = new DefaultEntry(distinguishedName);
        ldapService.delete(entry);
    }

    public String getDefaultContainer() {
        return ldapService.getUsersContainer();
    }

    public LdapService getLdapService() {
        return ldapService;
    }

}