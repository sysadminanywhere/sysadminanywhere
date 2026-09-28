package com.sysadminanywhere.common.inventory.model;

import java.time.LocalDateTime;

public record HardwareComputerItem(Long id, String name, LocalDateTime checkedAt,
                                   String scanStatus, String scanError, long componentCount) {
}
