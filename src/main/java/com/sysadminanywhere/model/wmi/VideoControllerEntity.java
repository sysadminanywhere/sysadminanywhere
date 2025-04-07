package com.sysadminanywhere.model.wmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoControllerEntity {

    @WMIAttribute(name = "AcceleratorCapabilities")
    private String acceleratorCapabilities;

    @WMIAttribute(name = "AdapterCompatibility")
    private String adapterCompatibility;

    @WMIAttribute(name = "AdapterDACType")
    private String adapterDACType;

    @WMIAttribute(name = "AdapterRAM")
    private long adapterRAM;

    @WMIAttribute(name = "Availability")
    private int availability;

    @WMIAttribute(name = "CapabilityDescriptions")
    private String capabilityDescriptions;

    @WMIAttribute(name = "Caption")
    private String caption;

    @WMIAttribute(name = "ColorTableEntries")
    private String colorTableEntries;

    @WMIAttribute(name = "ConfigManagerErrorCode")
    private int configManagerErrorCode;

    @WMIAttribute(name = "ConfigManagerUserConfig")
    private boolean configManagerUserConfig;

    @WMIAttribute(name = "CreationClassName")
    private String creationClassName;

    @WMIAttribute(name = "CurrentBitsPerPixel")
    private int currentBitsPerPixel;

    @WMIAttribute(name = "CurrentHorizontalResolution")
    private int currentHorizontalResolution;

    @WMIAttribute(name = "CurrentNumberOfColors")
    private long currentNumberOfColors;

    @WMIAttribute(name = "CurrentNumberOfColumns")
    private int currentNumberOfColumns;

    @WMIAttribute(name = "CurrentNumberOfRows")
    private int currentNumberOfRows;

    @WMIAttribute(name = "CurrentRefreshRate")
    private int currentRefreshRate;

    @WMIAttribute(name = "CurrentScanMode")
    private int currentScanMode;

    @WMIAttribute(name = "CurrentVerticalResolution")
    private int currentVerticalResolution;

    @WMIAttribute(name = "Description")
    private String description;

    @WMIAttribute(name = "DeviceID")
    private String deviceID;

    @WMIAttribute(name = "DeviceSpecificPens")
    private String deviceSpecificPens;

    @WMIAttribute(name = "DitherType")
    private int ditherType;

    @WMIAttribute(name = "DriverDate")
    private String driverDate;

    @WMIAttribute(name = "DriverVersion")
    private String driverVersion;

    @WMIAttribute(name = "ErrorCleared")
    private String errorCleared;

    @WMIAttribute(name = "ErrorDescription")
    private String errorDescription;

    @WMIAttribute(name = "ICMIntent")
    private String iCMIntent;

    @WMIAttribute(name = "ICMMethod")
    private String iCMMethod;

    @WMIAttribute(name = "InfFilename")
    private String infFilename;

    @WMIAttribute(name = "InfSection")
    private String infSection;

    @WMIAttribute(name = "InstallDate")
    private String installDate;

    @WMIAttribute(name = "InstalledDisplayDrivers")
    private String installedDisplayDrivers;

    @WMIAttribute(name = "LastErrorCode")
    private String lastErrorCode;

    @WMIAttribute(name = "MaxMemorySupported")
    private String maxMemorySupported;

    @WMIAttribute(name = "MaxNumberControlled")
    private String maxNumberControlled;

    @WMIAttribute(name = "MaxRefreshRate")
    private int maxRefreshRate;

    @WMIAttribute(name = "MinRefreshRate")
    private int minRefreshRate;

    @WMIAttribute(name = "Monochrome")
    private boolean monochrome;

    @WMIAttribute(name = "Name")
    private String name;

    @WMIAttribute(name = "NumberOfColorPlanes")
    private String numberOfColorPlanes;

    @WMIAttribute(name = "NumberOfVideoPages")
    private String numberOfVideoPages;

    @WMIAttribute(name = "PNPDeviceID")
    private String pNPDeviceID;

    @WMIAttribute(name = "PowerManagementCapabilities")
    private String powerManagementCapabilities;

    @WMIAttribute(name = "PowerManagementSupported")
    private String powerManagementSupported;

    @WMIAttribute(name = "ProtocolSupported")
    private String protocolSupported;

    @WMIAttribute(name = "ReservedSystemPaletteEntries")
    private String reservedSystemPaletteEntries;

    @WMIAttribute(name = "SpecificationVersion")
    private String specificationVersion;

    @WMIAttribute(name = "Status")
    private String status;

    @WMIAttribute(name = "StatusInfo")
    private String statusInfo;

    @WMIAttribute(name = "SystemCreationClassName")
    private String systemCreationClassName;

    @WMIAttribute(name = "SystemName")
    private String systemName;

    @WMIAttribute(name = "SystemPaletteEntries")
    private String systemPaletteEntries;

    @WMIAttribute(name = "TimeOfLastReset")
    private String timeOfLastReset;

    @WMIAttribute(name = "VideoArchitecture")
    private int videoArchitecture;

    @WMIAttribute(name = "VideoMemoryType")
    private int videoMemoryType;

    @WMIAttribute(name = "VideoMode")
    private String videoMode;

    @WMIAttribute(name = "VideoModeDescription")
    private String videoModeDescription;

    @WMIAttribute(name = "VideoProcessor")
    private String videoProcessor;

}