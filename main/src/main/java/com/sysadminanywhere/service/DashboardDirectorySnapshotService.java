package com.sysadminanywhere.service;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.UserEntry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class DashboardDirectorySnapshotService {
    private static final Duration MAX_AGE = Duration.ofMinutes(1);

    private final UsersService usersService;
    private final ComputersService computersService;
    private final GroupsService groupsService;
    private final PrintersService printersService;
    private final ContactsService contactsService;
    private Snapshot snapshot;

    public DashboardDirectorySnapshotService(UsersService usersService, ComputersService computersService,
                                             GroupsService groupsService, PrintersService printersService,
                                             ContactsService contactsService) {
        this.usersService = usersService;
        this.computersService = computersService;
        this.groupsService = groupsService;
        this.printersService = printersService;
        this.contactsService = contactsService;
    }

    public synchronized Snapshot get() {
        if (snapshot != null && Duration.between(snapshot.loadedAt(), Instant.now()).compareTo(MAX_AGE) < 0) {
            return snapshot;
        }
        snapshot = new Snapshot(Instant.now(), List.copyOf(usersService.getAll()),
                List.copyOf(computersService.getAll()), List.copyOf(groupsService.getAll()),
                printersService.getAll().size(), contactsService.getAll().size());
        return snapshot;
    }

    public synchronized void invalidate() {
        snapshot = null;
    }

    public record Snapshot(Instant loadedAt, List<UserEntry> users, List<ComputerEntry> computers,
                           List<GroupEntry> groups, int printers, int contacts) {
    }
}
