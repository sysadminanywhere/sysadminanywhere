package com.sysadminanywhere.model.hardware;

import com.sysadminanywhere.model.WMIAttribute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperatingSystemEntity {

    @WMIAttribute(name = "OSType")
    String oSType;

    @WMIAttribute(name = "Caption")
    String Caption;

    @WMIAttribute(name = "Manufacturer")
    String manufacturer;

    @WMIAttribute(name = "Version")
    String version;

    @WMIAttribute(name = "CSDVersion")
    String cSDVersion;

    @WMIAttribute(name = "SerialNumber")
    String serialNumber;

    @WMIAttribute(name = "OSArchitecture")
    String oSArchitecture;

    @WMIAttribute(name = "OperatingSystemSKU")
    String operatingSystemSKU;

    @WMIAttribute(name = "Locale")
    String locale;

    @WMIAttribute(name = "CountryCode")
    String countryCode;

    @WMIAttribute(name = "OSLanguage")
    String oSLanguage;

    @WMIAttribute(name = "Organization")
    String organization;

    @WMIAttribute(name = "SystemDirectory")
    String systemDirectory;

}
