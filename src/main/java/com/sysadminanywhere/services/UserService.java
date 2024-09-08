package com.sysadminanywhere.services;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.model.UserEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final DirectoryClient directoryClient;

    public UserService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Page<UserEntry> list(Pageable pageable, Specification<UserEntry> filter) {
        return directoryClient.getAllUsers(pageable).getBody();
    }

}
