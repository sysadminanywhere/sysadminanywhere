package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.dto.AddUserDto;
import com.sysadminanywhere.common.directory.dto.ChangeUserAccountControlDto;
import com.sysadminanywhere.common.directory.dto.ResetPasswordDto;
import com.sysadminanywhere.common.directory.model.UserEntry;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

public interface UsersServiceClient {

    @GetExchange("/api/users")
    PageResponse<UserEntry> getAll(@RequestParam int page, @RequestParam int size, @RequestParam String sort, @RequestParam String filters, @RequestParam String[] attributes);

    @GetExchange("/api/users/list")
    List<UserEntry> getList(@RequestParam String filters, @RequestParam String[] attributes);

    @GetExchange("/api/users/{cn}")
    UserEntry getByCN(@PathVariable String cn);

    @PostExchange("/api/users")
    UserEntry add(@RequestBody AddUserDto addUser);

    @PutExchange("/api/users")
    UserEntry update(@RequestBody UserEntry user);

    @DeleteExchange("/api/users")
    void delete(@RequestParam String distinguishedName);

    @PostExchange("/api/users/resetpassword")
    ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto);

    @PostExchange("/api/users/changeuac")
    ResponseEntity<?> changeUserAccountControl(@RequestBody ChangeUserAccountControlDto changeUserAccountControlDto);

}
