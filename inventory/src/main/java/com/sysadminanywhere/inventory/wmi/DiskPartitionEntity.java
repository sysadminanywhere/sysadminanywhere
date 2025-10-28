package com.sysadminanywhere.inventory.wmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiskPartitionEntity {

    @WMIAttribute(name = "Name")
    private String name;

    @WMIAttribute(name = "Description")
    private String description;

    @WMIAttribute(name = "DiskIndex")
    private String diskIndex;

    @WMIAttribute(name = "Bootable")
    private String bootable;

    @WMIAttribute(name = "BootPartition")
    private String bootPartition;

    @WMIAttribute(name = "Size")
    private String size;

    @WMIAttribute(name = "StartingOffset")
    private String startingOffset;

}