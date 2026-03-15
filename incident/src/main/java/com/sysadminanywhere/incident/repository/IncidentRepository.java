package com.sysadminanywhere.incident.repository;

import com.sysadminanywhere.common.incident.model.Severity;
import com.sysadminanywhere.incident.entity.IncidentEntity;
import com.sysadminanywhere.common.incident.model.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidentRepository extends JpaRepository<IncidentEntity, Long> {

    /**
     * Найти все инциденты за последние X минут
     * Используется для meta сигналов (S21, S22)
     */
    @Query("SELECT i FROM IncidentEntity i " +
            "WHERE i.createdAt >= :from")
    List<IncidentEntity> findRecent(@Param("from") LocalDateTime from);

    /**
     * Найти последние N минут инцидентов
     */
    default List<IncidentEntity> findRecent(int minutes) {
        LocalDateTime from = LocalDateTime.now().minusMinutes(minutes);
        return findRecent(from);
    }

    /**
     * Найти инциденты по signalId и affectedUser
     * Используется для повторяющихся инцидентов
     */
    List<IncidentEntity> findBySignalIdAndAffectedUserAndCreatedAtAfter(
            String signalId,
            String affectedUser,
            LocalDateTime after);

    /**
     * Найти инциденты по дедупликационному ключу
     * Чтобы не создавать дубликаты
     */
    IncidentEntity findByDeduplicationKey(String key);

    @Query("""
        SELECT i FROM IncidentEntity i
        WHERE i.severity = :severity
            AND i.status = :status
        ORDER BY i.createdAt DESC
        """)
    Page<IncidentEntity> findWithFilters(
            @Param("severity") Severity severity,
            @Param("status") IncidentStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT i FROM IncidentEntity i
        WHERE i.status = :status
        ORDER BY i.createdAt DESC
        """)
    Page<IncidentEntity> findWithStatus(
            @Param("status") IncidentStatus status,
            Pageable pageable
    );

}