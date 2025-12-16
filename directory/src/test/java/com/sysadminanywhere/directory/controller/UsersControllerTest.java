package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.dto.AddUserDto;
import com.sysadminanywhere.common.directory.dto.ChangeUserAccountControlDto;
import com.sysadminanywhere.common.directory.dto.ResetPasswordDto;
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
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private UsersService usersService;

    @InjectMocks
    private UsersController usersController;

    @Test
    void getAll_returnsPageOfUsers() {
        UserEntry user = createUserEntry("jdoe");

        Pageable pageable = PageRequest.of(0, 10);
        String[] attributes = {"cn"};
        Page<UserEntry> page = new PageImpl<>(List.of(user));
        when(usersService.getAll(pageable, "", attributes)).thenReturn(page);

        var response = usersController.getAll(pageable, "", attributes);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(page);
        verify(usersService).getAll(pageable, "", attributes);
    }

    @Test
    void getList_returnsListOfUsers() {
        UserEntry user = createUserEntry("jdoe");
        String[] attributes = {"cn"};

        when(usersService.getAll("", attributes)).thenReturn(List.of(user));

        var response = usersController.getList("", attributes);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsExactly(user);
        verify(usersService).getAll(eq(""), same(attributes));
    }

    @Test
    void getByCN_returnsUser() {
        UserEntry user = createUserEntry("jdoe");

        when(usersService.getByCN("jdoe")).thenReturn(user);

        var response = usersController.getByCN("jdoe");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(user);
        verify(usersService).getByCN("jdoe");
    }

    @Test
    void add_createsUser() {
        AddUserDto addUserDto = new AddUserDto();
        addUserDto.setCn("jdoe");
        addUserDto.setDisplayName("John Doe");
        addUserDto.setFirstName("John");
        addUserDto.setLastName("Doe");
        addUserDto.setPassword("password123");
        addUserDto.setCannotChangePassword(false);
        addUserDto.setPasswordNeverExpires(false);
        addUserDto.setAccountDisabled(false);
        addUserDto.setMustChangePassword(false);

        UserEntry createdUser = createUserEntry("jdoe");

        when(usersService.add(isNull(), eq("jdoe"), eq("John Doe"), eq("John"), eq("Doe"), isNull(),
                eq("password123"), eq(false), eq(false), eq(false), eq(false)))
                .thenReturn(createdUser);

        var response = usersController.add(addUserDto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(createdUser);
        verify(usersService).add(isNull(), eq("jdoe"), eq("John Doe"), eq("John"), eq("Doe"), isNull(),
                eq("password123"), eq(false), eq(false), eq(false), eq(false));
    }

    @Test
    void update_modifiesUser() {
        UserEntry user = createUserEntry("jdoe");

        when(usersService.update(any(UserEntry.class))).thenReturn(user);

        var response = usersController.update(user);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(user);
        verify(usersService).update(any(UserEntry.class));
    }

    @Test
    void delete_removesUser() {
        doNothing().when(usersService).delete(anyString());

        var response = usersController.delete("cn=jdoe,CN=Users,DC=example,DC=com");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(usersService).delete("cn=jdoe,CN=Users,DC=example,DC=com");
    }

    @Test
    void resetPassword_resetsUserPassword() {
        doNothing().when(usersService).resetPassword(anyString(), anyString());

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setDistinguishedName("cn=jdoe,CN=Users,DC=example,DC=com");
        resetPasswordDto.setPassword("newPassword123");

        var response = usersController.resetPassword(resetPasswordDto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(usersService).resetPassword("cn=jdoe,CN=Users,DC=example,DC=com", "newPassword123");
    }

    @Test
    void changeUserAccountControl_changesUAC() {
        UserEntry user = createUserEntry("jdoe");
        user.setUserAccountControl(512);

        ChangeUserAccountControlDto changeUacDto = new ChangeUserAccountControlDto();
        changeUacDto.setUser(user);
        changeUacDto.setCannotChangePassword(true);
        changeUacDto.setPasswordNeverExpires(false);
        changeUacDto.setAccountDisabled(false);
        changeUacDto.setMustChangePassword(false);

        doNothing().when(usersService).changeUserAccountControl(any(UserEntry.class), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        var response = usersController.changeUserAccountControl(changeUacDto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(usersService).changeUserAccountControl(any(UserEntry.class), eq(true), eq(false), eq(false), eq(false));
    }

    private UserEntry createUserEntry(String cn) {
        UserEntry user = new UserEntry();
        user.setCn(cn);
        user.setDistinguishedName("cn=" + cn + ",CN=Users,DC=example,DC=com");
        user.setPwdLastSet("0");
        return user;
    }
}
