package com.sysadminanywhere.incident.repository;

import com.sysadminanywhere.incident.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findBySignalIdAndAffectedUserAndStatus(String signalId, String affectedUser, String status);
}