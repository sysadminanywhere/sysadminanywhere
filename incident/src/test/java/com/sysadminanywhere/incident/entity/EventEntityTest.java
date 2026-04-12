package com.sysadminanywhere.incident.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventEntityTest {

    @Test
    void constructor_shouldCreateEmptyEvent() {
        EventEntity event = new EventEntity();

        assertNull(event.getId());
        assertNull(event.getEventId());
        assertNull(event.getMachineName());
        assertNull(event.getTimeCreated());
        assertNull(event.getExtra());
        assertNull(event.getIncidentId());
    }

    @Test
    void setters_shouldUpdateValues() {
        EventEntity event = new EventEntity();

        event.setId(1L);
        event.setEventId(1001);
        event.setMachineName("PC001");
        event.setTimeCreated(LocalDateTime.now());
        event.setExtra("{\"key\":\"value\"}");
        event.setIncidentId(10L);

        assertEquals(1L, event.getId());
        assertEquals(1001, event.getEventId());
        assertEquals("PC001", event.getMachineName());
        assertNotNull(event.getTimeCreated());
        assertEquals("{\"key\":\"value\"}", event.getExtra());
        assertEquals(10L, event.getIncidentId());
    }

    @Test
    void equals_shouldCompareEvents() {
        EventEntity e1 = new EventEntity();
        e1.setId(1L);
        e1.setEventId(1001);

        EventEntity e2 = new EventEntity();
        e2.setId(1L);
        e2.setEventId(1001);

        EventEntity e3 = new EventEntity();
        e3.setId(2L);
        e3.setEventId(1002);

        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        EventEntity e1 = new EventEntity();
        e1.setId(1L);
        e1.setEventId(1001);

        EventEntity e2 = new EventEntity();
        e2.setId(1L);
        e2.setEventId(1001);

        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
