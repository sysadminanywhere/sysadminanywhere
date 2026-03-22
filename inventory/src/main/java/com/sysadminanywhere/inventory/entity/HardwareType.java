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
@Table(name = "hardware_types", indexes = {
        @Index(name = "idx_hardware_type_name", columnList = "name", unique = true)
})
public class HardwareType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "hardwareType", cascade = CascadeType.ALL)
    private List<HardwareModel> hardwareModels; // Constructors

}