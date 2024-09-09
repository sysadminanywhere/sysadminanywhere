package com.sysadminanywhere.services;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.domain.FilterSpecification;
import com.sysadminanywhere.model.UserEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private final DirectoryClient directoryClient;

    public UserService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Page<UserEntry> list(Pageable pageable, FilterSpecification<UserEntry> filter) {
        return directoryClient.getAllUsers(pageable, filter.getFilters()).getBody();
    }

    public UserEntry getByCN(String cn) {
        return directoryClient.getUserByCN(cn).getBody();
    }

}