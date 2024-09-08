package com.sysadminanywhere.services;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.model.GroupEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class GroupService {

    private final DirectoryClient directoryClient;

    public GroupService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Page<GroupEntry> list(Pageable pageable, Specification<GroupEntry> filter) {
        return directoryClient.getAllGroups(pageable).getBody();
    }

}
