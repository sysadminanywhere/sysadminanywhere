package com.sysadminanywhere.common.vendor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorContractor {

    private Long id;
    private String name;
    private String company;
    private String email;
    private String phone;
    private String adUsername;
    private String adDomain;
    private LocalDate startDate;
    private LocalDate endDate;
    private AccessLevel accessLevel;
    private ContractorStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum AccessLevel {
        READ_ONLY,
        STANDARD,
        ELEVATED,
        ADMIN
    }

    public enum ContractorStatus {
        ACTIVE,
        EXPIRED,
        REVOKED,
        PENDING
    }

}
