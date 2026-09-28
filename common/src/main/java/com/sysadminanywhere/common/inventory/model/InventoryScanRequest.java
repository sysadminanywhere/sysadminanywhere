package com.sysadminanywhere.common.inventory.model;

import java.util.List;

public record InventoryScanRequest(List<String> computerNames) {
}
