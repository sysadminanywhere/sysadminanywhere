package com.sysadminanywhere.incident.repository;

import com.sysadminanywhere.incident.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    /**
     * Найти последние сохранённые события
     */
    List<EventEntity> findTop100ByOrderByRecordIdDesc();

    /**
     * Получить последние событие по RecordId
     */
    EventEntity findTopByOrderByRecordIdDesc();

    /**
     * Получить все события, ещё не привязанные к инциденту
     */
    List<EventEntity> findByIncidentIdIsNull();

    /**
     * Получить события по eventId за последние N минут
     */
    @Query("SELECT e FROM EventEntity e " +
            "WHERE e.eventId IN :eventIds " +
            "AND e.timeCreated >= :from")
    List<EventEntity> findRecentEvents(
            @Param("eventIds") List<Integer> eventIds,
            @Param("from") LocalDateTime from);

    /**
     * Получить события по фильтру groupBy и time window
     * Используется для агрегации по сигналу
     */
    @Query("SELECT e FROM EventEntity e " +
            "WHERE e.eventId IN :eventIds " +
            "AND e.timeCreated BETWEEN :from AND :to")
    List<EventEntity> findEventsForSignal(
            @Param("eventIds") List<Integer> eventIds,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /**
     * Получить события по RecordId > lastId
     * Используется для incremental загрузки
     */
    List<EventEntity> findByRecordIdGreaterThanOrderByRecordIdAsc(Long lastId);

    /**
     * Получить события для корреляции по eventId и group key (например user_name + source_host)
     */
    @Query("SELECT e FROM EventEntity e " +
            "WHERE e.eventId IN :eventIds " +
            "AND e.timeCreated >= :from " +
            "AND e.timeCreated <= :to")
    List<EventEntity> findForCorrelation(
            @Param("eventIds") List<Integer> eventIds,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

}