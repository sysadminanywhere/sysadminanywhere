package com.sysadminanywhere.directory.controller.dto;

import com.sysadminanywhere.directory.model.UserEntry;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddUserDto {
    @NotNull
    private String distinguishedName;

    @NotNull
    private UserEntry user;

    @NotNull
    private String password;

    private boolean isCannotChangePassword;
    private boolean isPasswordNeverExpires;
    private boolean isAccountDisabled;
    private boolean isMustChangePassword;
}