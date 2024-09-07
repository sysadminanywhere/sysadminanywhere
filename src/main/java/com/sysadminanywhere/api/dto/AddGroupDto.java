package com.sysadminanywhere.api.dto;

import com.sysadminanywhere.model.GroupEntry;
import com.sysadminanywhere.model.GroupScope;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddGroupDto {
    @NotNull
    private String distinguishedName;

    @NotNull
    private GroupEntry group;

    @NotNull
    private GroupScope groupScope;

    private boolean isSecurity;
}