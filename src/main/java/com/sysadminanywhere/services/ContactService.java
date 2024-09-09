package com.sysadminanywhere.services;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.domain.FilterSpecification;
import com.sysadminanywhere.model.ContactEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ContactService {

    private final DirectoryClient directoryClient;

    public ContactService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Page<ContactEntry> list(Pageable pageable, FilterSpecification<ContactEntry> filter) {
        return directoryClient.getAllContacts(pageable, filter.getFilters()).getBody();
    }

}
