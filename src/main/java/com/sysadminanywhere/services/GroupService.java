package com.sysadminanywhere.services;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.domain.FilterSpecification;
import com.sysadminanywhere.model.GroupEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GroupService {

    private final DirectoryClient directoryClient;

    public GroupService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Page<GroupEntry> list(Pageable pageable, FilterSpecification<GroupEntry> filter) {
        return directoryClient.getAllGroups(pageable, filter.getFilters()).getBody();
    }

}
