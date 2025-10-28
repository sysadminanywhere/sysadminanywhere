package com.sysadminanywhere.inventory.wmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseboardEntity {

    @WMIAttribute(name = "Product")
    private String product;

    @WMIAttribute(name = "Manufacturer")
    private String manufacturer;

    @WMIAttribute(name = "HotSwappable")
    private String hotSwappable;

    @WMIAttribute(name = "HostingBoard")
    private String hostingBoard;

    @WMIAttribute(name = "Removable")
    private String removable;

    @WMIAttribute(name = "Replaceable")
    private String replaceable;

    @WMIAttribute(name = "RequiresDaughterBoard")
    private String requiresDaughterBoard;

    @WMIAttribute(name = "Version")
    private String version;

    @WMIAttribute(name = "SerialNumber")
    private String serialNumber;

    @WMIAttribute(name = "TotalCpuSockets")
    private String totalCpuSockets;

}