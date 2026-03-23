package com.sysadminanywhere.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "hardware_models", indexes = {
        @Index(name = "idx_model_name_type", columnList = "name, hardware_type", unique = true)
})
public class HardwareModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String name;

    @JoinColumn(name = "hardware_type", nullable = false)
    private String hardwareType;

    @OneToMany(mappedBy = "hardwareModel", cascade = CascadeType.ALL)
    private List<ComputerHardware> computerHardwares;

}