package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.dto.AddUserDto;
import com.sysadminanywhere.common.directory.dto.ChangeUserAccountControlDto;
import com.sysadminanywhere.common.directory.dto.ResetPasswordDto;
import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "users",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface UsersServiceClient {

    @GetMapping("/api/users")
    Page<UserEntry> getAll(Pageable pageable, @RequestParam("filters") String filters, @RequestParam("attributes") String[] attributes);

    @GetMapping("/api/users/list")
    List<UserEntry> getList(@RequestParam("filters") String filters);

    @GetMapping("/api/users/{cn}")
    UserEntry getByCN(@PathVariable("cn") String cn);

    @PostMapping("/api/users")
    UserEntry add(@RequestBody AddUserDto addUser);

    @PutMapping("/api/users")
    UserEntry update(@RequestBody UserEntry user);

    @DeleteMapping("/api/users")
    void delete(@RequestParam("distinguishedName") String distinguishedName);

    @PostMapping("/api/users/resetpassword")
    void resetPassword(@RequestBody ResetPasswordDto resetPasswordDto);

    @PostMapping("/api/users/changeuac")
    void changeUserAccountControl(@RequestBody ChangeUserAccountControlDto changeUserAccountControlDto);

}