package com.sysadminanywhere.common.directory.dto;

import java.util.UUID;

public record OrganizationEmployee(UUID id, String distinguishedName, String cn, String name, String login,
                                   String title, String department, String country, String office,
                                   String managerDn, boolean disabled) {
}
