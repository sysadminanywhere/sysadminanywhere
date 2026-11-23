package com.sysadminanywhere.common.inventory.model;

import java.time.Instant;
import java.time.LocalDate;

public record PatchRemediation(String computer, String owner, LocalDate dueDate, Instant verifiedAt) {
}
