package com.sysadminanywhere.common.directory.dto;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddComputerDto {

    private String distinguishedName;

    private ComputerEntry computer;

    private boolean enabled;

}