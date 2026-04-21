package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.dto.AddUserDto;
import com.sysadminanywhere.common.directory.dto.ChangeUserAccountControlDto;
import com.sysadminanywhere.common.directory.dto.ResetPasswordDto;
import com.sysadminanywhere.common.directory.model.UserEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

public interface UsersServiceClient {

    @GetExchange("/api/users")
    Page<UserEntry> getAll(Pageable pageable, String filters, String[] attributes);

    @GetExchange("/api/users/list")
    List<UserEntry> getList(String filters, String[] attributes);

    @GetExchange("/api/users/{cn}")
    UserEntry getByCN(String cn);

    @PostExchange("/api/users")
    UserEntry add(AddUserDto addUser);

    @PutExchange("/api/users")
    UserEntry update(UserEntry user);

    @DeleteExchange("/api/users")
    void delete(String distinguishedName);

    @PostExchange("/api/users/resetpassword")
    ResponseEntity<?> resetPassword(ResetPasswordDto resetPasswordDto);

    @PostExchange("/api/users/changeuac")
    ResponseEntity<?> changeUserAccountControl(ChangeUserAccountControlDto changeUserAccountControlDto);

}
