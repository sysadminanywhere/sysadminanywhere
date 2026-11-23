package com.sysadminanywhere.common.inventory.model;

import java.time.Instant;

public record InventorySchedule(String cron, boolean enabled, Instant nextRun, String zoneId) {
    public InventorySchedule(String cron, boolean enabled) {
        this(cron, enabled, null, null);
    }
}
