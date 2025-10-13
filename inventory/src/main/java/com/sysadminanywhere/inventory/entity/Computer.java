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
@Table(name = "computers")
public class Computer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String dns;

    @OneToMany(mappedBy = "computer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Installation> installations;

    @OneToMany(mappedBy = "computer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ComputerHardware> computerHardwares;

}