package com.sysadminanywhere.incident.repository;

import com.sysadminanywhere.incident.entity.IncidentEntity;
import com.sysadminanywhere.incident.model.IncidentStatus;
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
     * Найти все открытые инциденты
     */
    List<IncidentEntity> findByStatus(IncidentStatus status);

    /**
     * Найти инциденты по дедупликационному ключу
     * Чтобы не создавать дубликаты
     */
    IncidentEntity findByDeduplicationKey(String key);

    @Query("""
        SELECT i FROM IncidentEntity i
        WHERE (:status IS NULL OR i.status = :status)
          AND (:signalId IS NULL OR i.signalId = :signalId)
          AND (:from IS NULL OR i.createdAt >= :from)
          AND (:to IS NULL OR i.createdAt <= :to)
        ORDER BY i.createdAt DESC
        """)
    Page<IncidentEntity> findWithFilters(
            @Param("status") IncidentStatus status,
            @Param("signalId") String signalId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

}