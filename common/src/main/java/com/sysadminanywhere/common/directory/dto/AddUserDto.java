package com.sysadminanywhere.common.directory.dto;

import com.sysadminanywhere.common.directory.model.UserEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddUserDto {

    private String distinguishedName;
    private UserEntry user;
    private String password;

    private boolean cannotChangePassword;
    private boolean passwordNeverExpires;
    private boolean accountDisabled;
    private boolean mustChangePassword;

}