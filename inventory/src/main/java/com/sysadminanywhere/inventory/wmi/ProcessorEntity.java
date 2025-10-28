package com.sysadminanywhere.inventory.wmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessorEntity {

    @WMIAttribute(name = "Name")
    private String name;

    @WMIAttribute(name = "Manufacturer")
    private String manufacturer;

    @WMIAttribute(name = "Model")
    private String model;

    @WMIAttribute(name = "Description")
    private String description;

    @WMIAttribute(name = "ThreadCount")
    private String threadCount;

    @WMIAttribute(name = "NumberOfCores")
    private String numberOfCores;

    @WMIAttribute(name = "NumberOfLogicalProcessors")
    private String numberOfLogicalProcessors;

    @WMIAttribute(name = "ProcessorId")
    private String processorId;

    @WMIAttribute(name = "SocketDesignation")
    private String socketDesignation;

    @WMIAttribute(name = "MaxClockSpeed")
    private String maxClockSpeed;

    @WMIAttribute(name = "Voltage")
    private String voltage;

    @WMIAttribute(name = "AddressWidth")
    private String addressWidth;

    @WMIAttribute(name = "Device")
    private String device;

    @WMIAttribute(name = "L2CacheSize")
    private String l2CacheSize;

    @WMIAttribute(name = "L3CacheSize")
    private String l3CacheSize;

    @WMIAttribute(name = "NumberOfEnabledCore")
    private String numberOfEnabledCore;

    @WMIAttribute(name = "CurrentClockSpeed")
    private String currentClockSpeed;

    @WMIAttribute(name = "SerialNumber")
    private String serialNumber;

    @WMIAttribute(name = "VirtualizationFirmwareEnabled")
    private String virtualizationFirmwareEnabled;

}