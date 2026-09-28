package com.sysadminanywhere.common.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomainHealthDto {
    private String overallStatus;
    private LocalDateTime checkedAt;
    private List<DomainHealthCheckDto> checks;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DomainHealthCheckDto {
        private String name;
        private String status;
        private String details;
    }
}
