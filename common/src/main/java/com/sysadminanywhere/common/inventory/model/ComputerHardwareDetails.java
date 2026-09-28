package com.sysadminanywhere.common.inventory.model;

import java.util.List;

public record ComputerHardwareDetails(HardwareComputerItem computer, List<HardwareModelItem> components) {
}
