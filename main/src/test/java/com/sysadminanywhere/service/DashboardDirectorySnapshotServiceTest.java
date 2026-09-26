package com.sysadminanywhere.service;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.UserEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardDirectorySnapshotServiceTest {
    @Test
    void reusesDirectoryDataAndReloadsAfterInvalidation() {
        UsersService users = mock(UsersService.class);
        ComputersService computers = mock(ComputersService.class);
        GroupsService groups = mock(GroupsService.class);
        PrintersService printers = mock(PrintersService.class);
        ContactsService contacts = mock(ContactsService.class);
        when(users.getAll()).thenReturn(List.of(new UserEntry()));
        when(computers.getAll()).thenReturn(List.of(new ComputerEntry()));
        when(groups.getAll()).thenReturn(List.of(new GroupEntry()));
        when(printers.getAll()).thenReturn(List.of());
        when(contacts.getAll()).thenReturn(List.of());

        DashboardDirectorySnapshotService service = new DashboardDirectorySnapshotService(
                users, computers, groups, printers, contacts);

        var first = service.get();
        assertSame(first, service.get());
        assertEquals(1, first.users().size());
        assertEquals(1, first.computers().size());
        assertEquals(1, first.groups().size());
        verify(users).getAll();
        verify(computers).getAll();

        service.invalidate();
        service.get();
        verify(users, times(2)).getAll();
        verify(computers, times(2)).getAll();
    }
}
