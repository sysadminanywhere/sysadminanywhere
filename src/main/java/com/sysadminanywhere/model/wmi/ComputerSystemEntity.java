package com.sysadminanywhere.model.wmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComputerSystemEntity {

    @WMIAttribute(name = "AdminPasswordStatus")
    private String adminPasswordStatus;

    @WMIAttribute(name = "AutomaticManagedPagefile")
    private String automaticManagedPagefile;

    @WMIAttribute(name = "AutomaticResetBootOption")
    private String automaticResetBootOption;

    @WMIAttribute(name = "AutomaticResetCapability")
    private String automaticResetCapability;

    @WMIAttribute(name = "BootOptionOnLimit")
    private String bootOptionOnLimit;

    @WMIAttribute(name = "BootOptionOnWatchDog")
    private String bootOptionOnWatchDog;

    @WMIAttribute(name = "BootROMSupported")
    private String bootROMSupported;

    @WMIAttribute(name = "BootupState")
    private String bootupState;

    @WMIAttribute(name = "BootStatus")
    private String bootStatus;  // []

    @WMIAttribute(name = "Caption")
    private String caption;

    @WMIAttribute(name = "ChassisBootupState")
    private String chassisBootupState;

    @WMIAttribute(name = "ChassisSKUNumber")
    private String chassisSKUNumber;

    @WMIAttribute(name = "CreationClassName")
    private String creationClassName;

    @WMIAttribute(name = "CurrentTimeZone")
    private String currentTimeZone;

    @WMIAttribute(name = "DaylightInEffect")
    private String daylightInEffect;

    @WMIAttribute(name = "Description")
    private String description;

    @WMIAttribute(name = "DNSHostName")
    private String dNSHostName;

    @WMIAttribute(name = "Domain")
    private String domain;

    @WMIAttribute(name = "DomainRole")
    private String domainRole;

    @WMIAttribute(name = "EnableDaylightSavingsTime")
    private String enableDaylightSavingsTime;

    @WMIAttribute(name = "FrontPanelResetStatus")
    private String frontPanelResetStatus;

    @WMIAttribute(name = "HypervisorPresent")
    private String hypervisorPresent;

    @WMIAttribute(name = "InfraredSupported")
    private String infraredSupported;

    @WMIAttribute(name = "InitialLoadInfo")
    private String initialLoadInfo;     // []

    @WMIAttribute(name = "InstallDate")
    private String installDate;

    @WMIAttribute(name = "KeyboardPasswordStatus")
    private String keyboardPasswordStatus;

    @WMIAttribute(name = "LastLoadInfo")
    private String lastLoadInfo;

    @WMIAttribute(name = "Manufacturer")
    private String manufacturer;

    @WMIAttribute(name = "Model")
    private String model;

    @WMIAttribute(name = "Name")
    private String name;

    @WMIAttribute(name = "NameFormat")
    private String nameFormat;

    @WMIAttribute(name = "NetworkServerModeEnabled")
    private String networkServerModeEnabled;

    @WMIAttribute(name = "NumberOfLogicalProcessors")
    private String numberOfLogicalProcessors;

    @WMIAttribute(name = "NumberOfProcessors")
    private String numberOfProcessors;

    @WMIAttribute(name = "OEMLogoBitmap")
    private String oEMLogoBitmap;       // []

    @WMIAttribute(name = "OEMStringArray")
    private String oEMStringArray;    // []

    @WMIAttribute(name = "PartOfDomain")
    private String partOfDomain;

    @WMIAttribute(name = "PauseAfterReset")
    String pauseAfterReset;

    @WMIAttribute(name = "PCSystemType")
    private String pCSystemType;

    @WMIAttribute(name = "PCSystemTypeEx")
    private String pCSystemTypeEx;

    @WMIAttribute(name = "PowerManagementCapabilities")
    private String powerManagementCapabilities;   // []

    @WMIAttribute(name = "PowerManagementSupported")
    private String powerManagementSupported;

    @WMIAttribute(name = "PowerOnPasswordStatus")
    private String powerOnPasswordStatus;

    @WMIAttribute(name = "PowerState")
    private String powerState;

    @WMIAttribute(name = "PowerSupplyState")
    private String powerSupplyState;

    @WMIAttribute(name = "PrimaryOwnerContact")
    private String primaryOwnerContact;

    @WMIAttribute(name = "PrimaryOwnerName")
    private String primaryOwnerName;

    @WMIAttribute(name = "ResetCapability")
    private String resetCapability;

    @WMIAttribute(name = "ResetCount")
    private String resetCount;

    @WMIAttribute(name = "ResetLimit")
    private String resetLimit;

    @WMIAttribute(name = "Roles")
    private String Roles;                 // []

    @WMIAttribute(name = "Status")
    private String status;

    @WMIAttribute(name = "SupportContactDescription")
    private String supportContactDescription; // []

    @WMIAttribute(name = "SystemFamily")
    private String systemFamily;

    @WMIAttribute(name = "SystemSKUNumber")
    private String systemSKUNumber;

    @WMIAttribute(name = "SystemStartupDelay")
    private String systemStartupDelay;

    @WMIAttribute(name = "SystemStartupOptions")
    private String systemStartupOptions;    // []

    @WMIAttribute(name = "SystemStartupSetting")
    private String systemStartupSetting;

    @WMIAttribute(name = "SystemType")
    private String systemType;

    @WMIAttribute(name = "ThermalState")
    private String thermalState;

    @WMIAttribute(name = "TotalPhysicalMemory")
    private String totalPhysicalMemory;

    @WMIAttribute(name = "UserName")
    private String userName;

    @WMIAttribute(name = "WakeUpType")
    private String wakeUpType;

    @WMIAttribute(name = "Workgroup")
    private String workgroup;

}