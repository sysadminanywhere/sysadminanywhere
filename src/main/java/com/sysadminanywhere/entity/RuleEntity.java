package com.sysadminanywhere.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@Table(name = "rules")
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @Column(length = 100)
    private String cronExpression;

    private String type;

    @Column(length = 2048)
    private String parameters;

    private boolean active;

}