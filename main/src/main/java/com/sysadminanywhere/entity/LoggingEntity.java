package com.sysadminanywhere.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "logging")
public class LoggingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logdate", nullable = false)
    private LocalDateTime logDate;

    @Column(name = "username", nullable = false)
    private String userName;

    @Column(name = "action", nullable = false)
    private String action;

}
