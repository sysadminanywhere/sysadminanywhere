package com.sysadminanywhere.model.wmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogicalDiskEntity {

    @WMIAttribute(name = "Name")
    private String name;

    @WMIAttribute(name = "Description")
    private String description;

    @WMIAttribute(name = "FileSystem")
    private String fileSystem;

    @WMIAttribute(name = "Size")
    private String size;

    @WMIAttribute(name = "ProviderName")
    private String providerName;

    @WMIAttribute(name = "SupportsFileCompression")
    private String supportsFileCompression;

    @WMIAttribute(name = "SupportsDiskQuotas")
    private String supportsDiskQuotas;

    @WMIAttribute(name = "FreeSpace")
    private String freeSpace;

    @WMIAttribute(name = "Compressed")
    private String compressed;

    @WMIAttribute(name = "VolumeSerialNumber")
    private String volumeSerialNumber;

    @WMIAttribute(name = "VolumeName")
    private String volumeName;

}