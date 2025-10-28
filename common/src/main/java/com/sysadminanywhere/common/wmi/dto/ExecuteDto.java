package com.sysadminanywhere.common.wmi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteDto {

    private String hostName;
    private String wqlQuery;

}