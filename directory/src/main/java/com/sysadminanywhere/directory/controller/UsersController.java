package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.dto.AddUserDto;
import com.sysadminanywhere.common.directory.dto.ChangeUserAccountControlDto;
import com.sysadminanywhere.common.directory.dto.ResetPasswordDto;
import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.directory.service.UsersService;
import lombok.NonNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping()
    public ResponseEntity<Page<UserEntry>> getAll(@ParameterObject Pageable pageable, @RequestParam("filters") String filters, @RequestParam("attributes")  String[] attributes) {
        return new ResponseEntity<>(usersService.getAll(pageable, filters, attributes), HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserEntry>> getList(@RequestParam String filters) {
        return new ResponseEntity<>(usersService.getAll(filters), HttpStatus.OK);
    }

    @GetMapping("/{cn}")
    public ResponseEntity<UserEntry> getByCN(@PathVariable String cn) {
        return new ResponseEntity<>(usersService.getByCN(cn), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<UserEntry> add(@NonNull @RequestBody AddUserDto addUser) {
        return new ResponseEntity<>(usersService.add(
                addUser.getDistinguishedName(),
                addUser.getUser(),
                addUser.getPassword(),
                addUser.isCannotChangePassword(),
                addUser.isPasswordNeverExpires(),
                addUser.isAccountDisabled(),
                addUser.isMustChangePassword()
        ), HttpStatus.OK);
    }

    @PutMapping()
    public ResponseEntity<UserEntry> update(@NonNull @RequestBody UserEntry user) {
        return new ResponseEntity<>(usersService.update(user), HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity delete(@NonNull @RequestParam String distinguishedName) {
        usersService.delete(distinguishedName);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("resetpassword")
    public ResponseEntity resetPassword(@NonNull @RequestBody ResetPasswordDto resetPasswordDto) {
        usersService.resetPassword(resetPasswordDto.getDistinguishedName(), resetPasswordDto.getPassword());
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("changeuac")
    public ResponseEntity changeUserAccountControl(@NonNull @RequestBody ChangeUserAccountControlDto changeUserAccountControlDto) {
        usersService.changeUserAccountControl(changeUserAccountControlDto.getUser(),
                changeUserAccountControlDto.isCannotChangePassword(),
                changeUserAccountControlDto.isPasswordNeverExpires(),
                changeUserAccountControlDto.isAccountDisabled(),
                changeUserAccountControlDto.isMustChangePassword());

        return new ResponseEntity(HttpStatus.OK);
    }

}