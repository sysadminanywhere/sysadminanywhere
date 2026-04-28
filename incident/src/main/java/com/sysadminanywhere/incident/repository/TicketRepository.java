package com.sysadminanywhere.incident.repository;

import com.sysadminanywhere.common.incident.model.Category;
import com.sysadminanywhere.common.incident.model.Priority;
import com.sysadminanywhere.common.incident.model.TicketStatus;
import com.sysadminanywhere.incident.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    Optional<TicketEntity> findByTicketNumber(String ticketNumber);

    List<TicketEntity> findByAssignee(String assignee);

    List<TicketEntity> findByIncidentId(Long incidentId);

    @Query("""
        SELECT t FROM TicketEntity t
        WHERE (:status IS NULL OR t.status = :status)
            AND (:priority IS NULL OR t.priority = :priority)
            AND (:category IS NULL OR t.category = :category)
            AND (:assignee IS NULL OR t.assignee = :assignee)
        ORDER BY t.createdAt DESC
        """)
    Page<TicketEntity> findWithFilters(
            @Param("status") TicketStatus status,
            @Param("priority") Priority priority,
            @Param("category") Category category,
            @Param("assignee") String assignee,
            Pageable pageable
    );

    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.status = :status
        ORDER BY t.createdAt DESC
        """)
    Page<TicketEntity> findByStatus(
            @Param("status") TicketStatus status,
            Pageable pageable
    );

}
