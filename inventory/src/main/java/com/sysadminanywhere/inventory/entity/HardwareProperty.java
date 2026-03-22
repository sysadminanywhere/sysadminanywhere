package com.sysadminanywhere.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hardware_properties", indexes = {
        @Index(name = "idx_hw_prop_hw_id", columnList = "computer_hardware_id"),
        @Index(name = "idx_hw_prop_name", columnList = "property_name"),
        @Index(name = "idx_hw_prop_comp_hw_name", columnList = "computer_hardware_id, property_name")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HardwareProperty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "computer_hardware_id", nullable = false)
    private ComputerHardware computerHardware;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hardware_value_id", nullable = false)
    private HardwareValue hardwareValue;

    @Column(name = "property_name", nullable = false, length = 255)
    private String propertyName;

    @Column(name = "property_value", nullable = false, length = 1000)
    private String propertyValue;

}