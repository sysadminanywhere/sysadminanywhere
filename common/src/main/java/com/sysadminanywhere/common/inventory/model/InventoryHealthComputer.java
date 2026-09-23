package com.sysadminanywhere.common.inventory.model;

import java.time.LocalDateTime;

public record InventoryHealthComputer(Long id, String name, LocalDateTime checkingDate, long daysSinceCheck) {
}
