package com.sysadminanywhere.model.wmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiskDriveEntity {

    @WMIAttribute(name = "Name")
    private String name;

    @WMIAttribute(name = "Model")
    private String model;

    @WMIAttribute(name = "Manufacturer")
    private String manufacturer;

    @WMIAttribute(name = "InterfaceType")
    private String interfaceType;

    @WMIAttribute(name = "Size")
    private String size;

    @WMIAttribute(name = "MediaType")
    private String mediaType;

    @WMIAttribute(name = "DiskDrive")
    private String diskDrive;

    @WMIAttribute(name = "FirmwareRevisions")
    private String firmwareRevisions;

    @WMIAttribute(name = "Partitions")
    private String partitions;

    @WMIAttribute(name = "SerialNumber")
    private String serialNumber;

}
