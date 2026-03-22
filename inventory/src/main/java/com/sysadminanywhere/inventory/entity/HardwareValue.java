package com.sysadminanywhere.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "hardware_values")
public class HardwareValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "computer_id", nullable = false)
    private Computer computer;

    @OneToMany(mappedBy = "hardwareValue", fetch = FetchType.LAZY)
    private Set<HardwareProperty> hardwareProperties;

    @Column(nullable = false)
    private String propertyValue;

}