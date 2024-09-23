package com.sysadminanywhere.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SoftwareEntity {

    @WMIAttribute(name = "Name")
    String name;

    @WMIAttribute(name = "Vendor")
    String vendor;

    @WMIAttribute(name = "Version")
    String version;

}
