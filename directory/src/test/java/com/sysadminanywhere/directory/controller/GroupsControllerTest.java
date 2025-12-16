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
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupsControllerTest {

    @Mock
    private GroupsService groupsService;

    @InjectMocks
    private GroupsController groupsController;

    @Test
    void getAll_returnsPageOfGroups() {
        GroupEntry group = createGroupEntry("Admins");

        Pageable pageable = PageRequest.of(0, 10);
        String[] attributes = {"cn"};
        Page<GroupEntry> page = new PageImpl<>(List.of(group));
        when(groupsService.getAll(pageable, "", attributes)).thenReturn(page);

        var response = groupsController.getAll(pageable, "", attributes);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(page);
        verify(groupsService).getAll(pageable, "", attributes);
    }

    @Test
    void getList_returnsListOfGroups() {
        GroupEntry group = createGroupEntry("Admins");
        String[] attributes = {"cn"};

        when(groupsService.getAll("", attributes)).thenReturn(List.of(group));

        var response = groupsController.getList("", attributes);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsExactly(group);
        verify(groupsService).getAll(eq(""), same(attributes));
    }

    @Test
    void getByCN_returnsGroup() {
        GroupEntry group = createGroupEntry("Admins");

        when(groupsService.getByCN("Admins")).thenReturn(group);

        var response = groupsController.getByCN("Admins");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(group);
        verify(groupsService).getByCN("Admins");
    }

    @Test
    void add_createsGroup() {
        AddGroupDto addGroupDto = new AddGroupDto();
        addGroupDto.setCn("TestGroup");
        addGroupDto.setDescription("Test group");
        addGroupDto.setGroupScope(GroupScope.Global);
        addGroupDto.setSecurity(true);

        GroupEntry createdGroup = createGroupEntry("TestGroup");

        when(groupsService.add(isNull(), eq("TestGroup"), eq("Test group"), eq(GroupScope.Global), eq(true)))
                .thenReturn(createdGroup);

        var response = groupsController.add(addGroupDto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(createdGroup);
        verify(groupsService).add(isNull(), eq("TestGroup"), eq("Test group"), eq(GroupScope.Global), eq(true));
    }

    @Test
    void update_modifiesGroup() {
        GroupEntry group = createGroupEntry("TestGroup");

        when(groupsService.update(any(GroupEntry.class))).thenReturn(group);

        var response = groupsController.update(group);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(group);
        verify(groupsService).update(any(GroupEntry.class));
    }

    @Test
    void delete_removesGroup() {
        doNothing().when(groupsService).delete(anyString());

        var response = groupsController.delete("cn=TestGroup,CN=Users,DC=example,DC=com");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(groupsService).delete("cn=TestGroup,CN=Users,DC=example,DC=com");
    }

    private GroupEntry createGroupEntry(String cn) {
        GroupEntry group = new GroupEntry();
        group.setCn(cn);
        group.setDistinguishedName("cn=" + cn + ",CN=Users,DC=example,DC=com");
        return group;
    }
}
