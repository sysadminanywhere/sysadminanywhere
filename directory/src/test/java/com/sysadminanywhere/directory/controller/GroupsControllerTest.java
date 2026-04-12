package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.dto.AddGroupDto;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.GroupScope;
import com.sysadminanywhere.directory.service.GroupsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupsControllerTest {

    @Mock
    private GroupsService groupsService;

    @InjectMocks
    private GroupsController groupsController;

    @Test
    void getAll_shouldReturnPageOfGroups() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<GroupEntry> page = new PageImpl<>(List.of(new GroupEntry()));
        when(groupsService.getAll(any(PageRequest.class), anyString(), any(String[].class))).thenReturn(page);

        ResponseEntity<Page<GroupEntry>> result = groupsController.getAll(pageable, "filter", new String[]{"cn"});

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(groupsService).getAll(eq(pageable), eq("filter"), any(String[].class));
    }

    @Test
    void getList_shouldReturnListOfGroups() {
        List<GroupEntry> groups = List.of(new GroupEntry(), new GroupEntry());
        when(groupsService.getAll(anyString(), any(String[].class))).thenReturn(groups);

        ResponseEntity<List<GroupEntry>> result = groupsController.getList("", new String[]{"cn"});

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
    }

    @Test
    void getByCN_shouldReturnGroup() {
        GroupEntry group = new GroupEntry();
        group.setCn("Domain Admins");
        when(groupsService.getByCN("Domain Admins")).thenReturn(group);

        ResponseEntity<GroupEntry> result = groupsController.getByCN("Domain Admins");

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Domain Admins", result.getBody().getCn());
    }

    @Test
    void add_shouldCreateGroup() {
        AddGroupDto dto = new AddGroupDto();
        dto.setDistinguishedName("CN=TestGroup,OU=Groups,DC=com");
        dto.setCn("TestGroup");
        dto.setGroupScope(GroupScope.Global);
        dto.setSecurity(true);

        GroupEntry group = new GroupEntry();
        group.setCn("TestGroup");
        lenient().when(groupsService.add(anyString(), anyString(), any(), any(GroupScope.class), anyBoolean()))
            .thenReturn(group);

        ResponseEntity<GroupEntry> result = groupsController.add(dto);

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    void delete_shouldCallServiceDelete() {
        String dn = "CN=TestGroup,OU=Groups,DC=example,DC=com";
        doNothing().when(groupsService).delete(dn);

        ResponseEntity<?> result = groupsController.delete(dn);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(groupsService).delete(dn);
    }
}
