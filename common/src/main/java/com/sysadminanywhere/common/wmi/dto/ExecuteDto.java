package com.sysadminanywhere.common.wmi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteDto {

    @JsonProperty("hostName")
    private String hostName;

    @JsonProperty("wqlQuery")
    private String wqlQuery;

}