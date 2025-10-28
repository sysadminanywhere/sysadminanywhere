package com.sysadminanywhere.inventory.wmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BIOSEntity {

    @WMIAttribute(name = "Manufacturer")
    private String manufacturer;

    @WMIAttribute(name = "Version")
    private String version;

    @WMIAttribute(name = "SystemBIOSMajorVersion")
    private String systemBIOSMajorVersion;

    @WMIAttribute(name = "SystemBIOSMinorVersion")
    private String systemBIOSMinorVersion;

    @WMIAttribute(name = "SMBIOSBIOSVersion")
    private String sMBIOSBIOSVersion;

    @WMIAttribute(name = "SMBIOSMajorVersion")
    private String sMBIOSMajorVersion;

    @WMIAttribute(name = "SMBIOSMinorVersion")
    private String sMBIOSMinorVersion;

    @WMIAttribute(name = "ReleaseDate")
    private String releaseDate;

    @WMIAttribute(name = "SerialNumber")
    private String serialNumber;

}