package com.sysadminanywhere.common.inventory.model;

public record InventoryCoverage(long computers, long withOperatingSystem, long withPatches,
                                long softwareWithoutVersion, Integer patchStaleDays) { }
