package com.sysadminanywhere.inventory.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SoftwareDto {

    private Long id;
    private String name;
    private String vendor;
    private String version;

}
