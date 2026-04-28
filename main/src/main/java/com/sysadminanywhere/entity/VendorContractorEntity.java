package com.sysadminanywhere.entity;

import com.sysadminanywhere.common.vendor.model.VendorContractor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vendor_contractors", indexes = {
        @Index(name = "idx_vendor_contractors_company", columnList = "company"),
        @Index(name = "idx_vendor_contractors_status", columnList = "status"),
        @Index(name = "idx_vendor_contractors_end_date", columnList = "end_date")
})
public class VendorContractorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String company;
    private String email;
    private String phone;

    @Column(name = "ad_username")
    private String adUsername;

    @Column(name = "ad_domain")
    private String adDomain;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level")
    private VendorContractor.AccessLevel accessLevel;

    @Enumerated(EnumType.STRING)
    private VendorContractor.ContractorStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public VendorContractor toModel() {
        return VendorContractor.builder()
                .id(id)
                .name(name)
                .company(company)
                .email(email)
                .phone(phone)
                .adUsername(adUsername)
                .adDomain(adDomain)
                .startDate(startDate)
                .endDate(endDate)
                .accessLevel(accessLevel)
                .status(status)
                .notes(notes)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static VendorContractorEntity fromModel(VendorContractor model) {
        VendorContractorEntity entity = new VendorContractorEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setCompany(model.getCompany());
        entity.setEmail(model.getEmail());
        entity.setPhone(model.getPhone());
        entity.setAdUsername(model.getAdUsername());
        entity.setAdDomain(model.getAdDomain());
        entity.setStartDate(model.getStartDate());
        entity.setEndDate(model.getEndDate());
        entity.setAccessLevel(model.getAccessLevel());
        entity.setStatus(model.getStatus());
        entity.setNotes(model.getNotes());
        return entity;
    }

}
