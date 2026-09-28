package com.sysadminanywhere.common.inventory.model;

import java.time.LocalDateTime;

public record InventoryScanStatus(boolean running, int processed, int total,
                                  LocalDateTime startedAt, LocalDateTime finishedAt,
                                  String lastError) {
}
