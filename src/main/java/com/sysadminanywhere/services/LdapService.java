package com.sysadminanywhere.services;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.model.UserEntry;
import org.springframework.stereotype.Service;

@Service
public class LdapService {

    private final DirectoryClient directoryClient;

    public LdapService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public UserEntry me() {
        return directoryClient.getMe().getBody();
    }

}
