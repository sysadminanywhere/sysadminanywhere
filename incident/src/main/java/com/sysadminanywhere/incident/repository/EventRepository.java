package com.sysadminanywhere.incident.repository;

import com.sysadminanywhere.incident.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByProcessedFalseOrderByTimestampAsc();
}