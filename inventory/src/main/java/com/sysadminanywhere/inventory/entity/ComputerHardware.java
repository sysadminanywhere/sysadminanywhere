package com.sysadminanywhere.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "computer_hardware",
        indexes = {
                @Index(name = "idx_computer_hardware_computer", columnList = "computer_id"),
                @Index(name = "idx_computer_hardware_hardware", columnList = "hardware_id")
        }
)
public class ComputerHardware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "computer_id", nullable = false)
    private Computer computer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hardware_id", nullable = false)
    private Hardware hardware;

    @Column(nullable = false)
    private LocalDateTime checkingDate;

}