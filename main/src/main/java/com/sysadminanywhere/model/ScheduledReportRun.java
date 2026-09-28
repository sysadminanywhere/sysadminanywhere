package com.sysadminanywhere.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledReportRun {
    private String configId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String status;
    private String filePath;
    private String error;
}
