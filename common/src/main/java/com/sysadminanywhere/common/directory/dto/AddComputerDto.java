package com.sysadminanywhere.common.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddComputerDto {

    private String distinguishedName;

    private String cn;
    private String description;
    private String location;

    private boolean enabled;

}