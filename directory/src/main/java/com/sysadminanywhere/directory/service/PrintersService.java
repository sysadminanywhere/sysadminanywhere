package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.PrinterEntry;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PrintersService {

    @Getter
    private final LdapService ldapService;

    ResolveService<PrinterEntry> resolveService = new ResolveService<>(PrinterEntry.class);

    public PrintersService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    /**
     * Получение всех принтеров с постраничным выводом и фильтрацией
     */
    @SneakyThrows
    public Page<PrinterEntry> getAll(Pageable pageable, String filters, String... attributes) {
        try {
            Page<Entry> result = ldapService.searchPage("(&(objectClass=printQueue)" + filters + ")", pageable.getSort(), pageable, attributes);
            log.info("Retrieved printers page with filters");
            return resolveService.getADPage(result);
        } catch (Exception e) {
            log.error("Error retrieving printers page: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Получение всех принтеров без постраничного вывода
     */
    @SneakyThrows
    public List<PrinterEntry> getAll(String filters, String... attributes) {
        try {
            List<Entry> result = ldapService.searchWithAttributes("(&(objectClass=printQueue)" + filters + ")", attributes);
            log.info("Retrieved printers list with filters");
            return resolveService.getADList(result);
        } catch (Exception e) {
            log.error("Error retrieving printers list: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Получение принтера по CN (Common Name)
     */
    public PrinterEntry getByCN(String cn) {
        try {
            List<Entry> result = ldapService.search("(&(objectClass=printQueue)(cn=" + cn + "))");
            Optional<Entry> entry = result.stream().findFirst();

            if (entry.isPresent()) {
                log.info("Retrieved printer by CN: {}", cn);
                return resolveService.getADValue(entry.get());
            } else {
                log.warn("Printer not found by CN: {}", cn);
                return null;
            }
        } catch (Exception e) {
            log.error("Error retrieving printer by CN {}: {}", cn, e.getMessage());
            throw e;
        }
    }

    /**
     * Удаление принтера
     */
    @SneakyThrows
    public void delete(String distinguishedName) {
        try {
            Entry entry = new DefaultEntry(distinguishedName);
            ldapService.delete(entry);
            log.info("Printer deleted: {}", distinguishedName);
        } catch (Exception e) {
            log.error("Error deleting printer {}: {}", distinguishedName, e.getMessage());
            throw e;
        }
    }

    /**
     * Получение контейнера принтеров по умолчанию
     */
    public String getDefaultContainer() {
        return ldapService.getUsersContainer();
    }


}