package com.sysadminanywhere.model;

import java.time.LocalDateTime;
import java.util.List;

public record SecurityAuditSnapshot(LocalDateTime checkedAt,
                                    int privilegedUsers,
                                    int privilegedGroups,
                                    int usersWithSpn,
                                    int computersWithSpn,
                                    int usersMissingContactData,
                                    List<SecurityFinding> findings,
                                    String error) {
}
