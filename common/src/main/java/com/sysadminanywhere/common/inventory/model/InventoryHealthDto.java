package com.sysadminanywhere.common.inventory.model;

import java.time.LocalDateTime;
import java.util.List;

public record InventoryHealthDto(LocalDateTime checkedAt, int staleDays, int totalComputers,
                                int staleCount, int neverScannedCount,
                                List<InventoryHealthComputer> computers) {
}
