package com.sysadminanywhere.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "hardware_changes", indexes = {
        @Index(name = "idx_hardware_change_computer_date", columnList = "computer_id, changed_at"),
        @Index(name = "idx_hardware_change_model_date", columnList = "hardware_model_id, changed_at")
})
public class HardwareChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "computer_id", nullable = false)
    private Computer computer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hardware_model_id")
    private HardwareModel hardwareModel;

    @Column(nullable = false, length = 40)
    private String changeType;

    @Column(length = 255)
    private String propertyName;

    @Column(length = 1000)
    private String oldValue;

    @Column(length = 1000)
    private String newValue;

    @Column(nullable = false)
    private LocalDateTime changedAt;
}
