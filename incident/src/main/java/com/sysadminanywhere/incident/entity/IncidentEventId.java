package com.sysadminanywhere.incident.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncidentEventId implements Serializable {

    private Long incidentId;
    private Long eventId;

}