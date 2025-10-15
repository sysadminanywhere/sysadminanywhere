package com.sysadminanywhere.common.directory.dto;

import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.GroupScope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddGroupDto {

    private String distinguishedName;
    private GroupEntry group;
    private GroupScope groupScope;
    private boolean isSecurity;

}