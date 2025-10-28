package com.sysadminanywhere.inventory.wmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceEntity {

    @WMIAttribute(name = "Caption")
    String caption;

    @WMIAttribute(name = "Description")
    String description;

    @WMIAttribute(name = "Name")
    String name;

    @WMIAttribute(name = "State")
    String state;

    @WMIAttribute(name = "PathName")
    String pathName;

    @WMIAttribute(name = "DisplayName")
    String displayName;

    @WMIAttribute(name = "StartMode")
    String startMode;

    @WMIAttribute(name = "ProcessId")
    String processId;
}
