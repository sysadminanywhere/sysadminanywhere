package com.sysadminanywhere.common.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeJournalDto {
    private String id;
    private String objectName;
    private String distinguishedName;
    private String objectClass;
    private String action;
    private String actor;
    private LocalDateTime changedAt;
    private Map<String, String> before;
    private Map<String, String> after;
}
