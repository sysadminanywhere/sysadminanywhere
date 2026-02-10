package com.sysadminanywhere.incident.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue
    private Long id;
    private String signalId;
    private String title;
    private String severity;
    private String status = "new";
    private String affectedUser;
    private String sourceHost;
    private LocalDateTime timestamp;
    private LocalDateTime lastUpdated;
    @Column(columnDefinition = "TEXT")
    private String explanation;
    @Column(columnDefinition = "jsonb")
    private String recommendation; // JSON массив

}
