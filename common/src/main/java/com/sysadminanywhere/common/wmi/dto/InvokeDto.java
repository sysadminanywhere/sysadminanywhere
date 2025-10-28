package com.sysadminanywhere.common.wmi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvokeDto {

    private String hostName;
    private String path;
    private String className;
    private String methodName;
    private Map<String, Object> inputMap;

}