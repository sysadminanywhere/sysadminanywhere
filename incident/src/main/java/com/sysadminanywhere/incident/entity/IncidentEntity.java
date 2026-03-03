package com.sysadminanywhere.incident.entity;

import com.sysadminanywhere.incident.model.IncidentStatus;
import com.sysadminanywhere.incident.model.Severity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "incidents",
        indexes = {
                @Index(name = "idx_incident_signal", columnList = "signalId"),
                @Index(name = "idx_incident_status", columnList = "status"),
                @Index(name = "idx_incident_created", columnList = "createdAt"),
                @Index(name = "idx_incident_hash", columnList = "deduplicationKey")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID сигнала (S1, S2 ...)
     */
    @Column(nullable = false, length = 20)
    private String signalId;

    /**
     * Название сигнала
     */
    @Column(nullable = false)
    private String name;

    /**
     * Уровень критичности
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    /**
     * Текущий статус
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status;

    /**
     * Количество событий в инциденте
     */
    private Integer eventCount;

    /**
     * Скомпилированная рекомендация
     */
    @Column(columnDefinition = "TEXT")
    private String recommendation;

    /**
     * JSON контекст (user_name, source_host и т.п.)
     */
    @Column(columnDefinition = "TEXT")
    private String context;

    /**
     * Для S21 (Repeated Incident)
     */
    private String affectedUser;

    /**
     * Для дедупликации инцидентов
     * hash(signalId + groupBy fields)
     */
    @Column(length = 255)
    private String deduplicationKey;

    /**
     * Является ли meta-инцидентом (S21, S22)
     */
    private Boolean meta;

    /**
     * Время первого события
     */
    private LocalDateTime firstEventTime;

    /**
     * Время последнего события
     */
    private LocalDateTime lastEventTime;

    /**
     * Время создания инцидента
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Время обновления
     */
    private LocalDateTime updatedAt;

    /**
     * Кто закрыл
     */
    private String closedBy;

    /**
     * Когда закрыли
     */
    private LocalDateTime closedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = IncidentStatus.OPEN;
        }
        if (meta == null) {
            meta = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    String machineName;

}