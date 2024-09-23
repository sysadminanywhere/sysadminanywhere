package com.sysadminanywhere.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessEntity {

    @WMIAttribute(name = "Caption")
    String caption;

    @WMIAttribute(name = "ExecutablePath")
    String executablePath;

    @WMIAttribute(name = "Description")
    String description;

    @WMIAttribute(name = "Handle")
    String handle;

    @WMIAttribute(name = "WorkingSetSize")
    String workingSetSize;
}
