package com.sysadminanywhere.common.wmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhysicalMemoryEntity {

    @WMIAttribute(name = "Attributes")
    private int attributes;

    @WMIAttribute(name = "BankLabel")
    private String bankLabel;

    @WMIAttribute(name = "Capacity")
    private long capacity;

    @WMIAttribute(name = "Caption")
    private String caption;

    @WMIAttribute(name = "ConfiguredClockSpeed")
    private int configuredClockSpeed;

    @WMIAttribute(name = "ConfiguredVoltage")
    private String configuredVoltage;

    @WMIAttribute(name = "CreationClassName")
    private String creationClassName;

    @WMIAttribute(name = "DataWidth")
    private int dataWidth;

    @WMIAttribute(name = "Description")
    private String description;

    @WMIAttribute(name = "DeviceLocator")
    private String deviceLocator;

    @WMIAttribute(name = "FormFactor")
    private int formFactor;

    @WMIAttribute(name = "HotSwappable")
    private String hotSwappable;

    @WMIAttribute(name = "InstallDate")
    private String installDate;

    @WMIAttribute(name = "InterleaveDataDepth")
    private int interleaveDataDepth;

    @WMIAttribute(name = "InterleavePosition")
    private int interleavePosition;

    @WMIAttribute(name = "Manufacturer")
    private String manufacturer;

    @WMIAttribute(name = "MaxVoltage")
    private String maxVoltage;

    @WMIAttribute(name = "MemoryType")
    private int memoryType;

    @WMIAttribute(name = "MinVoltage")
    private String minVoltage;

    @WMIAttribute(name = "Model")
    private String model;

    @WMIAttribute(name = "Name")
    private String name;

    @WMIAttribute(name = "OtherIdentifyingInfo")
    private String otherIdentifyingInfo;

    @WMIAttribute(name = "PartNumber")
    private String partNumber;

    @WMIAttribute(name = "PositionInRow")
    private String positionInRow;

    @WMIAttribute(name = "PoweredOn")
    private String poweredOn;

    @WMIAttribute(name = "Removable")
    private String removable;

    @WMIAttribute(name = "Replaceable")
    private String replaceable;

    @WMIAttribute(name = "SerialNumber")
    private String serialNumber;

    @WMIAttribute(name = "SKU")
    private String sKU;

    @WMIAttribute(name = "SMBIOSMemoryType")
    private int sMBIOSMemoryType;

    @WMIAttribute(name = "Speed")
    private int speed;

    @WMIAttribute(name = "Status")
    private String status;

    @WMIAttribute(name = "Tag")
    private String tag;

    @WMIAttribute(name = "TotalWidth")
    private int totalWidth;

    @WMIAttribute(name = "TypeDetail")
    private int typeDetail;

    @WMIAttribute(name = "Version")
    private String version;

}