package com.sysadminanywhere.common.directory.dto;

import com.sysadminanywhere.common.directory.model.ContactEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddContactDto {

    private String distinguishedName;

    private String cn;
    private String displayName;
    private String firstName;
    private String lastName;
    private String initials;

}