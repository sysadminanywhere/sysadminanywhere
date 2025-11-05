package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.PrintersServiceClient;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.model.PrinterEntry;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public Page<PrinterEntry> getAll(Pageable pageable, String filters, String... attributes) {
        try {
            return printersServiceClient.getAll(pageable, filters, attributes);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @SneakyThrows
    public List<PrinterEntry> getAll(String filters) {
        return printersServiceClient.getList(filters);
    }

    public List<PrinterEntry> getAll() {
        List<EntryDto> list = ldapService.searchWithAttributes("(objectClass=printQueue)",
                "cn");

        List<PrinterEntry> items = new ArrayList<>();

        if(list != null) {
            for (EntryDto entryDto : list) {
                PrinterEntry item = new PrinterEntry();
                item.setCn(entryDto.getAttributes().get("cn").toString());
                items.add(item);
            }
        }

        return items;
    }

    public PrinterEntry getByCN(String cn) {
        return printersServiceClient.getByCN(cn);
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