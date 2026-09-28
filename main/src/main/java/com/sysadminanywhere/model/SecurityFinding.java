package com.sysadminanywhere.model;

public record SecurityFinding(String category, String severity, String objectType,
                              String name, String distinguishedName, String details) {
}
