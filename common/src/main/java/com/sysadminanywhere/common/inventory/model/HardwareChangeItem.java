package com.sysadminanywhere.common.inventory.model;

import java.time.LocalDateTime;

public record HardwareChangeItem(Long id, Long computerId, String computerName, String hardwareType,
                                 String hardwareName, String changeType, String propertyName,
                                 String oldValue, String newValue, LocalDateTime changedAt) {
}
