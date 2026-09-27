package com.sysadminanywhere.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HardwareType {

    DISK_DRIVE("DiskDrive"),
    OPERATING_SYSTEM("OperatingSystem"),
    DISK_PARTITION("DiskPartition"),
    PROCESSOR("Processor"),
    VIDEO_CONTROLLER("VideoController"),
    PHYSICAL_MEMORY("PhysicalMemory"),
    BASE_BOARD("BaseBoard"),
    BIOS("BIOS"),
    COMPUTER_SYSTEM("ComputerSystem"),
    OPTICAL_DRIVE("OpticalDrive"),
    SOUND_DEVICE("SoundDevice"),
    KEYBOARD("Keyboard"),
    POINTING_DEVICE("PointingDevice"),
    NETWORK_ADAPTER("NetworkAdapter"),
    PATCH("Patch");

    private final String displayName;

    @Override
    public String toString() {
        return displayName;
    }
}
