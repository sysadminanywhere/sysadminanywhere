package com.sysadminanywhere.common.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddUserDto {

    private String distinguishedName;

    private String cn;
    private String displayName;
    private String firstName;
    private String lastName;
    private String initials;

    private String password;

    private boolean cannotChangePassword;
    private boolean passwordNeverExpires;
    private boolean accountDisabled;
    private boolean mustChangePassword;

}