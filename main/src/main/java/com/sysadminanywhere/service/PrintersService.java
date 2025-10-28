package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.PrintersServiceClient;
import com.sysadminanywhere.common.directory.model.PrinterEntry;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrintersService {

    private final LdapService ldapService;
    private final PrintersServiceClient printersServiceClient;

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
        printersServiceClient.delete(distinguishedName);
    }

    public String getDefaultContainer() {
        return ldapService.getUsersContainer();
    }

    public LdapService getLdapService() {
        return ldapService;
    }

}