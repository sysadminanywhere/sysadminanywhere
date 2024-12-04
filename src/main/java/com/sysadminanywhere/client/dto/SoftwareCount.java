package com.sysadminanywhere.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SoftwareCount {

    private String name;
    private String vendor;
    private String version;
    private Long count;

}