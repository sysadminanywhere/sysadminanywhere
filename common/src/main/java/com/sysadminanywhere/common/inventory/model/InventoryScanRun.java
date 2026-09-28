package com.sysadminanywhere.common.inventory.model;

import java.time.LocalDateTime;

public record InventoryScanRun(Long id, LocalDateTime startedAt, LocalDateTime finishedAt,
                               String status, int processed, int total, String error) {
}
