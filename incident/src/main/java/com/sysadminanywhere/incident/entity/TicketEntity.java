package com.sysadminanywhere.incident.entity;

import com.sysadminanywhere.common.incident.model.Category;
import com.sysadminanywhere.common.incident.model.Priority;
import com.sysadminanywhere.common.incident.model.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tickets",
        indexes = {
                @Index(name = "idx_ticket_number", columnList = "ticketNumber", unique = true),
                @Index(name = "idx_ticket_status", columnList = "status"),
                @Index(name = "idx_ticket_priority", columnList = "priority"),
                @Index(name = "idx_ticket_category", columnList = "category"),
                @Index(name = "idx_ticket_assignee", columnList = "assignee"),
                @Index(name = "idx_ticket_incident", columnList = "incidentId"),
                @Index(name = "idx_ticket_created", columnList = "createdAt"),
                @Index(name = "idx_ticket_status_created", columnList = "status, createdAt")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String ticketNumber;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(length = 100)
    private String requester;

    @Column(length = 100)
    private String assignee;

    private Long incidentId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

    @Column(length = 100)
    private String resolvedBy;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TicketStatus.OPEN;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
