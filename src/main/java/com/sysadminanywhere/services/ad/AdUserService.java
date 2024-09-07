package com.sysadminanywhere.services.ad;

import com.sysadminanywhere.api.DirectoryClient;
import com.sysadminanywhere.data.SamplePerson;
import com.sysadminanywhere.model.UserEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class AdUserService {

    private final DirectoryClient directoryClient;

    public AdUserService(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Page<UserEntry> list(Pageable pageable, Specification<SamplePerson> filter) {
        return directoryClient.getAllUsers(pageable).getBody();
    }

}
