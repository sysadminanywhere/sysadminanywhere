package com.sysadminanywhere.incident.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue
    private Long id;

    private Long recordId;

    private Integer eventId;

    private OffsetDateTime timeCreated;

    private String machineName;

    private String levelDisplayName;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "jsonb")
    private String extra; // JSONB как строка

    private Boolean processed = false;

}