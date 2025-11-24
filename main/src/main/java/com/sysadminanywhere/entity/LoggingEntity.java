package com.sysadminanywhere.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

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

    @Column(name = "logdate", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime logDate;

    @Column(name = "username", nullable = false)
    private String userName;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "parameters", columnDefinition = "TEXT", nullable = false)
    private String parameters;

}
