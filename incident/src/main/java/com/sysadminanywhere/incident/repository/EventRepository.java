package com.sysadminanywhere.incident.repository;

import com.sysadminanywhere.incident.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    Optional<EventEntity> findTopByOrderByRecordIdDesc();
    List<EventEntity> findByProcessedFalseOrderByTimestampAsc();

}