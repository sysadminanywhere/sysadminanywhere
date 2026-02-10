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
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue
    private Long id;
    private Integer eventId;
    private String signalId;
    private String eventType;
    private LocalDateTime timestamp;
    private String userName;
    private String sourceHost;
    private String targetHost;
    private String domain;
    private String logName;
    @Column(columnDefinition = "TEXT")
    private String message;
    @Column(columnDefinition = "jsonb")
    private String extra; // JSONB как строка
    private Boolean processed = false;

}