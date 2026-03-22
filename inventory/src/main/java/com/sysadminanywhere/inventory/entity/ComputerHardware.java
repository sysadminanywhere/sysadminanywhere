package com.sysadminanywhere.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "computer_hardwares", indexes = {
        @Index(name = "idx_computer_hw_link", columnList = "computer_id, hardware_model_id", unique = true)
})
public class ComputerHardware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "computer_id", nullable = false)
    private Computer computer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hardware_model_id", nullable = false)
    private HardwareModel hardwareModel;

    @Column(name = "checking_date")
    private LocalDateTime checkingDate;

    @OneToMany(mappedBy = "computerHardware", cascade = CascadeType.ALL)
    private List<HardwareProperty> properties;

}