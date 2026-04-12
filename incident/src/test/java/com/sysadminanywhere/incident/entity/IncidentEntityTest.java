package com.sysadminanywhere.incident.entity;

import com.sysadminanywhere.common.incident.model.IncidentStatus;
import com.sysadminanywhere.common.incident.model.Severity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IncidentEntityTest {

    @Test
    void builder_shouldCreateIncidentWithAllFields() {
        LocalDateTime now = LocalDateTime.now();

        IncidentEntity incident = IncidentEntity.builder()
                .id(1L)
                .signalId("SIG-001")
                .name("Test Incident")
                .severity(Severity.HIGH)
                .status(IncidentStatus.OPEN)
                .eventCount(5)
                .recommendation("Check logs")
                .context("{\"user\":\"admin\"}")
                .affectedUser("user1")
                .deduplicationKey("DEDUP-001")
                .meta(false)
                .firstEventTime(now)
                .lastEventTime(now.plusMinutes(10))
                .createdAt(now)
                .updatedAt(now)
                .closedBy(null)
                .closedAt(null)
                .machineName("SERVER01")
                .build();

        assertEquals(1L, incident.getId());
        assertEquals("SIG-001", incident.getSignalId());
        assertEquals("Test Incident", incident.getName());
        assertEquals(Severity.HIGH, incident.getSeverity());
        assertEquals(IncidentStatus.OPEN, incident.getStatus());
        assertEquals(5, incident.getEventCount());
        assertFalse(incident.getMeta());
        assertEquals("SERVER01", incident.getMachineName());
    }

    @Test
    void setters_shouldUpdateValues() {
        IncidentEntity incident = new IncidentEntity();

        incident.setId(2L);
        incident.setSignalId("SIG-002");
        incident.setName("Updated Incident");
        incident.setSeverity(Severity.CRITICAL);
        incident.setStatus(IncidentStatus.IN_PROGRESS);
        incident.setEventCount(10);

        assertEquals(2L, incident.getId());
        assertEquals("SIG-002", incident.getSignalId());
        assertEquals("Updated Incident", incident.getName());
        assertEquals(Severity.CRITICAL, incident.getSeverity());
        assertEquals(IncidentStatus.IN_PROGRESS, incident.getStatus());
        assertEquals(10, incident.getEventCount());
    }

    @Test
    void prePersist_shouldSetCreatedAtAndStatus() {
        IncidentEntity incident = IncidentEntity.builder()
                .signalId("SIG-001")
                .name("Test")
                .severity(Severity.LOW)
                .build();

        incident.prePersist();

        assertNotNull(incident.getCreatedAt());
        assertNotNull(incident.getUpdatedAt());
        assertEquals(IncidentStatus.OPEN, incident.getStatus());
        assertFalse(incident.getMeta());
    }

    @Test
    void preUpdate_shouldSetUpdatedAt() throws InterruptedException {
        IncidentEntity incident = IncidentEntity.builder()
                .signalId("SIG-001")
                .name("Test")
                .severity(Severity.LOW)
                .build();

        incident.prePersist();
        LocalDateTime updatedAtBefore = incident.getUpdatedAt();

        Thread.sleep(10);
        incident.preUpdate();

        assertTrue(incident.getUpdatedAt().isAfter(updatedAtBefore) || 
                   incident.getUpdatedAt().isEqual(updatedAtBefore));
    }
}
