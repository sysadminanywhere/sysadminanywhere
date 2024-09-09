package com.sysadminanywhere.services;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.model.UserEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ComputerService {

    private final DirectoryClient directoryClient;

    public ComputerService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Page<ComputerEntry> list(Pageable pageable, Specification<ComputerEntry> filter) {
        return directoryClient.getAllComputers(pageable).getBody();
    }

}
