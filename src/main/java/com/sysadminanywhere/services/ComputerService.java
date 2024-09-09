package com.sysadminanywhere.services;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.domain.FilterSpecification;
import com.sysadminanywhere.model.ComputerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ComputerService {

    private final DirectoryClient directoryClient;

    public ComputerService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Page<ComputerEntry> list(Pageable pageable, FilterSpecification<ComputerEntry> filter) {
        return directoryClient.getAllComputers(pageable, filter.getFilters()).getBody();
    }

}
