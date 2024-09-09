package com.sysadminanywhere.services;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.model.PrinterEntry;
import com.sysadminanywhere.model.UserEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PrinterService {

    private final DirectoryClient directoryClient;

    public PrinterService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Page<PrinterEntry> list(Pageable pageable, Specification<PrinterEntry> filter) {
        return directoryClient.getAllPrinters(pageable).getBody();
    }

}
