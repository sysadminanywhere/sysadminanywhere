package com.sysadminanywhere.common.directory.dto;

import com.sysadminanywhere.common.directory.model.UserEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeUserAccountControlDto {

    private UserEntry user;

    private boolean isCannotChangePassword;
    private boolean isPasswordNeverExpires;
    private boolean isAccountDisabled;
    private boolean isMustChangePassword;

}
