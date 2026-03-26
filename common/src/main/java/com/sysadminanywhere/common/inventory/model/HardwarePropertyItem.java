package com.sysadminanywhere.common.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HardwarePropertyItem {
    private Long propertyId;
    private String propertyName;
    private String propertyValue;
    private Long computerHardwareId;
}
