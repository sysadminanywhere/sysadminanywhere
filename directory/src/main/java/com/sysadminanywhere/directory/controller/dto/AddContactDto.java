package com.sysadminanywhere.directory.controller.dto;

import com.sysadminanywhere.directory.model.ContactEntry;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddContactDto {
    @NotNull
    private String distinguishedName;

    @NotNull
    private ContactEntry contact;
}