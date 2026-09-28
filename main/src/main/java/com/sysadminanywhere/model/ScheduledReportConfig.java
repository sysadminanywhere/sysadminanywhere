package com.sysadminanywhere.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledReportConfig {
    private String id;
    private boolean enabled;
    private String entry;
    private String reportId;
    private String frequency;
    private int hour;
    private int minute;
    private String format;
    private String recipients;
}
