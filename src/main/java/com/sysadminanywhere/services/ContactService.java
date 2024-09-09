package com.sysadminanywhere.services;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.model.ContactEntry;
import com.sysadminanywhere.model.UserEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private final DirectoryClient directoryClient;

    public ContactService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Page<ContactEntry> list(Pageable pageable, Specification<UserEntry> filter) {
        return directoryClient.getAllContacts(pageable).getBody();
    }

}
