package com.sysadminanywhere.incident.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "incident_events")
@IdClass(IncidentEventId.class)
public class IncidentEvent {

    @Id
    private Long incidentId;
    @Id
    private Long eventId;

}
