package com.sysadminanywhere.common.inventory.model;

import java.time.LocalDate;

public record SoftwareLicense(Long id, String name, String vendor, String version,
                              long purchased, LocalDate expiresAt, String notes) {
}
