package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.dto.AddUserDto;
import com.sysadminanywhere.common.directory.dto.ResetPasswordDto;
import com.sysadminanywhere.common.directory.dto.ChangeUserAccountControlDto;
import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.directory.service.UsersService;
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
class UsersControllerTest {

    @Mock
    private UsersService usersService;

    @InjectMocks
    private UsersController usersController;

    @Test
    void getAll_shouldReturnPageOfUsers() {
        Page<UserEntry> page = new PageImpl<>(List.of(new UserEntry()));
        when(usersService.getAll(any(PageRequest.class), anyString(), any(String[].class))).thenReturn(page);

        ResponseEntity<PageResponse<UserEntry>> result = usersController.getAll(PageRequest.of(0, 10), "filter", new String[]{"cn"});

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(usersService).getAll(any(PageRequest.class), eq("filter"), any(String[].class));
    }

    @Test
    void getList_shouldReturnListOfUsers() {
        List<UserEntry> users = List.of(new UserEntry(), new UserEntry());
        when(usersService.getAll(anyString(), any(String[].class))).thenReturn(users);

        ResponseEntity<List<UserEntry>> result = usersController.getList("", new String[]{"cn"});

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
    }

    @Test
    void getByCN_shouldReturnUser() {
        UserEntry user = new UserEntry();
        user.setCn("john.doe");
        when(usersService.getByCN("john.doe")).thenReturn(user);

        ResponseEntity<UserEntry> result = usersController.getByCN("john.doe");

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("john.doe", result.getBody().getCn());
    }

    @Test
    void resetPassword_shouldCallService() {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setDistinguishedName("CN=User,DC=com");
        dto.setPassword("NewPass123!");
        doNothing().when(usersService).resetPassword(anyString(), anyString());

        ResponseEntity<?> result = usersController.resetPassword(dto);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(usersService).resetPassword("CN=User,DC=com", "NewPass123!");
    }

    @Test
    void changeUserAccountControl_shouldCallService() {
        ChangeUserAccountControlDto dto = new ChangeUserAccountControlDto();
        UserEntry user = new UserEntry();
        user.setCn("test.user");
        dto.setUser(user);
        dto.setAccountDisabled(true);
        doNothing().when(usersService).changeUserAccountControl(any(UserEntry.class), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        ResponseEntity<?> result = usersController.changeUserAccountControl(dto);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(usersService).changeUserAccountControl(eq(user), anyBoolean(), anyBoolean(), eq(true), anyBoolean());
    }

    @Test
    void delete_shouldCallServiceDelete() {
        String dn = "CN=User,OU=Users,DC=example,DC=com";
        doNothing().when(usersService).delete(dn);

        ResponseEntity<?> result = usersController.delete(dn);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(usersService).delete(dn);
    }
}
