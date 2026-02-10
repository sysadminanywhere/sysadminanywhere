package com.sysadminanywhere.incident.repository;

import com.sysadminanywhere.incident.entity.IncidentEvent;
import com.sysadminanywhere.incident.entity.IncidentEventId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentEventRepository extends JpaRepository<IncidentEvent, IncidentEventId> {

}