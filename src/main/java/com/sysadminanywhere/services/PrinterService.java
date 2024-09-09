package com.sysadminanywhere.services;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.domain.FilterSpecification;
import com.sysadminanywhere.model.PrinterEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrinterService {

    private final DirectoryClient directoryClient;

    public PrinterService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Page<PrinterEntry> list(Pageable pageable, FilterSpecification<PrinterEntry> filter) {
        return directoryClient.getAllPrinters(pageable, filter.getFilters()).getBody();
    }

}
