package com.sysadminanywhere.inventory.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SoftwareOnComputer {

    private String name;
    private String vendor;
    private String version;
    private LocalDateTime installDate;
    private LocalDateTime checkingDate;

}
